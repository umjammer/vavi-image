/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.imageio.IIOUtil;


/**
 * ImageIO display.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021117 nsano initial version <br>
 */
public class CustomPNGReader {

    public static void main(String[] args) throws Exception {
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("PNG");
        System.err.println("-- PNG reader --");
        while (irs.hasNext()) {
            System.err.println(irs.next().getClass().getName());
        }

        deregister(ImageReaderSpi.class, "com.sun.imageio.plugins.png.PNGImageReaderSpi");

        BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(args[0])));

        JFrame frame = new JFrame();
        frame.setSize(image.getWidth(), image.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    /**
     * already in library
     * @see IIOUtil#deregister(Class, String)
     */
    public static <T> void deregister(Class<T> pt, String p0) {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        T sp = null;
        Iterator<T> i = iioRegistry.getServiceProviders(pt, true);
        while (i.hasNext()) {
            T p = i.next();
            if (p0.equals(p.getClass().getName())) {
                sp = p;
            }
        }
        if (sp == null) {
            throw new IllegalArgumentException(p0 + " not found");
        }
        iioRegistry.deregisterServiceProvider(sp, pt);
    }
}
