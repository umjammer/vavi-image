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


/**
 * Image を BufferedImage に変換するユーティリティです。
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 021124 nsano initial version <br>
 */
public class ImageConverter {

    /** */
//  private static Log logger = LogFactory.getLog(ImageConverter.class);

    /** */
    private ImageConverter() {}

    /** */
    private static ImageConverter instance = new ImageConverter();

    /** */
    public static ImageConverter getInstance() {
        return instance;
    }

    /** */
    private int cmType = BufferedImage.TYPE_4BYTE_ABGR;

    /** TODO 自動化 */
    public void setColorModelType(int cmType) {
        this.cmType = cmType;
    }

    /** TODO BufferedImage のカラーモデルを自動設定する */
    public BufferedImage toBufferedImage(Image image) {
        int w = image.getWidth(imageObserver);
        int h = image.getHeight(imageObserver);
//logger.debug(w + ", " + h + ": " + image.getClass().getName());
        BufferedImage bi = new BufferedImage(w, h, cmType);
        Graphics g = bi.createGraphics();
        g.drawImage(image, 0, 0, imageObserver);
        return bi;
    }

    /** */
    private ImageObserver imageObserver = new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags,
                                   int x, int y, int width, int height) {
//Debug.println(infoflags);
            if ((infoflags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS) {
                return false;
            } else {
                return true;
            }
        }
    };
}

/* */
