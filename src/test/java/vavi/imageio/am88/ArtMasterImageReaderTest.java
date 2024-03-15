/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.am88;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
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

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod",
                           "sun\\.util\\.logging\\.[\\w\\$]+#\\w+");
    }

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
    void test() throws Exception {
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("AM88");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(ArtMasterImageReader.class.getName())) {
                ir = tmpIr;
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get("src/test/resources/test.am88")));
        Image image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    void test1() throws Exception {
        URL url = ArtMasterImageReaderTest.class.getResource("/test.am88");
        Image image = ImageIO.read(url);
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test0() throws Exception {
        // using cdl cause junit stops awt thread suddenly
        CountDownLatch cdl = new CountDownLatch(1);
        main(new String[] { art88Image });
        cdl.await(); // depends on main frame's exit on close
    }

    //----

    /** */
    public static void main(String[] args) throws IOException {
Debug.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("AM88");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
Debug.println("ImageReader: " + tmpIr.getClass().getName());
            if (tmpIr.getClass().getName().equals(ArtMasterImageReader.class.getName())) {
                ir = tmpIr;
Debug.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(Files.newInputStream(Paths.get(args[0])));
        Image image = ir.read(0);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, 640, 400, 0, 0, 640, 200, this);
            }
        };
        panel.setPreferredSize(new Dimension(640, 400));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
