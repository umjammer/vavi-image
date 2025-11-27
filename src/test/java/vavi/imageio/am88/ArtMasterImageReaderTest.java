/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.am88;

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
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import vavi.imageio.IIOUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * ArtMasterImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/11 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class ArtMasterImageReaderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String art88Image = "src/test/resources/test.am88";

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @DisplayName("via spi, manual selection")
    void test() throws Exception {
        ImageReader ir = IIOUtil.getImageReader("AM88", ArtMasterImageReader.class.getName());
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.am88")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @DisplayName("via spi, auto selection")
    void test1() throws Exception {
        URL url = ArtMasterImageReaderTest.class.getResource("/test.am88");
        assert url != null;
        Image image = ImageIO.read(url);
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test0() throws Exception {
        show(ImageIO.read(Path.of(art88Image).toFile()));
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

    //----

    /** */
    public static void main(String[] args) throws Exception {
Debug.println(args[0]);
        ImageReader ir = IIOUtil.getImageReader("AM88", ArtMasterImageReader.class.getName());
        ir.setInput(Files.newInputStream(Paths.get(args[0])));
        BufferedImage image = ir.read(0);
        show(image);
    }
}
