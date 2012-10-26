/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;



/**
 * アイコンのイメージを作成します．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
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

    /** @see ImageProducer */
    public synchronized void addConsumer(ImageConsumer ic) {
        bitmap.addConsumer(ic);
    }

    /** @see ImageProducer */
    public void startProduction(ImageConsumer ic) {
        bitmap.addConsumer(ic);
    }

    /** @see ImageProducer */
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return bitmap.isConsumer(ic);
    }

    /** @see ImageProducer */
    public synchronized void removeConsumer(ImageConsumer ic) {
        bitmap.removeConsumer(ic);
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
    private Map<String, WindowsBitmapImageSource> bitmapCache = new HashMap<String, WindowsBitmapImageSource>();

    /** デバイスを ID で指定します． */
    public void changeDevice(int id) {
        if (id >= 0 && id < icons.length) {
            deviceId = id;
            if (bitmapCache.containsKey(String.valueOf(id))) {
                bitmap = bitmapCache.get(String.valueOf(id));
// Debug.println("cache hit: " + id);
            } else {
                bitmap = new WindowsBitmapImageSource(icons[id].getBitmap());
                bitmapCache.put(String.valueOf(id), bitmap);
// Debug.println("new: " + id);
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
