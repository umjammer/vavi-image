/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.resample;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.util.logging.Level;

import vavi.util.Debug;


/**
 * FfmpegResampleOp.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 060616 nsano initial version <br>
 */
public class FfmpegResampleOp implements BufferedImageOp {

    /** */
    public enum Hint {
        NONE(0),
        FAST_BILINEAR(1),
        BILINEAR(2),
        BICUBIC(4),
        X(8),
        POINT(0x10),
        AREA(0x20),
        BICUBLIN(0x40),
        GAUSS(0x80),
        SINC(0x100),
        LANCZOS(0x200),
        SPLINE(0x400);
        Hint(int value) {
            this.value = value;
        }
        int value;
    }

    /** */
    private Hint hint;

    /** */
    private double sx;
    /** */
    private double sy;

    /**
     * hint is {@link Hint#FAST_BILINEAR}
     * @param sx x scaling
     * @param sy y scaling
     */
    public FfmpegResampleOp(double sx, double sy) {
        this(sx, sy, Hint.AREA);
    }

    /**
     * @param sx x scaling
     * @param sy y scaling
     */
    public FfmpegResampleOp(double sx, double sy, Hint hint) {
        this.sx = sx;
        this.sy = sy;
        this.hint = hint;
    }

    /**
     * JNI.
     * @param inBuffer src pixels, int[] or byte[]
     * @param inType {@link DataBuffer#TYPE_INT} or {@link DataBuffer#TYPE_BYTE}
     * @param inPixelFormat src {@link BufferedImage#getType()}
     * @param inPixelSize 24 or 32 bits
     * @param inWidth src width
     * @param inHeight src height
     * @param outBuffer scaled pixels, int[] or byte[]
     * @param outWidth scaled width
     * @param outHeight scaled width
     * @param outType {@link DataBuffer#TYPE_INT} or {@link DataBuffer#TYPE_BYTE}
     * @param outPixelFormat dest {@link BufferedImage#getType()}
     * @param outPixelSize 24 or 32 bits
     * @param hint scaling hint, see {@link #hint}
     */
    private native void filterInternal(Object inBuffer, int inType, int inPixelFormat, int inPixelSize, int inWidth, int inHeight, Object outBuffer, int outType, int outPixelFormat, int outPixelSize, int outWidth, int outHeight, int hint);

    /* */
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {

        if (src.getColorModel() instanceof IndexColorModel) {
            throw new IllegalArgumentException("Resampling cannot be performed on an indexed image");
        }

        if (dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }

        int srcPxelFormat = src.getType();
        int destPxelFormat = dest.getType();
Debug.println(Level.FINE, "src pixel format: " + src.getType());
Debug.println(Level.FINE, "dest pixel format: " + dest.getType());

        int srcPixelSize = src.getColorModel().getPixelSize();
        int destPixelSize = dest.getColorModel().getPixelSize();
Debug.println(Level.FINE, "src pixel size: " + src.getColorModel().getPixelSize());
Debug.println(Level.FINE, "dest pixel size: " + dest.getColorModel().getPixelSize());

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        int resizedWidth = dest.getWidth();
        int resizedHeight = dest.getHeight();

        int srcDataType = src.getRaster().getDataBuffer().getDataType();
        int destDataType = dest.getRaster().getDataBuffer().getDataType();
Debug.println(Level.FINE, "src data type: " + src.getRaster().getDataBuffer().getDataType());
Debug.println(Level.FINE, "dest data type: " + dest.getRaster().getDataBuffer().getDataType());

        Object srcBuffer;
        if (srcDataType == DataBuffer.TYPE_BYTE) {
            srcBuffer = ((DataBufferByte) src.getRaster().getDataBuffer()).getData();
        } else {
            srcBuffer = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
            srcPixelSize = 32;
        }
        Object destBuffer;
        if (destDataType == DataBuffer.TYPE_BYTE) {
            destBuffer = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();
        } else {
            destBuffer = ((DataBufferInt) dest.getRaster().getDataBuffer()).getData();
            destPixelSize = 32;
        }

        filterInternal(srcBuffer, srcDataType, srcPxelFormat, srcPixelSize, srcWidth, srcHeight, destBuffer, destDataType, destPxelFormat, destPixelSize, resizedWidth, resizedHeight, hint.value);

        return dest;
    }

    /**
     * @param destCM when null, used src color model 
     */
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        Rectangle2D destBounds = getBounds2D(src);
        return new BufferedImage(destCM, destCM.createCompatibleWritableRaster((int) destBounds.getWidth(), (int) destBounds.getHeight()), destCM.isAlphaPremultiplied(), null);
    }

    /* */
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, (int) (src.getWidth() * sx), (int) (src.getHeight() * sy));
    }

    /* */
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX() * sx, srcPt.getY() * sy);
        return dstPt;
    }

    /* TODO implement */
    public RenderingHints getRenderingHints() {
        return null;
    }

    /* */
    static {
        try {
            System.loadLibrary("FfmpegResampleOpWrapper");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace(System.err);
            throw new IllegalStateException(e);
        }
    }
}

/* */
