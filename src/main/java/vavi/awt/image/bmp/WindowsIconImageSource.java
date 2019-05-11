/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * アイコンのイメージを作成します．
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano refine <br>
 *          1.01 020413 nsano independent of WindowsBitmapSource <br>
 *          1.02 021104 nsano don't touch iconDevice.colors <br>
 */
public class WindowsIconImageSource implements ImageProducer {

    /** */
    private WindowsBitmapImageSource bitmap;

    /** アイコンファイルには複数の大きさのアイコンが入っている */
    private WindowsIcon icons[];

    /** それぞれの大きさをデバイスと呼びそれを管理する数値 */
    private int deviceId = 0;

    /** */
    private BufferedImage image;

    /** @see ImageConsumer */
    private ImageConsumer ic;

    /** @see ImageProducer */
    public synchronized void addConsumer(ImageConsumer ic) {
        if (bitmap != null) {
            bitmap.addConsumer(ic);
        } else {
            this.ic = ic;
            if (this.ic != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                ColorModel cm = image.getColorModel();
                ic.setDimensions(width, height);
                ic.setProperties(new Hashtable<>());
                ic.setColorModel(cm);
//Debug.println(image.getType() + ", " + cm);
                ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);
                int[] buf = image.getRGB(0, 0, width, height, (int[]) null, 0, width);
//Debug.println(buf.length + ", " + width * height);
                ic.setPixels(0, 0, width, height, ColorModel.getRGBdefault(), buf, 0, width); // colorModel ???
                ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
            }
            this.ic = null;
        }
    }

    /** @see ImageProducer */
    public void startProduction(ImageConsumer ic) {
        if (bitmap != null) {
            bitmap.addConsumer(ic);
        } else {
            addConsumer(ic);
        }
    }

    /** @see ImageProducer */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        if (bitmap != null) {
            return bitmap.isConsumer(ic);
        } else {
            return ic == this.ic;
        }
    }

    /** @see ImageProducer */
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (bitmap != null) {
            bitmap.removeConsumer(ic);
        } else {
            if (this.ic == ic) {
                this.ic = null;
            }
        }
    }

    /** @see ImageProducer */
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    /** 現在のデバイス ID を返します． */
    public int getDeviceId() {
        return deviceId;
    }

    /** 何個デバイスがあるかを返します． */
    public int getDeviceCount() {
        return icons.length;
    }

    /**
     * デバイスを指定した形式に変更します．
     *
     * @throws NoSuchElementException 指定したデバイスがなかった場合
     */
    public void changeDevice(WindowsIconDevice device) {

        for (int i = 0; i < icons.length; i++) {
            if (icons[i].getDevice().getWidth() == device.getWidth() &&
                icons[i].getDevice().getHeight() == device.getHeight() &&
                icons[i].getDevice().getColors() == device.getColors()) {
                changeDevice(i);
            }
        }

        throw new NoSuchElementException(String.valueOf(device));
    }

    /** */
    private Map<String, WindowsBitmapImageSource> bitmapCache = new HashMap<>();

    /** デバイスを ID で指定します． */
    public void changeDevice(int id) {
        if (id >= 0 && id < icons.length) {
            deviceId = id;
            if (bitmapCache.containsKey(String.valueOf(id))) {
                bitmap = bitmapCache.get(String.valueOf(id));
//Debug.println("cache hit: " + id);
            } else {
                if (icons[id].getBitmap() == null) {
                    image = icons[id].getImage();
                } else {
                    bitmap = new WindowsBitmapImageSource(icons[id].getBitmap());
                    bitmapCache.put(String.valueOf(id), bitmap);
                }
//Debug.println("new: " + id);
            }
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(id));
        }
    }

    /** ストリームからアイコンのイメージを作成します． */
    public WindowsIconImageSource(InputStream in) throws IOException {

        icons = WindowsIcon.readFrom(in);

        changeDevice(0);
    }

    /** */
    public WindowsBitmap getWindowsBitmap() {
        return bitmap.getWindowsBitmap();
    }
}

/* */
