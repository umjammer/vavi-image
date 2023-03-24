/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.mag;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * RetroMagTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-03-20 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
class RetroMagTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "magImage")
    String magImage;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test0() throws Exception {
        new RetroMag().mainProcess(Files.newInputStream(Paths.get(magImage)));
    }

    @Test
    @DisplayName("MAG")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        BufferedImage image = new RetroMag().mainProcess(Files.newInputStream(Paths.get(magImage)));
        show(image, "MAG");
    }

    /** using cdl cause junit stops awt thread suddenly */
    void show(BufferedImage image, String type) throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        JFrame frame = new JFrame(type);
        JComponent panel = new JComponent() {
            @Override public void paintComponent(Graphics g) { g.drawImage(image, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, image.getWidth(), image.getHeight(), this); }
        };
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { panel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight())); }
        });
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }
}
