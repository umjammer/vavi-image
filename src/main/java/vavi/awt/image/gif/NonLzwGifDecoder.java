/*
 * https://web.archive.org/web/20161106215528/http://homepage1.nifty.com/uchi/software.htm
 */

package vavi.awt.image.gif;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Non LZW theory GIF decoder.
 *
 * @author DJ.Uchi [H.Uchida]
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 2.00 040913 nsano port to java <br>
 */
public class NonLzwGifDecoder {

    private static final Logger logger = getLogger(NonLzwGifDecoder.class.getName());

    /**
     * DIB structure
     */
    static class RgbContext {

        /** Expanded data write position (offset value on the same line) */
        int xPoint;
        /** Expanded data write offset (number of lines * alignment) */
        int offset;
        /** horizontal pixel count */
        int width;
        /** Vertical pixel count */
        int height;
        /** Alignment value (number of bytes per line) */
        int bytesPerLine;
        /** Color Bit Value */
        int colorDepth;
        /** Expanded data writing line (used when interlacing) */
        int currentLine;
        /** Interlace offset (number of lines) */
        int interlaceOffset;
        /** Interlace flag: true for interlacing */
        boolean interlaced;
    }

    /**
     * GIF data parsing structure.
     * A collection of information necessary to obtain the GIF encoding code.
     */
    static class GifContext {

        /** RgbDecodeStatus Structure reference pointer */
        RgbContext rgb;
        /** Code Size(CS) */
        int codeSize;
        /** Bit Size(CBL) */
        int bitSize;
        /** Clear Code */
        int clearCode;
        /** End Code */
        int endCode;
        /** Current number of entries */
        int entry;
        /** Current bit position */
        int bitPoint;
        /** Next block start position */
        int nextBlock;
        /** Data size */
        int dataSize;
    }

    /**
     * Extracts the GIF image (only the first one).
     * Input is GIF data, output is RGB.
     * This mainly involves pre-processing for decoding.
     *
     * @return new allocated buffer (height * bytesPerLine)
     */
    public byte[] decode(byte[] data,
                         int width,
                         int height,
                         int bytesPerLine,
                         boolean interlaced,
                         int colorDepth,
                         int size) {

        byte[] vram = new byte[height * bytesPerLine];
        int[] lzw = new int[8192]; // Array for storing compressed data (not a dictionary table!)
        int code; // Encoding code initialization

        // DIB Data Write Structure Initialization
        RgbContext rgb = new RgbContext();
        rgb.xPoint = 0; // Expand data write position initialization
        rgb.offset = (height - 1) * bytesPerLine; // Write position offset
        rgb.width = width; // Get horizontal pixel count
        rgb.height = height; // Get vertical pixel count
        rgb.bytesPerLine = bytesPerLine; // Get alignment value
        rgb.colorDepth = colorDepth; // Get color bit value
        rgb.currentLine = 0; // Expand data write line initialization
        rgb.interlaceOffset = 8; // Interlace offset initialization
        rgb.interlaced = interlaced; // Get Interlace Flag
//logger.log(Level.TRACE, StringUtil.paramString(rgb));

        // Initialize structure for GIF data analysis
        GifContext gif = new GifContext();
        gif.rgb = rgb; // Obtaining a pointer to a RgbDecodeStatus structure
        gif.codeSize = data[0] & 0xff; // Get Code Size
        gif.bitSize = gif.codeSize + 1; // CBL initialization (code size + 1)
        gif.clearCode = 1 << gif.codeSize; // Clear Code Setting (2 ^ Code Size)
        gif.endCode = gif.clearCode + 1; // End code setting (2 ^ code size + 1)
        gif.entry = gif.endCode + 1; // Entry count initialization (2 ^ code size + 2)
        gif.bitPoint = 8; // Bit pointer initialization (8 bit = 1 byte)
        gif.nextBlock = 1; // Next block position initialization (first block)
        gif.dataSize = size; // Get GIF data size
//logger.log(Level.TRACE, StringUtil.paramString(gif));

        int times = 0; // Initialize deployment count
        int offset = 0; // Initialize deployment start position

        // Loop until the end code appears
        while ((code = getCode(data, gif)) != gif.endCode) {
            // If a clear code appears
//logger.log(Level.TRACE, "code: " + code);
            if (code == gif.clearCode) {
                // Number of entries & CBL initialization
                gif.entry = gif.endCode + 1;
                gif.bitSize = gif.codeSize + 1;

                // Call the expansion function.
                decodeLzw(vram, lzw, gif, times, offset);
                times = 0;
                offset = 0;
            } else {
                // The obtained encoded codes are stored in an int sized array.
                // This is to make the extraction process smoother, and is different from
                // a dictionary table. It is also possible to extract the obtained encoded
                // codes directly without storing them in an array, but this would be
                // problematic in terms of speed, so this method is used.
                lzw[times++] = code & 0xffff;

                // If no clear code appears for a long time, the array will overflow,
                // so the extraction process will be performed once 8192 encoding codes have been accumulated.
                // Is this a measure against uncompressed GIFs?
                if (times == 8192) {
                    decodeLzw(vram, lzw, gif, times, offset);
                    times = 4096;
                    offset = 4096;
                }
                if (gif.entry < 4095) {
                    gif.entry++;
                }
            }
        }

        // Extract the remaining encoded codes from the array.
        decodeLzw(vram, lzw, gif, times, offset);

        return vram;
    }

