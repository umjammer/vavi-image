/*
 * Copyright 2006 Jerry Huxtable
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vavi.awt.image;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.Serializable;


/**
 * A filter which acts as a superclass for filters which need to have the whole
 * image in memory to do their stuff.
 *
 * @see "http://www.jhlabs.com/"
 */
public abstract class BasicBufferedImageOp extends AbstractBufferedImageOp implements Serializable {

    /** */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        int[] inPixels = getRGB(src, 0, 0, width, height, null);
        inPixels = filterPixels(width, height, inPixels);
        setRGB(dst, 0, 0, dst.getWidth(), dst.getHeight(), inPixels);

        return dst;
    }

    /**
     * @param destCM when null, used src color model
     */
    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        Rectangle2D destBounds = getBounds2D(src);
        if (destCM != null) {
            return new BufferedImage(destCM, destCM.createCompatibleWritableRaster((int) destBounds.getWidth(), (int) destBounds.getHeight()), destCM.isAlphaPremultiplied(), null);
        } else {
            return new BufferedImage((int) destBounds.getWidth(), (int) destBounds.getHeight(), src.getType());
        }
    }

    /** */
    @Override
    public abstract Rectangle2D getBounds2D(BufferedImage src);

    /**
     * @param width src width
     * @param height src height
     */
    protected abstract int[] filterPixels(int width, int height, int[] inPixels);
}

/* */
