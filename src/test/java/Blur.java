/*
 * Copyright 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 *
 * -Redistributions of source code must retain the above copyright  
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright 
 *  notice, this list of conditions and the following disclaimer in 
 *  the documentation and/or other materials provided with the 
 *  distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility.
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.awt.image.AbstractBufferedImageOp;


/**
 * Blur.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/18 umjammer initial version <br>
 */
public class Blur {

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(Objects.requireNonNull(Blur.class.getResourceAsStream("erika.jpg")));
        BufferedImage bluredImage = new BlurOp().filter(image, null);

        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                int w = image.getWidth();

                g.drawImage(image, 0, 0, this);
                g.drawImage(bluredImage, w, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(image.getWidth() * 2, image.getHeight()));

        JFrame fame = new JFrame("Blur");
        fame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fame.getContentPane().add(panel);
        fame.pack();
        fame.setVisible(true);
    }
}

class BlurOp extends AbstractBufferedImageOp {

    private static final float[] elements = {
        .1111f, .1111f, .1111f,
        .1111f, .1111f, .1111f,
        .1111f, .1111f, .1111f
    };

    /* */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return cop.filter(src, null);
    }
}
