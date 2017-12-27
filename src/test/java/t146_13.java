/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * font display.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 070523 nsano initial version <br>
 */
public class t146_13 {

    public static void main(String[] args) throws Exception {
        String imageFilename = args[0];
System.err.println("image: " + args[0]);
        String text = args[1];
        String fontName = args[2];
        int point = 60;
        int ratio = 12;

        final BufferedImage image = ImageIO.read(new File(imageFilename));
System.err.println(image);

        Font font = new Font(fontName, Font.PLAIN, point);

        float stroke = point / (float) ratio;

        Graphics2D graphics = Graphics2D.class.cast(image.getGraphics());
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontRenderContext frc = graphics.getFontRenderContext();

        float y = image.getHeight() * 0.2f;

        StringTokenizer st = new StringTokenizer(text, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            AttributedString as = new AttributedString(line);
            as.addAttribute(TextAttribute.FONT, font, 0, line.length());
            AttributedCharacterIterator aci = as.getIterator();

            TextLayout tl = new TextLayout(aci, frc);
            float sw = (float) tl.getBounds().getWidth();
//          float sh = (float) tl.getBounds().getHeight();
            y += tl.getAscent();
            Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(image.getWidth() / 2 - sw / 2, y));
            graphics.setColor(Color.black);
            graphics.setStroke(new BasicStroke(stroke));
            graphics.draw(shape);
            graphics.setColor(Color.pink);
            graphics.fill(shape);

            y += tl.getDescent() + tl.getLeading();
        }

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
