/*
 * Copyright (c) 2013 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.bmp;

import java.awt.Graphics;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
    public void test() throws Exception {
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("BMP");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            System.err.println("ImageReader: " + tmpIr.getClass().getName());
            if (tmpIr.getClass().getName().equals(WindowsBitmapImageReader.class.getName())) {
                ir = tmpIr;
                System.err.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.bmp")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test0() throws Exception {
        main(new String[] { "src/test/resources/test.bmp" });
    }

    //----

    /** */
    public static void main(final String[] args) throws IOException {
System.err.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("BMP");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
System.err.println("ImageReader: " + tmpIr.getClass().getName());
            if (tmpIr.getClass().getName().equals(WindowsBitmapImageReader.class.getName())) {
                ir = tmpIr;
System.err.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get(args[0])));
        Image image = ir.read(0);

        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
