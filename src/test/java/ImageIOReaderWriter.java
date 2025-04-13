/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

import vavi.awt.ImageComponent;


/**
 * ImageIO display.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021117 nsano initial version <br>
 */
public class ImageIOReaderWriter {

    /**
     * @param args image
     */
    public static void main(String[] args) throws Exception {
        String[] rs = ImageIO.getReaderFormatNames();
        System.err.println("-- reader --");
        for (String r : rs) {
            System.err.println(r);
        }
        rs = ImageIO.getReaderFileSuffixes();
        System.err.println("-- reader suffixes --");
        for (String r : rs) {
            System.err.println(r);
        }
        System.err.println("-- writer --");
        String[] ws = ImageIO.getWriterFormatNames();
        for (String w : ws) {
            System.err.println(w);
        }

System.err.println(args[0]);
        Image image = ImageIO.read(new File(args[0]));
System.err.println(image);
        JFrame frame = new JFrame();
        frame.setSize(320, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageComponent component = new ImageComponent();
        component.setImage(image);
        frame.getContentPane().add(component);
        frame.setVisible(true);
    }
}
