/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.mag;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.swing.JImageComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * RetroMagImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/25 nsano initial version <br>
 */
class RetroMagImageReaderTest {

    /** */
    private static ImageReader getSpiImageReader() {
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("MAG");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(RetroMagImageReader.class.getName())) {
                ir = tmpIr;
                break;
            }
        }
        assert ir != null : "no suitable spi";
        return ir;
    }

    /** */
    private static ImageReader getImageReader() {
        return new RetroMagImageReader(new RetroMagImageReaderSpi());
    }

    @Test
    void test0() throws Exception {
        ImageReader ir = getImageReader();
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.mag")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @Disabled("not set as spi")
    void test1() throws Exception {
        URL url = RetroMagImageReaderTest.class.getResource("/test.mag");
        BufferedImage image = ImageIO.read(url);
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        ImageReader ir = getImageReader();
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test2.mag")));
        BufferedImage image = ir.read(0);
        show(image);
    }

    /** using cdl cause junit stops awt thread suddenly */
    static void show(BufferedImage image) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        JFrame frame = new JFrame("Retro MAG");
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        JImageComponent panel = new JImageComponent();
        panel.setImage(image);
        panel.setPreferredSize(new Dimension(640, 400));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }
}