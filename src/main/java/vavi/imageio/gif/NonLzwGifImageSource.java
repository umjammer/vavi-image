/*
 * Copyright (c) 2004  by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;


/**
 * NonLzwGifImageSource．
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 040913 nsano initail version <br>
 */
public class NonLzwGifImageSource implements ImageProducer {

    /** GIF Image */
    private GifImage gifImage;

    /** @see ImageConsumer */
    private ImageConsumer ic;

    /** @see ImageProducer */
    public synchronized void addConsumer(ImageConsumer ic) {
        this.ic = ic;
        if (this.ic != null) {
            loadPixel();
        }
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
        if (this.ic == ic) {
            this.ic = null;
        }
    }

    /** @see ImageProducer */
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    /** ビットマップを作成します． */
    public NonLzwGifImageSource(InputStream in) throws IOException {
        gifImage = GifImage.readFrom(in);
    }

    /** */
    public GifImage getGifImage() {
        return gifImage;
    }

    /** モノカラービットマップを作成します． */
    private byte[] loadMonoColor() {

        int width = gifImage.getWidth();
        int height = gifImage.getHeight();
        byte[] buffer = gifImage.getPixels();

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

    /** 16 色ビットマップを作成します． */
    private byte[] load16Color() {

        int width = gifImage.getWidth();
        int height = gifImage.getHeight();
        byte[] buffer = gifImage.getPixels();

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

    /** 256 色ビットマップを作成します． */
    private byte[] load256Color() {

        int width = gifImage.getWidth();
        int height = gifImage.getHeight();
        byte[] buffer = gifImage.getPixels();

        byte[] vram = new byte[width * height];
//Debug.println(width + ", " + height + ", " + width * height + ", " + buffer.length);

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

    /** ビットマップを作成します． */
    private void loadPixel() {

        ColorModel cm = gifImage.getColorModel();

        int width = gifImage.getWidth();
        int height = gifImage.getHeight();

        ic.setDimensions(width, height);
        ic.setProperties(new Hashtable<>());
        ic.setColorModel(cm);

        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        byte[] vram;
        switch (cm.getPixelSize()) {
        case 1:
            vram = loadMonoColor();
            break;
        case 2:
        case 3:
        case 4:
            vram = load16Color();
            break;
        default:
        case 8:
            vram = load256Color();
            break;
        }

        ic.setPixels(0, 0, width, height, cm, vram, 0, width);

        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
}

/* */
