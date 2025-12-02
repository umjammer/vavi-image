/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.System.Logger;

import static java.lang.System.getLogger;


/**
 * A utility to convert an Image to a BufferedImage.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021124 nsano initial version <br>
 */
public class ImageConverter {

//    private static final Logger logger = getLogger(ImageConverter.class.getName());

    /** */
    private ImageConverter() {}

    /** */
    private static final ImageConverter instance = new ImageConverter();

    /** */
    public static ImageConverter getInstance() {
        return instance;
    }

    /** */
    private int cmType = BufferedImage.TYPE_4BYTE_ABGR;

    /** TODO automation */
    public void setColorModelType(int cmType) {
        this.cmType = cmType;
    }

    /** TODO Automatically set the color model of a BufferedImage */
    public BufferedImage toBufferedImage(Image image) {
        int w = image.getWidth(imageObserver);
        int h = image.getHeight(imageObserver);
//logger.log(Level.DEBUG, w + ", " + h + ": " + image.getClass().getName());
        BufferedImage bi = new BufferedImage(w, h, cmType);
        Graphics g = bi.createGraphics();
        g.drawImage(image, 0, 0, imageObserver);
        return bi;
    }

    /** */
    private final ImageObserver imageObserver = (img, infoflags, x, y, width, height) -> {
//logger.log(Level.TRACE, infoflags);
        if ((infoflags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS) {
            return false;
        } else {
            return true;
        }
    };
}
