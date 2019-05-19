/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.Graphics;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import vavi.imageio.IIOUtil;


/**
 * NonLzwGifImageReaderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2009/06/21 nsano initial version <br>
 */
public class NonLzwGifImageReaderTest {

    @Test
    public void test() throws Exception {
        main(new String[] { "tmp/qr.gif" });
    }

    //----

    /** */
    public static void main(final String[] args) throws IOException {
System.err.println(args[0]);
        ImageReader ir = IIOUtil.getImageReader("GIF", NonLzwGifImageReader.class.getName());
//System.err.println("provider: " + StringUtil.paramString(ir.getOriginatingProvider().getInputTypes()));
        ir.setInput(new FileInputStream(args[0]));
        final Image image = ir.read(0);
//        final Image image = ImageIO.read(new FileInputStream(args[0]));

        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
