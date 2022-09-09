/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.imageio.IIOUtil;

import vavix.util.grep.FileDigger;
import vavix.util.grep.RegexFileDigger;


/**
 * GifUnderDirectory. gif in directory
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/02/09 umjammer initial version <br>
 */
public class GifUnderDirectory {

    static {
        IIOUtil.setOrder(ImageReaderSpi.class, "vavi.imageio.gif.NonLzwGifImageReaderSpi", "com.sun.imageio.plugins.gif.GIFImageReaderSpi");
    }

    static BufferedImage image;

    /**
     * @param args 0: dir
     */
    public static void main(String[] args) throws Exception {
        final JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(640, 480));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);

        new RegexFileDigger(new FileDigger.FileDredger() {
            public void dredge(File file) throws IOException {
                try {
System.err.println("--- " + file + " ---");
                    image = ImageIO.read(file);
                    panel.repaint();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, Pattern.compile(".+\\.(gif|GIF)")).dig(new File(args[0]));
    }
}

/* */