    /**
     * Variable bit-length input function.
     * One encoded code is taken and the bit position is incremented.
     */
    private static int getCode(byte[] data, GifContext gif) {
        int code = 0; // Encoding code initialization
        int bytePoint = gif.bitPoint >> 3; // Get the read position (in bytes)
//logger.log(Level.TRACE, "pt: " + bytePoint);

        // In case of size overflow, force return of end code (to prevent corrupted files)
        if ((bytePoint + 2) > gif.dataSize) {
            logger.log(Level.WARNING, "maybe broken");
            return gif.endCode;
        }

        // Get encoding code
        int i = 0; // Read byte count initialization
        while ((((gif.bitPoint + gif.bitSize) - 1) >> 3) >= bytePoint) {
            // If a block ends during a read
            if (bytePoint == gif.nextBlock) {
                gif.nextBlock += ((data[bytePoint++] & 0xff) + 1); // Update next block position
                gif.bitPoint += 8; // Add one byte to the bit pointer
            }
            code += ((data[bytePoint++] & 0xff) << i); // Get the code
            i += 8;
        }

        // The extra bits of the resulting code are then cut off (masking process).
        code = (code >> (gif.bitPoint & 0x07)) & ((1 << gif.bitSize) - 1);

        // Bit Pointer Update
        gif.bitPoint += gif.bitSize;

        // Check if CBL needs to be incremented.
        if (gif.entry > ((1 << gif.bitSize) - 1)) {
            gif.bitSize++;
        }

        return code;
    }

    /**
     * Non-LZW theory expansion function (main loop)
     * Decodes an encoding code stored in an int sized array.
     */
    private void decodeLzw(byte[] vram, int[] lzw, GifContext gif, int times, int offset) {
        // It just runs a loop and calls the expansion function.
//logger.log(Level.TRACE, " times: " + times + ", offset: " + offset);
        for (int i = offset; i < times; i++) {
            getLzwBytes(vram, lzw, gif, i);
        }
    }

    /**
     * Non-LZW theory expansion functions (core)
     * The core of non-LZW theory.
     * Returns the decompressed data for the specified encoding.
     */
    private void getLzwBytes(byte[] vram, int[] lzw, GifContext gif, int offset) {
        // Extracts one encoding code from the array.
        int code = lzw[offset];

        if (code < gif.clearCode) {
            // If the encoding code is smaller than the "number of colors",
            // the encoding code is written to RGB as is.
            writeRgb(vram, gif, code);

        } else if (code > gif.endCode + offset--) {
            // If the encoding code is unknown

            // Previous expansion data
            getLzwBytes(vram, lzw, gif, offset);

            // The first piece of the previous expanded data
            getLzwByte(vram, lzw, gif, offset);

        } else {
            // If the encoding code is greater than "number of colors + 1"
            // By the way, end codes and clear codes will never appear in the code entering this function,
            // so processing in those cases is not taken into consideration.

            // (Encoding code - number of colors + 1)
            getLzwBytes(vram, lzw, gif, code - gif.endCode - 1);

            // The first piece of (encoding code - number of colors + 2) expanded data
            getLzwByte(vram, lzw, gif, code - gif.endCode);
        }
    }

