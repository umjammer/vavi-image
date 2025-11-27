/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.cgmate;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.imageio.IIOUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * CgMateImagerReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-11-27 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class CgMateImagerReaderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String cgmate = "src/test/resources/cgmate.pic";

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @DisplayName("via spi, manual selection")
    @Disabled("not registered to service yet")
    void test() throws Exception {
        ImageReader ir = IIOUtil.getImageReader("CGMATE", CgMateImagerReaderTest.class.getName());
        ir.setInput(Files.newInputStream(Path.of(cgmate)));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @DisplayName("via spi, auto selection")
    @Disabled("not registered to service yet")
    void test1() throws Exception {
        URL url = CgMateImagerReaderTest.class.getResource("/cgmate.pic");
        assert url != null;
        Image image = ImageIO.read(url);
        assertNotNull(image);
    }

    @Test
    @DisplayName("raw api")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test0() throws Exception {
        Path path = Path.of(cgmate);
Debug.println(path + ", " + Files.exists(path));
        ImageReader ir = new vavi.imageio.cgmate.CgMateImagerReader(new CgMateImageReaderSpi());
        ir.setInput(Files.newInputStream(Path.of(cgmate)));
        BufferedImage image = ir.read(0);
        show(image);
    }

    /** gui */
    static void show(BufferedImage image) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);

        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, 640, 400, 0, 0, 640, 200, this);
            }
        };
        panel.setPreferredSize(new Dimension(640, 400));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

        cdl.await();
    }
}
