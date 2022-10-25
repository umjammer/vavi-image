/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.mag;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFrame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.io.LittleEndianSeekableDataInputStream;
import vavi.swing.JImageComponent;


/**
 * MagTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/25 nsano initial version <br>
 */
class MagTest {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        Path path = Paths.get(MagTest.class.getResource("/test.mag").toURI());
        BufferedImage image = Mag.load(new LittleEndianSeekableDataInputStream(Files.newByteChannel(path)));
        show(image);
        while (true) Thread.yield();
    }

    void show(BufferedImage image) {
        JFrame frame = new JFrame("MAG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JImageComponent panel = new JImageComponent();
        panel.setImage(image);
        panel.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}