/*
 * https://github.com/shikarunochi/M5StackGraphicLoader/blob/master/MAGLoader/MAGLoader.ino
 */

package vavi.awt.image.mag;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;

import vavi.io.SeekableDataInput;

import static java.lang.System.getLogger;


/**
 * Mag.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-25 nsano initial version <br>
 * @see "https://emk.name/2015/03/magjs.html"
 * @see "https://mooncore.eu/bunny/txt/makichan.htm"
 */
public class Mag {

    private static final Logger logger = getLogger(Mag.class.getName());

    private Mag() {}

    /** */
    private static int pack(int r, int g, int b) {
        return 0xff << 24 | g << 16 | r << 8 | b;
    }

    /**
     * @param sdi little endian
     */
    public static BufferedImage load(SeekableDataInput<?> sdi) throws IOException {
        byte[] maki02 = new byte[8];
        sdi.readFully(maki02);
        if (!Arrays.equals(maki02, "MAKI02  ".getBytes())) {
            // not MAG image
            throw new IllegalArgumentException(" is Not MAG Format.");
        }

        // skip until the header
        int headerOffset = 30;
        sdi.position(headerOffset);
        while (0x1a != sdi.readByte()) ++headerOffset;

        headerOffset++;
logger.log(Level.DEBUG, "headerOffset: " + headerOffset);
        sdi.position(headerOffset);

        class MagInfo {
            // heading 32byte is fixed order.
            int top;
            int platform;
            int flags;
            int mode;
            int sx;
            int sy;
            int ex;
            int ey;
            int flagAOffset;
            int flagBOffset;
            int flagBSize;
            int pixelOffset;
            int pixelSize;

            int flagASize;
            int colors;
            int pixelUnitLog;
            int width;
            int height;
            int flagSize;

            void init() {
                flagASize = flagBOffset - flagAOffset;

                colors = (mode & 0x80) != 0 ? 256 : 16;
                pixelUnitLog = (mode & 0x80) != 0 ? 1 : 2;
                width = ((ex & 0xfff8) | 7) - (sx & 0xfff8) + 1;
                height = ey - sy + 1;
                flagSize = width >> (pixelUnitLog + 1);
            }

            @Override
            public String toString() {
                return "MagInfo{" +
                        "top=" + top +
                        ", platform=" + platform +
                        ", flags=" + flags +
                        ", mode=" + mode +
                        ", sx=" + sx +
                        ", sy=" + sy +
                        ", ex=" + ex +
                        ", ey=" + ey +
                        ", flagAOffset=" + flagAOffset +
                        ", flagBOffset=" + flagBOffset +
                        ", flagBSize=" + flagBSize +
                        ", pixelOffset=" + pixelOffset +
                        ", pixelSize=" + pixelSize +
                        ", flagASize=" + flagASize +
                        ", colors=" + colors +
                        ", pixelUnitLog=" + pixelUnitLog +
                        ", width=" + width +
                        ", height=" + height +
                        ", flagSize=" + flagSize +
                        '}';
            }
        }

        MagInfo mag = new MagInfo();

        mag.top = sdi.readUnsignedByte();
        mag.platform = sdi.readUnsignedByte();
        mag.flags = sdi.readUnsignedByte();
        mag.mode = sdi.readUnsignedByte();
        mag.sx = sdi.readUnsignedShort();
        mag.sy = sdi.readUnsignedShort();
        mag.ex = sdi.readUnsignedShort();
        mag.ey = sdi.readUnsignedShort();
        mag.flagAOffset = sdi.readInt();
        mag.flagBOffset = sdi.readInt();
        mag.flagBSize = sdi.readInt();
        mag.pixelOffset = sdi.readInt();
        mag.pixelSize = sdi.readInt();
        mag.init();
logger.log(Level.DEBUG, mag);

        // image extraction buffer for 16 lines
        byte[] data = new byte[mag.width * 16];

        // color palette extraction buffer
        // (r0,g0,b0),(r1,g1,b1),...
        byte[] palette = new byte[mag.colors * 3];
        sdi.readFully(palette, 0, mag.colors * 3);
logger.log(Level.DEBUG, String.format("palette: pos: %d, len: %d", headerOffset + 32, mag.colors * 3));

        byte[] flagABuf = new byte[mag.flagASize];
        sdi.position(headerOffset + mag.flagAOffset);
        sdi.readFully(flagABuf, 0, mag.flagASize);
logger.log(Level.DEBUG, String.format("flagA: pos: %d, len: %d", headerOffset + mag.flagAOffset, mag.flagASize));

        byte[] flagBBuf = new byte[mag.flagBSize];
        sdi.position(headerOffset + mag.flagBOffset);
        sdi.readFully(flagBBuf, 0, mag.flagBSize);
logger.log(Level.DEBUG, String.format("flagB: pos: %d, len: %d", headerOffset + mag.flagBOffset, mag.flagBSize));

        byte[] flagBuf = new byte[mag.flagSize];

        final int pixelBufSize = 4096;
        final int halfBufSize = pixelBufSize >> 1;
        byte[] pixel = new byte[pixelBufSize];
        sdi.position(headerOffset + mag.pixelOffset);

        int src = 0; // (headerOffset + mag.pixelOffset) % (pixelBufSize);
        sdi.readFully(pixel, src, pixelBufSize - src);
logger.log(Level.DEBUG, String.format("pixel: pos: %d, len: %d", headerOffset + mag.pixelOffset, pixelBufSize - src));

        int flagAPos = 0;
        int flagBPos = 0;
        int dest = 0;
        // for copy position calculation
        int[] copyX = {0, 1, 2, 4, 0, 1, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0};
        int[] copyY = {0, 0, 0, 0, 1, 1, 2, 2, 2, 4, 4, 4, 8, 8, 8, 16};
        int[] copyPos = new int[16];

        for (int i = 0; i < 16; ++i) {
            copyPos[i] = -(copyY[i] * mag.width + (copyX[i] << mag.pixelUnitLog));
        }

        int copySize = 1 << mag.pixelUnitLog;
        int mask = 0x80;

logger.log(Level.DEBUG, String.format("width: %d, height: %d", mag.width, mag.height));

        int destDiff = 0;

        BufferedImage image = new BufferedImage(mag.width, mag.height, BufferedImage.TYPE_INT_ARGB);
        int[] buf = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int y = 0; y < mag.height; ++y) {
            if (0 != dest && (0 == (y & 1))) {
                int dy = (y - 2) & 15;
                for (int x = 0; x < mag.width >> 1; x++) {
                    if ((x * 2 + 1) + (dy + 1) * mag.width > dest) break;
                    int c1 = (data[x * 2 + dy * mag.width] & 0xff) * 3;
                    int c2 = (data[(x * 2 + 1) + dy * mag.width] & 0xff) * 3;
                    int c3 = (data[x * 2 + (dy + 1) * mag.width] & 0xff) * 3;
                    int c4 = (data[(x * 2 + 1) + (dy + 1) * mag.width] & 0xff) * 3;
                    buf[x * 2 + y * mag.width] = pack(palette[c1] & 0xff, palette[c1 + 1] & 0xff, palette[c1 + 2] & 0xff);
                    buf[x * 2 + 1 + y * mag.width] = pack(palette[c2] & 0xff, palette[c2 + 1] & 0xff, palette[c2 + 2] & 0xff);
                    buf[x * 2 + (y + 1) * mag.width] = pack(palette[c3] & 0xff, palette[c3 + 1] & 0xff, palette[c3 + 2] & 0xff);
                    buf[x * 2 + 1 + (y + 1) * mag.width] = pack(palette[c4] & 0xff, palette[c4 + 1] & 0xff, palette[c4 + 2] & 0xff);
                }
                if ((y & 15) == 0) {
                    destDiff = dest;
                    dest = 0;
                }
            }

            // extract flags for 1 line
            int x = 0;
            int xEnd = mag.flagSize;
            do {
                // check flag A 1 bit
                if ((flagABuf[flagAPos] & mask) != 0) {
                    // if 1, xor 1 byte read from flag B
                    flagBuf[x] = (byte) (flagBuf[x] ^ flagBBuf[flagBPos++]);
                }
                if ((mask >>= 1) == 0) {
                    mask = 0x80;
                    ++flagAPos;
                }
            } while (++x < xEnd);

            x = 0;
            xEnd <<= 1;
            do {
                // check one flag
                int v = flagBuf[x >> 1] & 0xff;
                if ((x & 1) != 0) v &= 0x0F;
                else v >>= 4;

                if (v == 0) {
                    if (src == pixelBufSize) {
                        sdi.readFully(pixel, 0, pixelBufSize);
                        src = 0;
                    }
                    // if 0, read one pixel (2 byte)
                    if (mag.colors == 16) {
                        byte tmp = pixel[src]; // use temp value for optimization
                        data[dest + 0] = (byte) ((tmp >> 4) & 0xF);
                        data[dest + 1] = (byte) (tmp & 0xF);
                        tmp = pixel[src + 1];
                        data[dest + 2] = (byte) ((tmp >> 4) & 0xF);
                        data[dest + 3] = (byte) (tmp & 0xF);
                        dest += 4;
                        src += 2;
                    } else {
                        System.arraycopy(pixel, src, data, dest, 2);
                        dest += 2;
                        src += 2;
                    }
                } else {
                    // else 0, copy one pixel (16 color 4dot/256 color 2 dot) from specified position
                    int copySrc = dest + copyPos[v];
                    if (copySrc < 0) copySrc += destDiff;
                    System.arraycopy(data, copySrc, data, dest, copySize);
                    dest += copySize;
                }
            } while (++x < xEnd);
        }

        return image;
    }
}
