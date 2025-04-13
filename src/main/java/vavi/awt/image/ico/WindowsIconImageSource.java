/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.ico;

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

import vavi.awt.image.bmp.WindowsBitmap;
import vavi.awt.image.bmp.WindowsBitmapImageSource;


/**
 * Creates an image for the icon.
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

    /** The icon file contains icons of multiple sizes. */
    private final WindowsIcon[] icons;

    /** Each size is called a device and is managed by a number. */
    private int deviceId = 0;

    /** */
    private BufferedImage image;

    /** @see ImageConsumer */
    private ImageConsumer ic;

    @Override
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
//logger.log(Level.TRACE, image.getType() + ", " + cm);
                ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);
                int[] buf = image.getRGB(0, 0, width, height, null, 0, width);
//logger.log(Level.TRACE, buf.length + ", " + width * height);
                ic.setPixels(0, 0, width, height, ColorModel.getRGBdefault(), buf, 0, width); // colorModel ???
                ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
            }
            this.ic = null;
        }
    }

    @Override
    public void startProduction(ImageConsumer ic) {
        if (bitmap != null) {
            bitmap.addConsumer(ic);
        } else {
            addConsumer(ic);
        }
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer ic) {
        if (bitmap != null) {
            return bitmap.isConsumer(ic);
        } else {
            return ic == this.ic;
        }
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (bitmap != null) {
            bitmap.removeConsumer(ic);
        } else {
            if (this.ic == ic) {
                this.ic = null;
            }
        }
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    /** Returns the current device ID. */
    public int getDeviceId() {
        return deviceId;
    }

    /** Returns how many devices are available. */
    public int getDeviceCount() {
        return icons.length;
    }

    /**
     * Changes the device to the specified format.
     *
     * @throws NoSuchElementException If the specified device does not exist
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
    private final Map<String, WindowsBitmapImageSource> bitmapCache = new HashMap<>();

    /** Selects the device by its ID. */
    public void changeDevice(int id) {
        if (id >= 0 && id < icons.length) {
            deviceId = id;
            if (bitmapCache.containsKey(String.valueOf(id))) {
                bitmap = bitmapCache.get(String.valueOf(id));
//logger.log(Level.TRACE, "cache hit: " + id);
            } else {
                if (icons[id].getBitmap() == null) {
                    image = icons[id].getImage();
                } else {
                    bitmap = new WindowsBitmapImageSource(icons[id].getBitmap());
                    bitmapCache.put(String.valueOf(id), bitmap);
                }
//logger.log(Level.TRACE, "new: " + id);
            }
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(id));
        }
    }

    /** Creates an icon image from a stream. */
    public WindowsIconImageSource(InputStream in) throws IOException {

        icons = WindowsIcon.readFrom(in);

        changeDevice(0);
    }

    /** */
    public WindowsBitmap getWindowsBitmap() {
        return bitmap.getWindowsBitmap();
    }
}
