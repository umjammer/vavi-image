/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.mag;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.swing.JImageComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * MagImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/25 nsano initial version <br>
 */
class MagImageReaderTest {

    @Test
    void test0() throws Exception {
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("MAG");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(MagImageReader.class.getName())) {
                ir = tmpIr;
                break;
            }
        }
        assert ir != null : "no suitable spi";
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.mag")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    void test1() throws Exception {
        URL url = MagImageReaderTest.class.getResource("/test.mag");
        BufferedImage image = ImageIO.read(url);
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        URL url = MagImageReaderTest.class.getResource("/test2.mag");
        BufferedImage image = ImageIO.read(url);
        show(image);
        while (true) Thread.yield();
    }

    void show(BufferedImage image) {
        JFrame frame = new JFrame("MAG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JImageComponent panel = new JImageComponent();
        panel.setImage(image);
        panel.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}