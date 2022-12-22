/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.pic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import vavi.swing.JImageComponent;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * RetroTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-12-18 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
class RetroTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "picImage")
    String picImage;

    @Property(name = "magImage")
    String magImage;

    @Property(name = "mkiImage")
    String mkiImage;

    @Property(name = "piImage")
    String piImage;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @DisplayName("PIC")
    void test1() throws Exception {
        BufferedImage image = new Retro().mainProcess(picImage);
        show(image, "PIC");
    }

    @Test
    @DisplayName("MAG")
    void test2() throws Exception {
        BufferedImage image = new Retro().mainProcess(magImage);
        show(image, "MAG");
    }

    @Test
    @DisplayName("MKI")
    void test3() throws Exception {
        BufferedImage image = new Retro().mainProcess(mkiImage);
        show(image, "MAKI");
    }

    // TODO wip, sample
    @Test
    @DisplayName("PI")
    void test4() throws Exception {
        BufferedImage image = new Retro().mainProcess(piImage);
        show(image, "PI");
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
