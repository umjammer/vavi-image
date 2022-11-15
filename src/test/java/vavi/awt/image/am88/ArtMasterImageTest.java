/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.am88;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * ArtMasterImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/11/29 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class ArtMasterImageTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "art88")
    String art88 = "src/test/resources/test.am88";

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        Image image = Toolkit.getDefaultToolkit().createImage(new ArtMasterImageSource(Files.newInputStream(Paths.get(art88))));
        assertNotNull(image);
    }

    @Test
    void test2() throws Exception {
        BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(art88)));
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test() throws Exception {
        main(new String[0]);
        while (true) Thread.yield();
    }

    //----

    /** */
    public static void main(String[] args) throws IOException {
        ArtMasterImageTest app = new ArtMasterImageTest();
//        app.art88 = args[0];
        PropsEntity.Util.bind(app, args);
Debug.println(app.art88);
        Image image = Toolkit.getDefaultToolkit().createImage(new ArtMasterImageSource(Files.newInputStream(Paths.get(app.art88))));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
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
