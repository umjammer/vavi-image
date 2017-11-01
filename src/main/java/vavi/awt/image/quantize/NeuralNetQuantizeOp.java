/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vavi.awt.image.quantize;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.io.IOException;


/**
 * <p>
 * Single-input/single-output operation that will reduce the number of colours
 * used in an image to 256 or less.
 * </p>
 * 
 * Current revision $Revision: 1.2 $ On branch $Name: $ Latest change by
 * $Author: jelmer $ on $Date: 2005/08/27 23:23:57 $
 * 
 * @author <a href="mailto:jkuperus@gmail.com">Jelmer Kuperus</a>
 */
public class NeuralNetQuantizeOp implements BufferedImageOp {

    /** */
    private int colors;

    /** */
    public NeuralNetQuantizeOp(int colors) {
        this.colors = colors;
    }

    /* */
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        try {
            NeuralNetQuantizer quantizer = new NeuralNetQuantizer(src, src.getWidth(), src.getHeight(), colors);

            if (dest == null) {
                dest = createCompatibleDestImage(src, null);
            }

            int width = src.getWidth();
            int height = src.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    dest.setRGB(x, y, quantizer.convert(src.getRGB(x, y)));
                }
            }
            return dest;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* */
    public Rectangle2D getBounds2D(BufferedImage src) {
        return src.getRaster().getBounds();
    }

    /* */
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {

        if (destCM == null) {
            destCM = src.getColorModel();
        }

        int width = src.getWidth();
        int height = src.getHeight();

        return new BufferedImage(destCM, destCM.createCompatibleWritableRaster(width, height), destCM.isAlphaPremultiplied(), null);
    }

    /* */
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    /* */
    public RenderingHints getRenderingHints() {
        return null;
    }
}

/* */

