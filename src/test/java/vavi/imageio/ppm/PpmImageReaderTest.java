/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ppm;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.imageio.IIOUtil;
import vavi.imageio.pic.PicImageReader;
import vavi.util.Debug;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * PpmImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class PpmImageReaderTest {

    @Test
    @DisplayName("via spi, manual selection")
    public void test() throws Exception {
        Image image = ImageIO.read(Path.of("src/test/resources/test.ppm").toFile());
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test0() throws Exception {
        BufferedImage image = ImageIO.read(Path.of("src/test/resources/test.ppm").toFile());
        show(image);
    }

    /** */
    static void show(BufferedImage image) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        frame.setSize(800, 600);
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);

        cdl.await();
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
Debug.println(args[0]);
        ImageReader ir = IIOUtil.getImageReader("PPM", PpmImageReader.class.getName());
//        ir.setInput(new FileInputStream(args[0]));
//        final Image image = ir.read(0);
        BufferedImage image = ImageIO.read(Path.of(args[0]).toFile());
        show(image);
    }
}
