/*
 * Copyright (c) 2013 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.bmp;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.imageio.IIOUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * WindowsBitmapImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2013/08/19 umjammer initial version <br>
 */
public class WindowsBitmapImageReaderTest {

    @Test
    @DisplayName("via spi, manual selection")
    public void test() throws Exception {
        ImageReader ir = IIOUtil.getImageReader("BMP", WindowsBitmapImageReader.class.getName());
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.bmp")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test0() throws Exception {
        BufferedImage image = ImageIO.read(Path.of("src/test/resources/test.bmp").toFile());
        show(image);
    }

    /** gui */
    static void show(Image image) throws Exception {
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
        System.err.println(args[0]);
        ImageReader ir = IIOUtil.getImageReader("BMP", WindowsBitmapImageReader.class.getName());
        ir.setInput(Files.newInputStream(Paths.get(args[0])));
        BufferedImage image = ir.read(0);
        show(image);
    }
}
