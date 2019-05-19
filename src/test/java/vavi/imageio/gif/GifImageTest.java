/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import vavi.awt.image.gif.NonLzwGifImageSource;
import vavi.swing.JImageComponent;


/**
 * GifImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class GifImageTest {

    @Test
    public void test() throws Exception {
        main(new String[] { "tmp/qr.gif" });
    }

    //----

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
System.err.println(args[0]);
        Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(new FileInputStream(args[0])));
        final JImageComponent component = new JImageComponent();
        final JFrame frame = new JFrame();
        frame.addMouseListener(new MouseAdapter() {
            int count = 1;
            public void mouseClicked(MouseEvent event) {
                try {
System.err.println(args[count]);
                    Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(new FileInputStream(args[count])));
                    component.setImage(image);
                    component.setPreferredSize(new Dimension(image.getWidth(component), image.getHeight(component)));
                    frame.pack();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
                if (count == args.length) {
                    count = 0;
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        component.setImage(image);
        component.setPreferredSize(new Dimension(image.getWidth(component), image.getHeight(component)));
        frame.getContentPane().add(component);
        frame.pack();
    }
}

/* */
