/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.zim;

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
 * ZimTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/26 nsano initial version <br>
 */
class ZimTest {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        Path path = Paths.get(ZimTest.class.getResource("/test1.zim").toURI());
        BufferedImage image = Zim.decode(new LittleEndianSeekableDataInputStream(Files.newByteChannel(path)));
        show(image);
        while (true) Thread.yield();
    }

    void show(BufferedImage image) {
        JFrame frame = new JFrame("ZIM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JImageComponent panel = new JImageComponent();
        panel.setImage(image);
        panel.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}