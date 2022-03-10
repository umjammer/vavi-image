/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.resample;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;


/**
 * FfmpegResampleOpTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 070613 nsano initial version <br>
 */
public class FfmpegResampleOpTest {

    static String file = "/erika.jpg";

    @Test
    public void test01() throws Exception {
        BufferedImage image = ImageIO.read(FfmpegResampleOpTest.class.getResourceAsStream(file));
System.err.println(image);
        for (float y = 1f; y > 0f; y -= 0.05f) {
            for (float x = 1f; x > 0f; x -= 0.05f) {
System.err.printf("%1.2f, %1.2f\n", x, y);
                BufferedImageOp filter = new FfmpegResampleOp(x, y);
                filter.filter(image, null);
            }
        }
    }

    @Test
    public void test02() throws Exception {
        BufferedImage image = ImageIO.read(FfmpegResampleOpTest.class.getResourceAsStream(file));
System.err.println(image);
        for (float y = 1f; y < 2f; y += 0.1f) {
            for (float x = 1f; x < 2f; x += 0.1f) {
System.err.printf("%1.2f, %1.2f\n", x, y);
                BufferedImageOp filter = new FfmpegResampleOp(x, y);
                filter.filter(image, null);
            }
        }
    }
}

/* */
