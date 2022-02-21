/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.am88;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import vavi.util.Debug;


/**
 * ArtMasterImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/11 umjammer initial version <br>
 */
class ArtMasterImageReaderTest {

    @Test
    void test() {
        fail("Not yet implemented");
    }

    //----

    /** */
    public static void main(final String[] args) throws IOException {
Debug.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("AM88");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
Debug.println("ImageReader: " + tmpIr.getClass().getName());
            if (tmpIr.getClass().getName().equals(ArtMasterImageReader.class.getName())) {
                ir = tmpIr;
Debug.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(new FileInputStream(args[0]));
        final Image image = ir.read(0);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, 640, 400, 0, 0, 640, 200, this);
            }
        };
        panel.setPreferredSize(new Dimension(640, 400));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}

/* */