    /**
     * Non-LZW theory expansion functions (sub)
     * The core of non-LZW theory.
     * Returns the first piece of decompressed data for the specified encoding.
     */
    private static void getLzwByte(byte[] vram, int[] lzw, GifContext gif, int offset) {
        int code;

        // Take one encoding code from the array and check if it is smaller than "number of colors".
        while ((code = lzw[offset]) >= gif.clearCode) {
            // If the encoding code is unknown
            if (code > gif.endCode + offset) {
                // The first piece of the previous expanded data
                offset--;

                // If the encoding code is greater than "number of colors + 1"
            } else {
                // The first piece of (encoding code - number of colors + 1) expanded data
                offset = code - gif.endCode - 1;
            }
        }

        // The resulting expanded data is written to the DIB.
        writeRgb(vram, gif, code);
    }

    /**
     * RGB image data writing function.
     * Write the extracted image data as RGB.
     */
    private static void writeRgb(byte[] rgb, GifContext gif, int code) {
        // Write image data to RGB.
        // In the case of monochrome or 16 colors, writing is done in bit units, so masking processing is performed.
        int i;

        // Write image data to RGB.
        // In the case of monochrome or 16 colors, writing is done in bit units, so masking processing is performed.
        int j;
//logger.log(Level.TRACE, "gif.rgb.color: " + gif.rgb.colors);
//logger.log(Level.TRACE, "rgb.offset: " + gif.rgb.offset + ", rgb.rgb.point: " + gif.rgb.xPoint + ", code: " + code);
//logger.log(Level.TRACE, String.format("%d", code));
        switch (gif.rgb.colorDepth) {
            case 1: // For monochrome images
                i = gif.rgb.offset + (gif.rgb.xPoint / 8);
                j = 7 - (gif.rgb.xPoint & 0x07);
                rgb[i] = (byte) ((rgb[i] & ~(1 << j)) | (code << j));
//logger.log(Level.TRACE, String.format("x: %d, y: %d / w: %d, h: %d: %02x", gif.rgb.xPoint, (gif.rgb.offset / gif.rgb.bytesPerLine), gif.rgb.width, gif.rgb.height, rgb[i]));
                break;
            case 4: // For a 16-color image
                i = gif.rgb.offset + (gif.rgb.xPoint >> 1);
                j = (gif.rgb.xPoint & 0x01) << 2;
                rgb[i] = (byte) ((rgb[i] & (0x0f << j)) | (code << (4 - j)));
                break;
            default: // 256 colors
                rgb[gif.rgb.offset + gif.rgb.xPoint] = (byte) code;
                break;
        }

        // Increment write position
        gif.rgb.xPoint++;

        // If the write position reaches the end of the line
        if (gif.rgb.xPoint == gif.rgb.width) {
//logger.log(Level.TRACE, "y: " + (gif.rgb.offset / gif.rgb.bytesPerLine));
            if (gif.rgb.interlaced) { // For interlaced GIF

                // When the interlace lines reach the bottom of the screen
                if ((gif.rgb.currentLine + gif.rgb.interlaceOffset) >= gif.rgb.height) {
                    if ((gif.rgb.currentLine & 0x07) == 0) {
                        gif.rgb.offset = (gif.rgb.height - 5) * gif.rgb.bytesPerLine;
                        gif.rgb.currentLine = 4;
                    } else if (gif.rgb.interlaceOffset == 8) {
                        gif.rgb.offset = (gif.rgb.height - 3) * gif.rgb.bytesPerLine;
                        gif.rgb.currentLine = 2;
                        gif.rgb.interlaceOffset = 4;
                    } else if (gif.rgb.interlaceOffset == 4) {
                        gif.rgb.offset = (gif.rgb.height - 2) * gif.rgb.bytesPerLine;
                        gif.rgb.currentLine = 1;
                        gif.rgb.interlaceOffset = 2;
                    }
                } else {
                    gif.rgb.offset -= gif.rgb.bytesPerLine * gif.rgb.interlaceOffset;
                    gif.rgb.currentLine += gif.rgb.interlaceOffset;
                }
            } else { // For Linear GIF
                gif.rgb.offset -= gif.rgb.bytesPerLine;
            }
            gif.rgb.xPoint = 0;
        }
    }
}
