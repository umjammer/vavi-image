/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.quantization;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;

import javax.imageio.ImageIO;

import vavi.awt.image.BasicBufferedImageOp;


/**
 * ImageMagickQuantizeOp.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 060616 nsano initial version <br>
 */
public class ImageMagickQuantizeOp extends BasicBufferedImageOp {

    /** */
    private final int colors;

    /** */
    public ImageMagickQuantizeOp(int colors) {
        this.colors = colors;
    }

    @Override
    protected int[] filterPixels(int width, int height, int[] inPixels) {
        int[][] pixels2D = new int[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels2D[x][y] = inPixels[y * width + x];
            }
        }

        int[] palette = ImageMagickQuantizer.quantizeImage(pixels2D, colors);

        int[] outPixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                outPixels[y * width + x] = palette[pixels2D[x][y]];
            }
        }

        return outPixels;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        String file = args[0];

        BufferedImage inImage = ImageIO.read(new File(file));
        BufferedImageOp filter = new ImageMagickQuantizeOp(256);
        BufferedImage outImage = filter.createCompatibleDestImage(inImage, null);
        filter.filter(inImage, outImage);
    }
}
