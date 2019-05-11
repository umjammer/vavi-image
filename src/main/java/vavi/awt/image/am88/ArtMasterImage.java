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


/**
 * ArtMasterImage.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/10 umjammer initial version <br>
 */
public class ArtMasterImage {

    /** PC9801 の Digital Color Model */
    private static final ColorModel defaultCm = new IndexColorModel(8, 8, new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff
    }, new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
    }, new byte[] {
        (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff
    });

    /** */
    public ColorModel cm = defaultCm;

    /** 幅 */
    public static final int W = 640;

    /** 高さ */
    public static final int H = 200;

    /** Red バッファ */
    private byte[] R = new byte[(W / 8) * H + 256];

    /** Blue バッファ */
    private byte[] G = new byte[(W / 8) * H + 256];

    /** Green バッファ */
    private byte[] B = new byte[(W / 8) * H + 256];

    /**
     * ArtMaster88 形式のイメージを作成します．
     * @throws IllegalArgumentException when header is wrong
     */
    public ArtMasterImage(InputStream in) throws IOException {

        byte[] buf = new byte[40];
        final byte[] header = {
            'S', 'S', '_', 'S', 'I', 'F', ' ', ' ', ' ', ' ', '0', '.', '0', '0', 0x1a, 0
        };

        int l = 0;
        while (l < 40) {
            l += in.read(buf, l, 40 - l);
//Debug.println(l);
        }

//Debug.dump(buf);
        for (int i = 0; i < 16; i++) {
            if (buf[i] != header[i]) {
                throw new IllegalArgumentException("wrong header");
            }
        }

        l = 0;
        if (buf[16] == 'I') {
            while (l < 756) {
                l += (int) in.skip(756 - l);
//Debug.println(l);
            }
        }
        l = 0;
        if (buf[18] == 'B') {
            while (l < 2292) {
                l += (int) in.skip(2292 - l);
//Debug.println(l);
            }
        }

        PushbackInputStream pin = new PushbackInputStream(in, 2);

        for (int i = 0; i < 3; i++) {

            byte g[] = null;
            int count = 0;

            switch (buf[19 + i]) {
            case 'R':
                g = R;
                break;
            case 'G':
                g = G;
                break;
            case 'B':
                g = B;
                break;
            default:
                throw new IOException("wrong color table");
            }

            while (count < (W / 8) * H) {

                int p1 = pin.read();
                int p2 = pin.read();
                int bl = pin.read();
//Debug.println("p1: " + Debug.toHex2(p1) + ", " +
// "p2: " + Debug.toHex2(p2) + ", " +
// "bl: " + Debug.toHex2(bl) + "(" + bl + ")");

                if (p1 == p2) {
                    for (int j = 0; j < (bl == 0 ? 256 : bl); j++) {
                        g[count++] = (byte) p1;
                    }
//Debug.println("m: " + Debug.toHex2(g[count-1]));
                } else {
                    g[count++] = (byte) p1;
                    pin.unread(bl);
                    pin.unread(p2);
//Debug.println("s: " + Debug.toHex2(g[count-1]));
                }
//Debug.println("g: " + Debug.toHex2(g[count-1]));
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

/* */
