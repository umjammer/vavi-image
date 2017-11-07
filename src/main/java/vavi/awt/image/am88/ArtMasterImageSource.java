/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.am88;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Hashtable;


/**
 * アートマスター形式のイメージを作成します．
 *
 * TODO palette control
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano refine access mode, messages <br>
 *          1.01 010903 nsano fix read bug <br>
 *          1.02 020413 nsano optimize color model <br>
 */
public class ArtMasterImageSource implements ImageProducer {

    /** イメージのバッファ */
    private byte[] vram;

    /** PC9801 の Digital Color Model */
    private static final ColorModel defaultCm = new IndexColorModel(8, 8, new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff
    }, new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
    }, new byte[] {
        (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff
    });

    /** */
    private ColorModel cm = defaultCm;

    /** 幅 */
    private static final int W = 640;

    /** 高さ */
    private static final int H = 200;

    /** Red バッファ */
    private byte[] R = new byte[(W / 8) * H + 256];

    /** Blue バッファ */
    private byte[] G = new byte[(W / 8) * H + 256];

    /** Green バッファ */
    private byte[] B = new byte[(W / 8) * H + 256];

    /** */
    private ImageConsumer ic;

    /** @see ImageProducer */
    public synchronized void addConsumer(ImageConsumer ic) {
        this.ic = ic;
        if (this.ic != null)
            loadPixel();
        this.ic = null;
    }

    /** @see ImageProducer */
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
    }

    /** @see ImageProducer */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return ic == this.ic;
    }

    /** @see ImageProducer */
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (this.ic == ic)
            this.ic = null;
    }

    /** @see ImageProducer */
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    /**
     * ArtMaster88 形式のイメージを作成します．
     * @throws IllegalArgumentException when header is wrong
     */
    public ArtMasterImageSource(InputStream in) throws IOException {

        byte[] buf = new byte[40];
        final byte[] header = {
            'S', 'S', '_', 'S', 'I', 'F', ' ', ' ', ' ', ' ', '0', '.', '0', '0', 0x1a, 0
        };

        int l = 0;
        while (l < 40) {
            l += in.read(buf, l, 40 - l);
// Debug.println(l);
        }

        for (int i = 0; i < 16; i++) {
            if (buf[i] != header[i]) {
                throw new IllegalArgumentException("wrong header");
            }
        }

        l = 0;
        if (buf[16] == 'I') {
            while (l < 756) {
                l += (int) in.skip(756 - l);
// Debug.println(l);
            }
        }
        l = 0;
        if (buf[18] == 'B') {
            while (l < 2292) {
                l += (int) in.skip(2292 - l);
// Debug.println(l);
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
// Debug.println("p1: " + Debug.toHex2(p1) + ", " +
// "p2: " + Debug.toHex2(p2) + ", " +
// "bl: " + Debug.toHex2(bl) + "(" + bl + ")");

                if (p1 == p2) {
                    for (int j = 0; j < (bl == 0 ? 256 : bl); j++) {
                        g[count++] = (byte) p1;
                    }
// Debug.println("m: " + Debug.toHex2(g[count-1]));
                } else {
                    g[count++] = (byte) p1;
                    pin.unread(bl);
                    pin.unread(p2);
// Debug.println("s: " + Debug.toHex2(g[count-1]));
                }
// Debug.println("g: " + Debug.toHex2(g[count-1]));
            }
        }
    }

    /**
     * ピクセルをロードします．
     */
    private void loadPixel() {

        ic.setDimensions(W, H);
        ic.setProperties(new Hashtable<>());
        ic.setColorModel(cm);

        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        vram = new byte[W * H];

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int mask = 0x80 >> (x % 8);
                int pos = y * (W / 8) + (x / 8);
                int r = ((R[pos] & mask) != 0) ? 2 : 0;
                int g = ((G[pos] & mask) != 0) ? 4 : 0;
                int b = ((B[pos] & mask) != 0) ? 1 : 0;
                vram[y * W + x] = (byte) (r + g + b);
            }
        }

        ic.setPixels(0, 0, W, H, cm, vram, 0, W);

        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
}

/* */
