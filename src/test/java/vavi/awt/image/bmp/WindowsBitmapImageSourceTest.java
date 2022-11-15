/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * WindowsBitmapImageSourceTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/11 umjammer initial version <br>
 */
class WindowsBitmapImageSourceTest {

    @Test
    void test() throws Exception {
        BufferedImage image = ImageIO.read(WindowsBitmapImageSourceTest.class.getResourceAsStream("/test.bmp"));
        assertNotNull(image);
    }

    //----

    /** */
    public static void main(String[] args) throws IOException {
System.err.println(args[0]);
        Image image = Toolkit.getDefaultToolkit().createImage(new WindowsBitmapImageSource(Files.newInputStream(Paths.get(args[0]))));

        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
