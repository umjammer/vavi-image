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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.awt.ImageComponent;
import vavi.awt.image.gif.NonLzwGifImageSource;
import vavi.util.Debug;

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
        Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(Files.newInputStream(Path.of("src/test/resources/test.gif"))));
        assertNotNull(image);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test0() throws Exception {
        main(new String[] { "src/test/resources/test.gif" });

        // using cdl cause junit stops awt thread suddenly
    }

    //----

    /**
     * @param args 0...: gif
     */
    public static void main(String[] args) throws Exception {
Debug.println(args[0]);
        CountDownLatch cdl = new CountDownLatch(1);

        Image image = Toolkit.getDefaultToolkit().createImage(new NonLzwGifImageSource(Files.newInputStream(Paths.get(args[0]))));
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        ImageComponent component = new ImageComponent();
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
        frame.setTitle("NonLzwGif");
        frame.setLocation(300, 300);
        component.setImage(image);
        component.setPreferredSize(new Dimension(image.getWidth(component), image.getHeight(component)));
        frame.getContentPane().add(component);
        frame.pack();
        frame.setVisible(true);

        cdl.await();
    }
}
