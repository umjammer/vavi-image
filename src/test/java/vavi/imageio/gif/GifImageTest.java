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
import java.io.IOException;

import javax.swing.JFrame;

import org.junit.Test;

import vavi.imageio.bmp.WindowsBitmapImageReaderTest;
import vavi.swing.JImageComponent;


/**
 * GifImageTest.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class GifImageTest {

    @Test
    public void test() throws Exception {
        WindowsBitmapImageReaderTest.main(new String[] { "tmp/qr.gif" });
    }

    //----

    /** */
    public static void main(final String[] args) throws IOException {
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
                    component.repaint();
                    frame.setPreferredSize(new Dimension(image.getWidth(null) + frame.getInsets().left + frame.getInsets().right, image.getHeight(null) + frame.getInsets().top + frame.getInsets().bottom));
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
        frame.setPreferredSize(new Dimension(image.getWidth(null) + frame.getInsets().left + frame.getInsets().right, image.getHeight(null) + frame.getInsets().top + frame.getInsets().bottom));
        component.setImage(image);
        frame.getContentPane().add(component);
        frame.pack();
    }
}

/* */
