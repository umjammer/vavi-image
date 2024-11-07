/*
 * https://web.archive.org/web/20161106215528/http://homepage1.nifty.com/uchi/software.htm
 */

package vavi.awt.image.gif;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vavi.io.LittleEndianDataInputStream;

import static java.lang.System.getLogger;


/**
 * GifImage.
 *
 * <pre>
 *
 *  function \ version | GIF87  | GIF87a | GIF89a
 *  -------------------+--------+--------+-------
 *  normal image       |   ✓    |   ✓    |   ✓
 *  interlace GIF      |   -    |   ✓    |   ✓
 *  transparent GIF    |   -    |   -    |   ✓
 *  GIF animation      |   -    |   -    |   ✓
 *
 * </pre>
 * <pre>
 *
 *  GIF Header
 *  Block
 *   :
 *  Trailer 0x3b
 *
 * </pre>
 *
 * @author DJ.Uchi [H.Uchida]
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 2.00 xxxxxx huchida original version <br>
 * @version 2.10 040914 nsano initial version <br>
 * @see "http://www.w3.org/Graphics/GIF/spec-gif89a.txt"
 */
public class GifImage {

    private static final Logger logger = getLogger(GifImage.class.getName());

    /** */
    private static final NonLzwGifDecoder decoder = new NonLzwGifDecoder();

    /**
     * GIF palette structure, 3 bytes in RGB order
     */
    private static class GifRGB {
        /** Red component */
        byte red;
        /** Green component */
        byte green;
        /** Blue component */
        byte blue;
        /** */
        static GifRGB readFrom(LittleEndianDataInputStream dis) throws IOException {
            GifRGB rgb = new GifRGB();
            rgb.red = (byte) dis.read();
            rgb.green = (byte) dis.read();
            rgb.blue = (byte) dis.read();
            return rgb;
        }
    }

    private static GifRGB[] readColorTable(LittleEndianDataInputStream dis, int sizeOfColorTable) throws IOException {
        int size = (int) Math.pow(2, sizeOfColorTable + 1);
        GifRGB[] colorTable = new GifRGB[size];
//logger.log(Level.TRACE, "colorTable: " + size);
        for (int i = 0; i < size; i++) {
            colorTable[i] = GifRGB.readFrom(dis);
//logger.log(Level.TRACE, "gifRGB: " + StringUtil.paramString(gifImage.palette[i]));
        }
        return colorTable;
    }

    /**
     * GIF screen description information structure Header section describing the entire GIF information.
     */
    private static class GifHeader {
        /** GIF Signature ("GIF") */
        final byte[] signature = new byte[3];
        /** GIF Version ("87a" or "89a") */
        final byte[] version = new byte[3];
        /**
         * The number of horizontal pixels needed to display this entire GIF.
         */
        @SuppressWarnings("unused")
        int logicalScreenWidth;
        /**
         * The number of vertical pixels needed to display this entire GIF.
         */
        @SuppressWarnings("unused")
        int logicalScreenHeight;
        /**
         * Packed Fields
         * <pre>
         * 7 654 3 210
         * ~ ~~~ ~ ~~~
         * | |   | +--- Size of Global Color Table
         * | |   +----- Sort Flag
         * | +--------- Color Resolution
         * +----------- Global Color Table Flag
         * </pre>
         */
        int packedFields;
        /** Background Color Index */
        @SuppressWarnings("unused")
        int backgroundColorIndex;
        /** Pixel Aspect Ratio */
        @SuppressWarnings("unused")
        int pixelAspectRatio;
        /**
         * @throws IllegalArgumentException not a gif
         */
        static GifHeader readFrom(LittleEndianDataInputStream dis) throws IOException {
            GifHeader gh = new GifHeader();
            dis.readFully(gh.signature);
            if (gh.signature[0] != 'G' || gh.signature[1] != 'I' || gh.signature[2] != 'F') {
                throw new IllegalArgumentException("not gif file");
            }
            dis.readFully(gh.version);
//System.err.println(new String(gh.signature) + new String(gh.version));
            gh.logicalScreenWidth = dis.readUnsignedShort();
            gh.logicalScreenHeight = dis.readUnsignedShort();
            gh.packedFields = dis.readUnsignedByte();
            gh.backgroundColorIndex = dis.readUnsignedByte();
            gh.pixelAspectRatio = dis.readUnsignedByte();
            return gh;
        }
        boolean hasGlobalColorTable() {
            return (packedFields & 0x80) != 0;
        }
        int getSizeOfGlobalColorTable() {
            return packedFields & 0x07;
        }
    }

