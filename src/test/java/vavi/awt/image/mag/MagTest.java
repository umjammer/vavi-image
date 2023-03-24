/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.mag;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.io.LittleEndianSeekableDataInputStream;
import vavi.swing.JImageComponent;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * MagTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/25 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
class MagTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "magImage")
    String magImage = "src/test/resources/test.mag";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        Path path = Paths.get(magImage);
        BufferedImage image = Mag.load(new LittleEndianSeekableDataInputStream(Files.newByteChannel(path)));
        show(image);
    }

    /** using cdl cause junit stops awt thread suddenly */
    void show(BufferedImage image) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        JFrame frame = new JFrame("MAG");
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        JPanel panel = new JPanel() {
            @Override public void paintComponent(Graphics g) { g.drawImage(image, 0, 0, 640, 400, this); }
        };
        panel.setPreferredSize(new Dimension(640, 400));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }
}