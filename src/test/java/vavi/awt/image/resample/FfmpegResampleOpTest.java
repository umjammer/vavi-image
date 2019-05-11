/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.resample;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;


/**
 * FfmpegResampleOpTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 070613 nsano initial version <br>
 */
public class FfmpegResampleOpTest {

    static String file = "tmp/erika.jpg";
//    static String file = "tmp/qr.png";

    @Test
    public void test01() throws Exception {
        BufferedImage image = ImageIO.read(new File(file));
System.err.println(image);
        for (float y = 1; y > 0; y -= 0.05) {
            for (float x = 1; x > 0; x -= 0.05) {
System.err.printf("%1.2f, %1.2f\n", x, y);
                BufferedImageOp filter = new FfmpegResampleOp(x, y);
                filter.filter(image, null);
            }
        }
    }

    @Test
    public void test02() throws Exception {
        BufferedImage image = ImageIO.read(new File(file));
System.err.println(image);
        for (float y = 1; y < 2; y += 0.1) {
            for (float x = 1; x < 2; x += 0.1) {
System.err.printf("%1.2f, %1.2f\n", x, y);
                BufferedImageOp filter = new FfmpegResampleOp(x, y);
                filter.filter(image, null);
            }
        }
    }
}

/* */