    /**
     * GIF image description information structure Describes information about a single GIF image.
     */
    public static class ImageDescriptor {
        /** Display position from top left of screen (horizontal pixels) */
        public int left;
        /** Display position from the top left of the screen (vertical pixels) */
        public int top;
        /** The horizontal pixel count of this image */
        public int width;
        /** The vertical pixel count of this image */
        public int height;
        /**
         * Packed Fields
         * <pre>
         * 7 6 5 43 210
         * ~ ~ ~ ~~ ~~~
         * | | | |  +--- Size of Local Color Table
         * | | | +------ Reserved
         * | | +-------- Sort Flag
         * | +---------- Interlace Flag
         * +------------ Local Color Table Flag
         * </pre>
         */
        int packedFields;
        /** */
        static ImageDescriptor readFrom(LittleEndianDataInputStream dis) throws IOException {
            ImageDescriptor id = new ImageDescriptor();
            // gih.split = (byte) dis.read();
            id.left = dis.readUnsignedShort();
            id.top = dis.readUnsignedShort();
            id.width = dis.readUnsignedShort();
            id.height = dis.readUnsignedShort();
            id.packedFields = dis.readUnsignedByte();
            return id;
        }
        boolean isInteraced() {
            return (packedFields & 0x40) != 0;
        }
        boolean hasLocalColorTable() {
            return (packedFields & 0x80) != 0;
        }
        int getSizeOfColorTable() {
            return packedFields & 0x07;
        }
    }

    /** */
    public static class GraphicControlExtension {
        /** */
        int packedFields;
        /** */
        public int delayTime;
        /** */
        int transparentColorIndex;
        boolean hasTransparentColor() {
            return (packedFields & 0x01) != 0;
        }
        public int getDisposalMethod() {
            return (packedFields >> 2) & 0x07;
        }
    }

    /**
     * @return null when not exists
     */
    public GraphicControlExtension getGraphicControlExtension(int index) {
        if (extentionBlocks.get(index).containsKey(0xf9)) {
            ExtensionBlock extensionBlock = extentionBlocks.get(index).get(0xf9);
            GraphicControlExtension extention = new GraphicControlExtension();
            byte[] data = extensionBlock.subBlocks.get(0);
            extention.packedFields = data[0] & 0xff;
            extention.delayTime = data[1] & 0xff | (data[2] << 8) & 0xff00;
            extention.transparentColorIndex = data[3] & 0xff;
            return extention;
        } else {
            return null;
        }
    }

    /** */
    private static class InternalImage {
        /** */
        ImageDescriptor imageDescriptor;
        /** */
        int sizeOfColorTable;
        /** */
        GifRGB[] localColorTable;
        /** */
        byte[] tableBasedImageData;

        static InternalImage readFrom(LittleEndianDataInputStream dis, int sizeOfGlobalColorTable, GifRGB[] globalColorTable) throws IOException {
            InternalImage image = new InternalImage();

            // first image descriptor
            image.imageDescriptor = ImageDescriptor.readFrom(dis);

            // interlace flag
            boolean interlaced = image.imageDescriptor.isInteraced();

            // has local color table or not
            if (image.imageDescriptor.hasLocalColorTable()) {
                image.sizeOfColorTable = image.imageDescriptor.getSizeOfColorTable();
                image.localColorTable = readColorTable(dis, image.sizeOfColorTable);
            } else {
                image.sizeOfColorTable = sizeOfGlobalColorTable;
                image.localColorTable = globalColorTable;
            }

            int bytesParLine = image.imageDescriptor.width;
            int dibColorDepth = -1;
            // Coerce color bit depth to what the DIB can handle (oversampling)
            if (image.sizeOfColorTable == 0) {
                bytesParLine = (((image.imageDescriptor.width + 7) >> 3) + 3) & ~3;
                dibColorDepth = 1;
            } else if (image.sizeOfColorTable < 4) {
                bytesParLine = (((image.imageDescriptor.width + 1) >> 1) + 3) & ~3;
                dibColorDepth = 4;
            } else if (image.sizeOfColorTable < 8) {
                bytesParLine = (image.imageDescriptor.width + 3) & ~3;
                dibColorDepth = 8;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int lzwMinimumCodeSize = dis.readUnsignedByte();
            baos.write(lzwMinimumCodeSize);
            while (true) {
                int subBlockSize = dis.readUnsignedByte();
                if (subBlockSize == 0) {
                    break;
                }
                baos.write(subBlockSize);
                byte[] subBlockData = new byte[subBlockSize];
                dis.readFully(subBlockData, 0, subBlockSize);
                baos.write(subBlockData);
            }
            byte[] data = baos.toByteArray();

            // GIF Extraction
            image.tableBasedImageData = decoder.decode(data,
                                                       image.imageDescriptor.width,
                                                       image.imageDescriptor.height,
                                                       bytesParLine,
                                                       interlaced,
                                                       dibColorDepth,
                                                       data.length);

            return image;
        }
    }

