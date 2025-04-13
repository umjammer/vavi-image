/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.ico;

import java.awt.Point;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import vavi.io.LittleEndianDataInputStream;

import static java.lang.System.getLogger;


/**
 * This class represents information such as the size of an icon.
 *
 * <pre>
 *
 *  BYTE  width
 *  BYTE  height
 *  BYTE  colors - 16: 16 colors, 0: 256 colors
 *  BYTE  reserved - must be 0
 *  WORD  hotspot x
 *  WORD  hotspot y
 *  DWORD size
 *  DWORD offset - header(6) + device(16) x count + size x #
 *
 * </pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 */
public class WindowsIconDevice {

    private static final Logger logger = getLogger(WindowsIconDevice.class.getName());

    /** width */
    private int width;

    /** height */
    private int height;

    /** number of colors */
    private int colors;

    /** X hot spot */
    private int hotspotX;

    /** Y hot spot */
    private int hotspotY;

    /** size */
    private int size;

    /** offset */
    private int offset;

    /** for {@link #readFrom} */
    private WindowsIconDevice() {
    }

    /** Creates an icon. */
    public WindowsIconDevice(int width, int height, int colors) {
        this.width = width;
        this.height = height;
        this.colors = colors;
    }

    /** Gets the width. */
    public int getWidth() {
        return width;
    }

    /** Gets the height. */
    public int getHeight() {
        return height;
    }

    /** Gets the number of colors. */
    public int getColors() {
        return colors;
    }

    /** Sets the number of colors. */
    public void setColors(int colors) {
        this.colors = colors;
    }

    /** Gets the size. */
    public int getSize() {
        return size;
    }

    /** Gets the offset. */
    public int getOffset() {
        return offset;
    }

    /** Gets the hot spot. */
    public Point getHotspot() {
        return new Point(hotspotX, hotspotY);
    }

    /** for debug */
    public String toString() {
        return " width: " + width +
                ", height: " + height +
                ", colors: " + colors +
                ", hotspot x: " + hotspotX +
                ", hotspot y: " + hotspotY +
                ", size: " + size +
                ", offset: " + offset;
    }

    /**
     * Creates a specified number of instances of the icon device from the stream.
     */
    public static WindowsIconDevice[] readFrom(LittleEndianDataInputStream lin, int number) throws IOException {

        WindowsIconDevice[] iconDevices = new WindowsIconDevice[number];

        for (int i = 0; i < number; i++) {
            iconDevices[i] = new WindowsIconDevice();

            // read 16 bytes
            iconDevices[i].width = lin.read();
            iconDevices[i].height = lin.read();
            iconDevices[i].colors = lin.read();
            lin.readByte();
            iconDevices[i].hotspotX = lin.readShort();
            iconDevices[i].hotspotY = lin.readShort();
            iconDevices[i].size = lin.readInt();
            iconDevices[i].offset = lin.readInt();

            // Set it back to 0 when exporting!
//            if (iconDevices[i].colors == 0) {
//logger.log(Level.TRACE, "set color 0 -> 256");
//                iconDevices[i].colors = 256;
//            }

//logger.log(Level.TRACE, "device [" + i + "]");
logger.log(Level.DEBUG, iconDevices[i]);
        }

        return iconDevices;
    }
}
