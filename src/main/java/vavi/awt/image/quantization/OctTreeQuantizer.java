/*
 * Copyright (C) Jerry Huxtable 1998-2001. All rights reserved.
 */

package vavi.awt.image.quantization;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An image Quantizer based on the Octree algorithm. This is a very basic implementation
 * at present and could be much improved by picking the nodes to reduce more carefully
 * (i.e. not completely at random) when I get the time.
 */
public class OctTreeQuantizer implements Quantizer {

    /**
     * The greatest depth the tree is allowed to reach
     */
    final static int MAX_LEVEL = 5;

    /**
     * An Octtree node.
     */
    static class OctTreeNode {
        int children;
        int level;
        OctTreeNode parent;
        final OctTreeNode[] leaf = new OctTreeNode[8];
        boolean isLeaf;
        int count;
        int    totalRed;
        int    totalGreen;
        int    totalBlue;
        int index;

        /**
         * A debugging method which prints the tree out.
         */
        public void list(PrintStream s, int level) {
            for (int i = 0; i < level; i++)
                System.out.print(' ');
            if (count == 0) {
                System.out.println(index + ": count=" + count);
            } else {
                System.out.println(index + ": count=" + count + " red=" + (totalRed / count) + " green=" + (totalGreen / count) + " blue=" + (totalBlue / count));
            }
            for (int i = 0; i < 8; i++) {
                if (leaf[i] != null) {
                    leaf[i].list(s, level+2);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private int nodes = 0;
    private final OctTreeNode root;
    private int reduceColors;
    private int maximumColors;
    private int colors = 0;
    private final List<OctTreeNode>[] colorList;

    @SuppressWarnings("unchecked")
    public OctTreeQuantizer() {
        setup(256);
        colorList = new ArrayList[MAX_LEVEL + 1];
        for (int i = 0; i < MAX_LEVEL + 1; i++) {
            colorList[i] = new ArrayList<>();
        }
        root = new OctTreeNode();
    }

    /**
     * Initialize the quantizer. This should be called before adding any pixels.
     * @param numColors the number of colors we're quantizing to.
     */
    @Override
    public void setup(int numColors) {
        maximumColors = numColors;
        reduceColors = Math.max(512, numColors * 2);
    }

    /**
     * Add pixels to the quantizer.
     * @param pixels the array of ARGB pixels
     * @param offset the offset into the array
     * @param count the count of pixels
     */
    @Override
    public void addPixels(int[] pixels, int offset, int count) {
        for (int i = 0; i < count; i++) {
            insertColor(pixels[i + offset]);
            if (colors > reduceColors) {
                reduceTree(reduceColors);
            }
        }
    }

    @Override
    public int getIndexForColor(int rgb) {
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;

        OctTreeNode node = root;

        for (int level = 0; level <= MAX_LEVEL; level++) {
            OctTreeNode child;
            int bit = 0x80 >> level;

            int index = 0;
            if ((red & bit) != 0) {
                index += 4;
            }
            if ((green & bit) != 0) {
                index += 2;
            }
            if ((blue & bit) != 0) {
                index += 1;
            }

            child = node.leaf[index];

            if (child == null) {
                return node.index;
            } else if (child.isLeaf) {
                return child.index;
            } else {
                node = child;
            }
        }
        System.out.println("getIndexForColor failed");
        return 0;
    }

    private void insertColor(int rgb) {
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;

        OctTreeNode node = root;

//        System.out.println("insertColor="+Integer.toHexString(rgb));
        for (int level = 0; level <= MAX_LEVEL; level++) {
            OctTreeNode child;
            int bit = 0x80 >> level;

            int index = 0;
            if ((red & bit) != 0) {
                index += 4;
            }
            if ((green & bit) != 0) {
                index += 2;
            }
            if ((blue & bit) != 0) {
                index += 1;
            }

            child = node.leaf[index];

            if (child == null) {
                node.children++;

                child = new OctTreeNode();
                child.parent = node;
                node.leaf[index] = child;
                node.isLeaf = false;
                nodes++;
                colorList[level].add(child);

                if (level == MAX_LEVEL) {
                    child.isLeaf = true;
                    child.count = 1;
                    child.totalRed = red;
                    child.totalGreen = green;
                    child.totalBlue = blue;
                    child.level = level;
                    colors++;
                    return;
                }

                node = child;
            } else if (child.isLeaf) {
                child.count++;
                child.totalRed += red;
                child.totalGreen += green;
                child.totalBlue += blue;
                return;
            } else {
                node = child;
            }
        }
        System.out.println("insertColor failed");
    }

    private void reduceTree(int numColors) {
        for (int level = MAX_LEVEL-1; level >= 0; level--) {
            List<OctTreeNode> v = colorList[level];
            if (v != null && !v.isEmpty()) {
                for (OctTreeNode node : v) {
                    if (node.children > 0) {
                        for (int i = 0; i < 8; i++) {
                            OctTreeNode child = node.leaf[i];
                            if (child != null) {
                                if (!child.isLeaf) {
                                    System.out.println("not a leaf!");
                                }
                                node.count += child.count;
                                node.totalRed += child.totalRed;
                                node.totalGreen += child.totalGreen;
                                node.totalBlue += child.totalBlue;
                                node.leaf[i] = null;
                                node.children--;
                                colors--;
                                nodes--;
                                colorList[level + 1].remove(child);
                            }
                        }
                        node.isLeaf = true;
                        colors++;
                        if (colors <= numColors) {
                            return;
                        }
                    }
                }
            }
        }

        System.out.println("Unable to reduce the OctTree");
    }

    @Override
    public int[] buildColorTable() {
        int[] table = new int[colors];
        buildColorTable(root, table, 0);
        return table;
    }

    /**
     * A quick way to use the quantizer. Just create a table the right size and pass in the pixels.
     */
    public void buildColorTable(int[] inPixels, int[] table) {
        int count = inPixels.length;
        maximumColors = table.length;
        for (int inPixel : inPixels) {
            insertColor(inPixel);
            if (colors > reduceColors) {
                reduceTree(reduceColors);
            }
        }
        if (colors > maximumColors) {
            reduceTree(maximumColors);
        }
        buildColorTable(root, table, 0);
    }

    private int buildColorTable(OctTreeNode node, int[] table, int index) {
        if (colors > maximumColors) {
            reduceTree(maximumColors);
        }

        if (node.isLeaf) {
            int count = node.count;
            table[index] = 0xff000000 |
                ((node.totalRed/count) << 16) |
                ((node.totalGreen/count) << 8) |
                node.totalBlue/count;
            node.index = index++;
        } else {
            for (int i = 0; i < 8; i++) {
                if (node.leaf[i] != null) {
                    node.index = index;
                    index = buildColorTable(node.leaf[i], table, index);
                }
            }
        }
        return index;
    }
}