    /**
     * @return width
     */
    public int getWidth(int index) {
        return images.get(index).imageDescriptor.width;
//        return header.logicalScreenWidth; // TODO
    }

    /**
     * @return height
     */
    public int getHeight(int index) {
        return images.get(index).imageDescriptor.height;
//        return header.logicalScreenHeight; // TODO
    }

    /** Creates a palette. */
    public ColorModel getColorModel(int index) {

        InternalImage image = images.get(index);

        int bits = image.sizeOfColorTable + 1;
        int usedColor = (int) Math.pow(2, bits);

        byte[] reds = new byte[usedColor];
        byte[] greens = new byte[usedColor];
        byte[] blues = new byte[usedColor];

        for (int i = 0; i < usedColor; i++) {
            blues[i] = image.localColorTable[i].blue;
            greens[i] = image.localColorTable[i].green;
            reds[i] = image.localColorTable[i].red;
//logger.log(Level.TRACE, "palette(" + i + "): " + StringUtil.paramString(palette[i]));
        }

        GraphicControlExtension graphicControlExtention = getGraphicControlExtension(index);
        if (graphicControlExtention != null && graphicControlExtention.hasTransparentColor()) {
            return new IndexColorModel(bits, usedColor, reds, greens, blues, graphicControlExtention.transparentColorIndex);
        } else {
            return new IndexColorModel(bits, usedColor, reds, greens, blues);
        }
    }

    /**
     * @return pixels
     */
    public byte[] getPixels(int index) {
//logger.log(Level.TRACE, "image[" + index + "]: " + images.get(index).tableBasedImageData.length);
        return images.get(index).tableBasedImageData;
    }

    static class ExtensionBlock {
        // 0xf9: Graphic Control Label
        // 0xfe: Comment Label
        // 0x01: Plain Text Label
        // 0xff: Application Extension Label
        //   byte[] applicationIdentifier = new byte[8];
        //   byte[] applicationAuthenticationCode = new byte[3];
        int extentionType;
        final List<byte[]> subBlocks = new ArrayList<>();
        static ExtensionBlock readFrom(LittleEndianDataInputStream dis) throws IOException {
            ExtensionBlock block = new ExtensionBlock();
            block.extentionType = dis.readUnsignedByte();
            while (true) {
                int subBlockSize = dis.readUnsignedByte();
                if (subBlockSize == 0) {
                    break;
                }
                byte[] subBlockData = new byte[subBlockSize];
                dis.readFully(subBlockData, 0, subBlockSize);
                block.subBlocks.add(subBlockData);
            }
            return block;
        }
        public String toString() {
            return String.format("21: extentionType: %02x", extentionType);
        }
    }

    /** */
    private GifHeader header;

    /** Global Color Table */
    private GifRGB[] globalColorTable;

    /** */
    private final List<InternalImage> images = new ArrayList<>();

    /** */
    private final List<Map<Integer, ExtensionBlock>> extentionBlocks = new ArrayList<>();

    public ImageDescriptor getImageDescriptor(int index) {
        return images.get(index).imageDescriptor;
    }

    /** */
    public int getNumImages() {
        return images.size();
    }

