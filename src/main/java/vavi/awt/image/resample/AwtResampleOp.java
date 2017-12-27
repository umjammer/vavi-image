/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.resample;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;


/**
 * AwtResampleOp.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 060616 nsano initial version <br>
 */
public class AwtResampleOp implements BufferedImageOp {

    /** */
    private double sx;
    /** */
    private double sy;

    /** */
    private int hint;

    /**
     * TODO hints
     * @param sx 比率だよ！
     * @param sy 比率だよ！
     */
    public AwtResampleOp(double sx, double sy) {
        this(sx, sy, Image.SCALE_AREA_AVERAGING);
    }

    /**
     * @param sx 比率だよ！
     * @param sy 比率だよ！
     */
    public AwtResampleOp(double sx, double sy, int hint) {
        this.sx = sx;
        this.sy = sy;
        this.hint = hint;
    }

    /**
     * @param dst when null, created by {@link #createCompatibleDestImage(BufferedImage, ColorModel)} 
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Rectangle destBounds = (Rectangle) getBounds2D(src);
        Image tmpImage = src.getScaledInstance(destBounds.width, destBounds.height, hint);
        if (dst == null) {
            dst = createCompatibleDestImage(src, src.getColorModel());
        }
        Graphics g = dst.createGraphics();
        g.drawImage(tmpImage, 0, 0, null);
        
        return dst;
    }

    /**
     * @param destCM when null, used src color model 
     */
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        Rectangle destBounds = (Rectangle) getBounds2D(src);
        if (destCM != null) {
            return new BufferedImage(destCM, destCM.createCompatibleWritableRaster(destBounds.width, destBounds.height), destCM.isAlphaPremultiplied(), null);
        } else {
            return new BufferedImage(destBounds.width, destBounds.height, src.getType());
        }
    }

    /** */
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, (int) (src.getWidth() * sx), (int) (src.getHeight() * sy));
    }

    /** */
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX() * sx, srcPt.getY() * sy);
        return dstPt;
    }

    /** TODO impl */
    public RenderingHints getRenderingHints() {
        return null;
    }
}

/* */
