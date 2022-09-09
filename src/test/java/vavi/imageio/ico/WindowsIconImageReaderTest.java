/*
 * Copyright (c) 2017 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ico;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * WindowsIconImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 Nov 4, 2017 umjammer initial version <br>
 */
public class WindowsIconImageReaderTest {

    @Test
    public void test1() throws Exception {
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("ICO");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(WindowsIconImageReader.class.getName())) {
                ir = tmpIr;
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.ico")));
        List<BufferedImage> images = new ArrayList<>();
        BufferedImage image = ir.read(0);
        images.add(image);
        int count = ir.getNumImages(false);
        LoopCounter counter = new LoopCounter(count);
        for (int i = 1; i < count; i++) {
            image = ir.read(i);
            images.add(image);
        }
        assertEquals(9, images.size());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test0() throws Exception {
        main(new String[] { "src/test/resources/test.ico" });
    }

    //----

    static class LoopCounter {
        int count = 0;
        final int max;
        LoopCounter(int max) {
            this.max = max;
        }
        void increment() {
            this.count = count < max - 1 ? count + 1 : 0;
//System.err.println(count);
        }
        int get() {
            return count;
        }
    }

    /** */
    public static void main(final String[] args) throws IOException {
System.err.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("ICO");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(WindowsIconImageReader.class.getName())) {
                ir = tmpIr;
System.err.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get(args[0])));
        List<BufferedImage> images = new ArrayList<>();
        BufferedImage image = ir.read(0);
        images.add(image);
        int count = ir.getNumImages(false);
        LoopCounter counter = new LoopCounter(count);
        for (int i = 1; i < count; i++) {
            image = ir.read(i);
            images.add(image);
        }

        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(images.get(counter.get()), 0, 0, this);
            }
        };
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                counter.increment();
                panel.repaint();
            }
        });
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
