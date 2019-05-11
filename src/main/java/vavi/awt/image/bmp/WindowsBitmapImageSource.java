/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;


/**
 * ウインドウズビットマップを作成します．
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano refine <br>
 *          1.10 010901 nsano refine <br>
 *          1.11 021104 nsano add 32bit color <br>
 */
public class WindowsBitmapImageSource implements ImageProducer {

    /** WindowsBitmap */
    private WindowsBitmap bitmap;

    /** @see ImageConsumer */
    private ImageConsumer ic;

    /* */
    public synchronized void addConsumer(ImageConsumer ic) {
        this.ic = ic;
        if (this.ic != null) {
            loadPixel();
        }
        this.ic = null;
    }

    /* */
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
    }

    /* */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return ic == this.ic;
    }

    /* */
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (this.ic == ic) {
            this.ic = null;
        }
    }

    /* */
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    /** WindowsIconSource 用 */
    WindowsBitmapImageSource(WindowsBitmap bitmap) {
        this.bitmap = bitmap;
    }

    /** ビットマップを作成します． */
    public WindowsBitmapImageSource(InputStream in) throws IOException {
        bitmap = WindowsBitmap.readFrom(in);
    }

    /** */
    public WindowsBitmap getWindowsBitmap() {
        return bitmap;
    }

    /** ビットマップを作成します． */
    private void loadPixel() {

        ColorModel cm = bitmap.getColorModel();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bits = bitmap.getBits();
        int compression = bitmap.getCompression();

        ic.setDimensions(width, height);
        ic.setProperties(new Hashtable<>());
        ic.setColorModel(cm);

        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        // インデックスカラー用イメージバッファ
        byte[] vram = null;
        // フルカラー用イメージ用バッファ
        int[] ivram = null;

        switch (bits) {
        case 1:
            vram = bitmap.getMonoColorData();
            break;
        case 4:
            if (compression == WindowsBitmap.Type.RLE4.ordinal()) {
                vram = bitmap.get16ColorRleData();
            } else {
                vram = bitmap.get16ColorData();
            }
            break;
        case 8:
            if (compression == WindowsBitmap.Type.RLE8.ordinal()) {
                vram = bitmap.get256ColorRleData();
            } else {
                vram = bitmap.get256ColorData();
            }
            break;
        case 24:
            ivram = bitmap.get24BitColorData();
            break;
        case 32:
            ivram = bitmap.get32BitColorData();
            break;
        }

        if (bits == 24 || bits == 32) {
            ic.setPixels(0, 0, width, height, cm, ivram, 0, width);
        } else {
            ic.setPixels(0, 0, width, height, cm, vram, 0, width);
        }

        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
}

/* */
