/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;

import javax.imageio.ImageIO;

import vavi.awt.image.quantize.NeuralNetQuantizeOp;


/**
 * TestQuantize5. GIF (NeuralNet)
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070628 nsano initial version <br>
 */
public class TestQuantize5 {

    /** */
    public static void main(String args[]) throws Exception {

        ImageFrame originalFrame = new ImageFrame();
        File file = new File(args[0]);
System.err.println("original size: " + file.length());
        BufferedImage image = ImageIO.read(file);
        originalFrame.setImage(image);
        originalFrame.setTitle("original");

        int x = 100;
        int y = 100;
        originalFrame.setLocation(x, y);

        for (int i = 2; i < args.length; i++) {
            x += 20;
            y += 20;
            BufferedImageOp filter = new NeuralNetQuantizeOp(Integer.parseInt(args[i]));
long tm = System.currentTimeMillis();
            BufferedImage filteredImage = filter.filter(image, null);
tm = System.currentTimeMillis() - tm;
            ImageFrame filteredFrame = new ImageFrame();
            filteredFrame.setImage(filteredImage);
            
            filteredFrame.setTitle(args[i] + " colors (NeuralNet)");
            filteredFrame.setLocation(x, y);

            //
            File gif = new File(args[1] + "_" + args[i] + ".gif");
            ImageIO.write(filteredImage, "GIF", gif);
System.out.println("reduced to " + args[i] + " in " + tm + "ms, size " + gif.length() + " using " + filter.getClass().getName());
        }
    }
}
