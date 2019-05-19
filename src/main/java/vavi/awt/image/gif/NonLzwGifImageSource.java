/*
 * Copyright (c) 2004  by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.gif;

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
            loadPixel(0); // TODO image index
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

    /** ビットマップを作成します． */
    private void loadPixel(int index) {

        ColorModel cm = gifImage.getColorModel(index);

        int width = gifImage.getWidth(index);
        int height = gifImage.getHeight(index);

        ic.setDimensions(width, height);
        ic.setProperties(new Hashtable<>());
        ic.setColorModel(cm);

        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        byte[] vram;
        switch (cm.getPixelSize()) {
        case 1:
            vram = gifImage.loadMonoColor(index);
            break;
        case 2:
        case 3:
        case 4:
            vram = gifImage.load16Color(index);
            break;
        default:
        case 8:
            vram = gifImage.load256Color(index);
            break;
        }

        ic.setPixels(0, 0, width, height, cm, vram, 0, width);

        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
}

/* */
