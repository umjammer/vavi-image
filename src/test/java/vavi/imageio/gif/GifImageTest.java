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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.awt.ImageComponent;
import vavi.awt.image.gif.NonLzwGifImageSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * GifImageTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class GifImageTest {

    @Test
    public void test() throws Exception {
        Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(Files.newInputStream(Paths.get("src/test/resources/test.gif"))));
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test0() throws Exception {
        // using cdl cause junit stops awt thread suddenly
        CountDownLatch cdl = new CountDownLatch(1);
        main(new String[] { "src/test/resources/test.gif" });
        cdl.await(); // depends on main frame's exit on close
    }

    //----

    /**
     * @param args 0...: gif
     */
    public static void main(String[] args) throws Exception {
System.err.println(args[0]);
        Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(Files.newInputStream(Paths.get(args[0]))));
        ImageComponent component = new ImageComponent();
        JFrame frame = new JFrame();
        frame.addMouseListener(new MouseAdapter() {
            int count = 1;
            @Override
            public void mouseClicked(MouseEvent event) {
                try {
System.err.println(args[count]);
                    Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(Files.newInputStream(Paths.get(args[count]))));
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
        frame.setTitle("NonLzwGif");
        frame.setVisible(true);
        frame.setLocation(300, 300);
        component.setImage(image);
        component.setPreferredSize(new Dimension(image.getWidth(component), image.getHeight(component)));
        frame.getContentPane().add(component);
        frame.pack();
    }
}
