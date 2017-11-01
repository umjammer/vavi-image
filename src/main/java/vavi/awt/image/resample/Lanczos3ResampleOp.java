/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.resample;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.geometry.Resample;
import net.sourceforge.jiu.gui.awt.RGBA;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import vavi.awt.image.BasicBufferedImageOp;


/**
 * Lanczos3ResampleOp.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060616 nsano initial version <br>
 */
public class Lanczos3ResampleOp extends BasicBufferedImageOp {

    /** */
    private double sx;
    /** */
    private double sy;

    /** */
    public Lanczos3ResampleOp(double sx, double sy) {
        this.sx = sx;
        this.sy = sy;
    }

    /* */
    protected int[] filterPixels(int width, int height, int[] inPixels) {

        RGB24Image inImage = new MemoryRGB24Image(width, height);
        int offset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = inPixels[offset++] & 0xffffff;
                // TODO: store alpha value; requires some sort of 
                // transparency channel data type yet to be implemented
                inImage.putSample(RGBIndex.INDEX_RED, x, y, pixel >> 16);
                inImage.putSample(RGBIndex.INDEX_GREEN, x, y, (pixel >> 8) & 0xff);
                inImage.putSample(RGBIndex.INDEX_BLUE, x, y, pixel & 0xff);
            }
        }

        int destWidth = (int) (width * sx);
        int destHeight = (int) (height * sy);

        Resample resample = new Resample();

        try {
            resample.setInputImage(inImage);
            resample.setSize(destWidth, destHeight);
            resample.setFilter(Resample.FILTER_TYPE_LANCZOS3);
            resample.process();
        } catch (MissingParameterException e) {
            throw new IllegalStateException(e);
        } catch (WrongParameterException e) {
            throw new IllegalStateException(e);
        }

        RGB24Image outImage = (RGB24Image) resample.getOutputImage(); 
        int[] outPixels = new int[destWidth * destHeight];
        byte[] red = new byte[destWidth];
        byte[] green = new byte[destWidth];
        byte[] blue = new byte[destWidth];
        int destOffset = 0;
        for (int y = 0; y < destHeight; y++) {
            outImage.getByteSamples(RGBIndex.INDEX_RED, 0, y, destWidth, 1, red, 0);
            outImage.getByteSamples(RGBIndex.INDEX_GREEN, 0, y, destWidth, 1, green, 0);
            outImage.getByteSamples(RGBIndex.INDEX_BLUE, 0, y, destWidth, 1, blue, 0);
            RGBA.convertFromRGB24(red, 0, green, 0, blue, 0, RGBA.DEFAULT_ALPHA, outPixels, destOffset, destWidth);
            destOffset += destWidth;
        }

        return outPixels;
    }

    /* */
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, (int) (src.getWidth() * sx), (int) (src.getHeight() * sy));
    }

    /** @return scaled point  */
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX() * sx, srcPt.getY() * sy);
        return dstPt;
    }
}

/* */