    /** */
    public static GifImage readFrom(InputStream is) throws IOException {

        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        GifImage gifImage = new GifImage();
        // Get global screen description information.
        gifImage.header = GifHeader.readFrom(dis);
        // Original GIF color bit depth (8 bit or less)
        int sizeOfGlobalColorTable = gifImage.header.getSizeOfGlobalColorTable();

        // Global color map processing
        if (gifImage.header.hasGlobalColorTable()) {
            gifImage.globalColorTable = readColorTable(dis, sizeOfGlobalColorTable);
        }

        while (true) {
            int blockType = dis.read();
            if (blockType == 0x2c) { // Image Separator
                InternalImage image = InternalImage.readFrom(dis, sizeOfGlobalColorTable, gifImage.globalColorTable);
                gifImage.images.add(image);
//logger.log(Level.TRACE, "imageBlock: " + image);
            } else if (blockType == 0x21) { // extention
                // GIF extension block
                ExtensionBlock extensionBlock = ExtensionBlock.readFrom(dis);
//logger.log(Level.TRACE, "extensionBlock: " + extensionBlock);
                if (gifImage.extentionBlocks.size() == gifImage.images.size()) {
                    Map<Integer, ExtensionBlock> extensionBlocks = new HashMap<>();
                    extensionBlocks.put(extensionBlock.extentionType, extensionBlock);
                    gifImage.extentionBlocks.add(extensionBlocks);
                } else {
                    gifImage.extentionBlocks.get(gifImage.images.size()).put(extensionBlock.extentionType, extensionBlock);
                }
            } else if (blockType == 0x3b) {
//logger.log(Level.TRACE, "3b: Trailer");
                break;
            } else if (blockType == -1) {
logger.log(Level.DEBUG, "unexpected eof");
                break;
            } else {
logger.log(Level.DEBUG, String.format("%02x: unknown block type", blockType));
            }
        }

        if (gifImage.images.isEmpty()) {
            throw new IllegalStateException("no image");
        }

        return gifImage;
    }

    /** Creates a monochrome bitmap. */
    public byte[] loadMonoColor(int index) {

        int width = getWidth(index);
        int height = getHeight(index);
        byte[] buffer = getPixels(index);

        byte[] vram = new byte[width * height];

        int count = 0;
        int skip = ((width + 7) / 8) % 4 != 0 ? 4 - ((width + 7) / 8) % 4 : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            int d = 0;
            for (int i = 0; i < width / 8; i++) {
                byte b = buffer[count++];
                int mask = 0x80;
                for (int k = 0; k < 8; k++) {
                    vram[ofs + d++] = (byte) ((b & mask) >> (7 - k));
                    mask >>= 1;
                }
            }
            if (width % 8 != 0) {
                byte b = buffer[count++];
                int mask = 0x80;
                for (int k = 0; k < width % 8; k++) {
                    vram[ofs + d++] = (byte) ((b & mask) >> (7 - k));
                    mask >>= 1;
                }
            }
            count += skip;
        }

        return vram;
    }

    /** Creates a 16-color bitmap. */
    public byte[] load16Color(int index) {

        int width = getWidth(index);
        int height = getHeight(index);
        byte[] buffer = getPixels(index);

        byte[] vram = new byte[width * height];

        int count = 0;
        int skip = ((width + 1) / 2) % 4 != 0 ? 4 - ((width + 1) / 2) % 4 : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            int d = 0;
            for (int i = 0; i < width / 2; i++) {
                int b = buffer[count++];
                vram[ofs + d++] = (byte) ((b & 0xf0) >> 4);
                vram[ofs + d++] = (byte) (b & 0x0f);
            }
            if (width % 2 != 0) {
                int b = buffer[count++];
                vram[ofs + d] = (byte) ((b & 0xf0) >> 4);
            }
            count += skip;
        }

        return vram;
    }

    /** Creates a 256 color bitmap. */
    public byte[] load256Color(int index) {

        int width = getWidth(index);
        int height = getHeight(index);
        byte[] buffer = getPixels(index);

        byte[] vram = new byte[width * height];
//logger.log(Level.TRACE, width + ", " + height + ", " + width * height + ", " + buffer.length);

        int count = 0;
        int skip = (width % 4 != 0) ? 4 - width % 4 : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            for (int i = 0; i < width; i++) {
                vram[ofs + i] = buffer[count++];
            }
            count += skip;
        }

        return vram;
    }
}
