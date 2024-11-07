/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.ico;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.imageio.ImageIO;

import vavi.awt.image.bmp.WindowsBitmap;
import vavi.io.LittleEndianDataInputStream;

import static java.lang.System.getLogger;


/**
 * Windows Icon format.．
 *
 * <pre><code>
 *
 *
 *   --- TOF FILE header ( 6 bytes ) --- ... Header
 *   [a header]
 *   --- 1 / count of ICON/CURSOR header ( 16 bytes ) --- ... WindowsIconDevice
 *   [one device]
 *   --- 2 / count ---
 *   [one device]
 *         :
 *         :
 *
 *
 * </code></pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 *          1.01 010817 nsano use Header class <br>
 *          1.10 010901 nsano refine <br>
 *          1.11 021104 nsano fix color related <br>
 */
public class WindowsIcon {

    private static final Logger logger = getLogger(WindowsIcon.class.getName());

    /** the device for the icon */
    private final WindowsIconDevice device;

    /** the bitmap for the icon */
    private WindowsBitmap bitmap;

    /** the image for the icon */
    private BufferedImage image;

    /** the mask */
    private byte[] mask;

    /** Creates an icon. */
    public WindowsIcon(WindowsIconDevice device, WindowsBitmap bitmap) {
        this.device = device;
        this.bitmap = bitmap;
        createMask();
    }

    /** Creates an icon. */
    public WindowsIcon(WindowsIconDevice device, BufferedImage image) {
        this.device = device;
        this.image = image;
    }

    /** Creates an icon device. */
    public WindowsIconDevice getDevice() {
        return device;
    }

    /** Gets the icon image. */
    public BufferedImage getImage() {
        return image;
    }

    /** Gets the icon bitmap. */
    public WindowsBitmap getBitmap() {
        return bitmap;
    }

    /** Gets the icon mask. */
    public byte[] getMask() {
        return mask;
    }

    /**
     * Creates a mask. The width of the mask data is padded to a multiple of 4. The Y axis is inverted.
     */
    private void createMask() {
        int off = bitmap.getImageSize() + bitmap.getOffset();
        int size = bitmap.getSize() - off;
//logger.log(Level.TRACE, "mask: " + size);
//logger.log(Level.TRACE, bitmap.getWidth() + ", " + bitmap.getHeight());

        byte[] buf = new byte[size]; // Mask size of the data
        for (int i = 0; i < size; i++) {
            buf[i] = bitmap.getBitmap()[bitmap.getImageSize() + i];
        }

//logger.log(Level.TRACE, StringUtil.getDump(buf));

        // Mask with padding removed
        mask = new byte[(bitmap.getWidth() / 8) * bitmap.getHeight()];
//logger.log(Level.TRACE, "real: " + (bitmap.getWidth() / 8) * bitmap.getHeight());

        int m = (((bitmap.getWidth() / 8) + 3) / 4) * 4; // Width with padding
//logger.log(Level.TRACE, "x: " + m);
        int i = 0;
top:    for (int y = bitmap.getHeight() - 1; y >= 0; y--) { // Invert Y-Axis
            for (int x = 0; x < m; x++) {
                if (x < bitmap.getWidth() / 8) {
//logger.log(Level.TRACE, y * m + x);
                    if (i < size) {
                        mask[i++] = buf[y * m + x];
//logger.log(Level.TRACE, Debug.toBits(mask[i-1]));
                    } else {
                        break top;
                    }
                }
            }
//logger.log(Level.TRACE, "");
        }
    }

    /**
     * This class represents the header of an icon file.
     *
     * <pre>
     *
     *  WORD  dummy
     *  WORD  type  1: icon
     *  WORD  count
     *
     * </pre>
     */
    private static final class Header {

        /** type */
        int type;

        /** Number of icon devices */
        int number;

        /**
         * Creates a file header instance from a stream.
         */
        static Header readFrom(LittleEndianDataInputStream lin) throws IOException {

            Header h = new Header();

            @SuppressWarnings("unused")
            int dummy;

            // 6 bytes
            dummy = lin.readByte();
            dummy = lin.readByte();
            h.type = lin.readShort();
            h.number = lin.readShort();

            return h;
        }

        /** for debug */
        public String toString() {
            return "type: " + type +
                    ", has: " + number;
        }
    }

    /**
     * Reads the icon for the specified device from the stream.
     */
    private static WindowsIcon readIcon(LittleEndianDataInputStream lin, WindowsIconDevice iconDevice) throws IOException {

        int offset = iconDevice.getOffset();
        int size = iconDevice.getSize();

        if (iconDevice.getWidth() == 0 && iconDevice.getHeight() == 0) {
            byte[] buf = new byte[size];
            DataInputStream dis = new DataInputStream(lin);
            dis.readFully(buf);
//logger.log(Level.TRACE, StringUtil.getDump(buf, 128));
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            BufferedImage image = ImageIO.read(bais);
logger.log(Level.DEBUG, image);

            return new WindowsIcon(iconDevice, image);
        } else {
            return new WindowsIcon(iconDevice, WindowsBitmap.readFrom(lin, offset, size));
        }
    }

    /**
     * Creates an instance of an icon from a stream.
     */
    public static WindowsIcon[] readFrom(InputStream in) throws IOException {

        LittleEndianDataInputStream lin = new LittleEndianDataInputStream(in);

        WindowsIcon[] icons;
        WindowsIconDevice[] iconDevices;

        Header h = Header.readFrom(lin);
logger.log(Level.DEBUG, h);
        icons = new WindowsIcon[h.number];
        iconDevices = WindowsIconDevice.readFrom(lin, h.number);

        for (int i = 0; i < h.number; i++) {
            icons[i] = readIcon(lin, iconDevices[i]);
//if (iconDevices[i].getColors() == 0) {
// icons[i].bitmap.setUsedColor(256);
// icons[i].bitmap.setBits(8);
//}
        }

        return icons;
    }
}
