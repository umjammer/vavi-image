/*
 * Copyright 2005 Huxtable.com. All rights reserved.
 */

package vavi.awt.image.blur;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;

import vavi.awt.image.AbstractBufferedImageOp;


/**
 * A filter which applies Gaussian blur to an image. This is a subclass of
 * ConvolveFilter which simply creates a kernel with a Gaussian distribution for
 * blurring.
 *
 * @author Jerry Huxtable
 */
public class GaussianBlurOp extends AbstractBufferedImageOp {

    /** */
    public static int ZERO_EDGES = 0;
    /** */
    public static final int CLAMP_EDGES = 1;
    /** */
    public static final int WRAP_EDGES = 2;

    /** */
    protected final boolean alpha = true;

    /** */
    protected float radius;

    /** */
    protected Kernel kernel;

    /**
     * Construct a Gaussian filter
     */
    public GaussianBlurOp() {
        this(2);
    }

    /**
     * Construct a Gaussian filter
     *
     * @param radius blur radius in pixels
     */
    public GaussianBlurOp(float radius) {
        setRadius(radius);
    }

    /**
     * Set the radius of the kernel, and hence the amount of blur. The bigger
     * the radius, the longer this filter will take.
     *
     * @param radius the radius of the blur in pixels.
     */
    public void setRadius(float radius) {
        this.radius = radius;
        kernel = makeKernel(radius);
    }

    /**
     * Get the radius of the kernel.
     *
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        src.getRGB(0, 0, width, height, inPixels, 0, width);

        convolveAndTranspose(kernel, inPixels, outPixels, width, height, alpha, CLAMP_EDGES);
        convolveAndTranspose(kernel, outPixels, inPixels, height, width, alpha, CLAMP_EDGES);

        dst.setRGB(0, 0, width, height, inPixels, 0, width);
        return dst;
    }

    /** */
    protected static void convolveAndTranspose(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;

        for (int y = 0; y < height; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                int moffset = cols2;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[moffset + col];

                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            if (edgeAction == CLAMP_EDGES)
                                ix = 0;
                            else if (edgeAction == WRAP_EDGES)
                                ix = (x + width) % width;
                        } else if (ix >= width) {
                            if (edgeAction == CLAMP_EDGES)
                                ix = width - 1;
                            else if (edgeAction == WRAP_EDGES)
                                ix = (x + width) % width;
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = alpha ? clamp((int) (a + 0.5)) : 0xff;
                int ir = clamp((int) (r + 0.5));
                int ig = clamp((int) (g + 0.5));
                int ib = clamp((int) (b + 0.5));
                outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                index += height;
            }
        }
    }

    /**
     * Make a Gaussian blur kernel.
     */
    protected static Kernel makeKernel(float radius) {
        int r = (int) Math.ceil(radius);
        int rows = r * 2 + 1;
        float[] matrix = new float[rows];
        float sigma = radius / 3;
        float sigma22 = 2 * sigma * sigma;
        float sigmaPi2 = 2 * (float) Math.PI * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = row * row;
            if (distance > radius2)
                matrix[index] = 0;
            else
                matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++)
            matrix[i] /= total;

        return new Kernel(rows, 1, matrix);
    }

    /**
     * Clamp a value to the range 0..255
     */
    private static int clamp(int c) {
        if (c < 0) {
            return 0;
        }
        if (c > 255) {
            return 255;
        }
        return c;
    }
}
