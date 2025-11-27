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
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;

import vavi.imageio.IIOUtil;
import vavi.swing.JImageComponent;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * RetroMagImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/25 nsano initial version <br>
 */
class RetroMagImageReaderTest {

    @Test
    @DisplayName("not set as spi")
    void test0() throws Exception {
//        ImageReader ir = IIOUtil.getImageReader("MAG", RetroMagImageReader.class.getName());
        ImageReader ir = new RetroMagImageReader(new RetroMagImageReaderSpi());
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.mag")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @Disabled("not set as spi")
    void test1() throws Exception {
        URL url = RetroMagImageReaderTest.class.getResource("/test.mag");
        assert url != null;
        BufferedImage image = ImageIO.read(url);
        assertNotNull(image);
    }

    @Test
    @Disabled("raw api direct")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        ImageReader ir = new RetroMagImageReader(new RetroMagImageReaderSpi());
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