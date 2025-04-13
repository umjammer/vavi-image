/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.am88;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import vavi.util.StringUtil;

import static java.lang.System.getLogger;


/**
 * ArtMasterImage.
 * <p>
 * magic SS_SIF
 * separated RGB RLE
 * </p>
 * TODO 400 line
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/10 umjammer initial version <br>
 */
public class ArtMasterImage {

    private static final Logger logger = getLogger(ArtMasterImage.class.getName());

    /** Digital Color Model for PC9801 */
    private static final ColorModel defaultCm = new IndexColorModel(8, 8, new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff
    }, new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
    }, new byte[] {
        (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff
    });

    /** */
    public final ColorModel cm = defaultCm;

    /** width */
    private static final int W = 640;

    /** height */
    private int H = 200;

    /** Red buffer */
    private final byte[] R;

    /** Blue buffer */
    private final byte[] G;

    /** Green buffer */
    private final byte[] B;

    private static final byte[] header = {
        'S', 'S', '_', 'S', 'I', 'F', ' ', ' ', ' ', ' ', '#', '.', '#', '#', 0x1a, 0
    };

    /** */
    public int getWidth() {
        return W;
    }

    /** */
    public int getHeight() {
        return H;
    }

    /** */
    public ColorModel getColorModel() {
        return defaultCm;
    }

    /**
     * Creates ArtMaster88 image. 
     * @throws IllegalArgumentException when header is wrong
     */
    public ArtMasterImage(InputStream in) throws IOException {

        byte[] buf = new byte[40];

        int l = 0;
        while (l < 40) {
            l += in.read(buf, l, 40 - l);
        }

        for (int i = 0; i < 16; i++) {
            if (header[i] == '#') {
                if (!Character.isDigit(buf[i])) {
                    throw new IllegalArgumentException("wrong version: " + StringUtil.getDump(buf, 16));
                }
            } else if (buf[i] != header[i]) {
                throw new IllegalArgumentException("wrong header: " + StringUtil.getDump(buf, 16));
            }
        }
logger.log(Level.DEBUG, "version: " + (char) buf[10] + "." + (char) buf[12] + (char) buf[13]);

        l = 0;
        if (buf[16] == 'I') {
            while (l < 756) {
                l += (int) in.skip(756 - l);
            }
logger.log(Level.DEBUG, "skip I: " + l);
        }
        l = 0;
        if (buf[18] == 'B') {
            while (l < 2292) {
                l += (int) in.skip(2292 - l);
            }
logger.log(Level.DEBUG, "skip B: " + l);
        }

        H = buf[26] & 0xff | (buf[27] & 0xff) << 8;
        if (H == 0) H = 200;
        if (H == 400) {
            // TODO use recoil currently
            throw new IllegalArgumentException("wrong height: 400 lines mode is not supported");
        }
logger.log(Level.DEBUG, "height: " + H);

        R = new byte[(W / 8) * H + 256];
        G = new byte[(W / 8) * H + 256];
        B = new byte[(W / 8) * H + 256];

        PushbackInputStream pin = new PushbackInputStream(in, 2);

        for (int i = 0; i < 3; i++) {

            byte[] g;
            int count = 0;

            g = switch (buf[19 + i]) {
                case 'R' -> R;
                case 'G' -> G;
                case 'B' -> B;
                default -> throw new IOException("wrong color table");
            };

            while (count < (W / 8) * H) {

                int p1 = pin.read();
                int p2 = pin.read();
                int bl = pin.read();
//logger.log(Level.TRACE, String.format("p1: %02x, p2: %02x, bl: %3$02x(%3$d)", p1, p2, bl));

                if (p1 == p2) {
                    for (int j = 0; j < (bl == 0 ? 256 : bl); j++) {
                        g[count++] = (byte) p1;
                    }
//logger.log(Level.TRACE, String.format("m: %02x", g[count-1]));
                } else {
                    g[count++] = (byte) p1;
                    pin.unread(bl);
                    pin.unread(p2);
//logger.log(Level.TRACE, String.format("s: %02x", g[count-1]));
                }
//logger.log(Level.TRACE, String.format("g: %02x", g[count-1]));
            }
        }
    }

    /** */
    public byte[] getPixels() {
        byte[] result = new byte[W * H];

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int mask = 0x80 >> (x % 8);
                int pos = y * (W / 8) + (x / 8);
                int r = ((R[pos] & mask) != 0) ? 2 : 0;
                int g = ((G[pos] & mask) != 0) ? 4 : 0;
                int b = ((B[pos] & mask) != 0) ? 1 : 0;
                result[y * W + x] = (byte) (r + g + b);
            }
        }

        return result;
    }
}
