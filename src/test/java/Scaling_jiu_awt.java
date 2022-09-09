/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.geometry.Resample;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.gui.awt.RGBA;


/**
 * Lanczos (jiu).
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 061010 nsano initial version <br>
 */
public class Scaling_jiu_awt {

    public static void main(String[] args) throws Exception {

System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));
        int w = image.getWidth();
        int h = image.getHeight();
System.err.println(w + ", " + h);

        // 1. jiu
        RGB24Image jiuImage = ImageCreator.convertImageToRGB24Image(image);

        Resample resample = new Resample();
        resample.setInputImage(jiuImage);
        resample.setSize(w / 3, h / 3);
        resample.setFilter(Resample.FILTER_TYPE_LANCZOS3);
long t = System.currentTimeMillis();
        resample.process();
System.err.println("jiu filter: " + (System.currentTimeMillis() - t) + " ms");
        PixelImage scaledImage = resample.getOutputImage();

        Image awtImage = ImageCreator.convertToAwtImage(scaledImage, RGBA.DEFAULT_ALPHA);

System.err.println(awtImage.getWidth(null) + ", " + awtImage.getHeight(null));
        ImageFrame frame = new ImageFrame();
        frame.setImage(awtImage);
        frame.setTitle("Lanczos3 (jiu)");
        frame.setVisible(true);

        // 2. awt
t = System.currentTimeMillis();
        Image image2 = image.getScaledInstance(w / 3, h / 3, Image.SCALE_AREA_AVERAGING);
System.err.println("awt filter: " + (System.currentTimeMillis() - t) + " ms");
        ImageFrame frame2 = new ImageFrame();
        frame2.setImage(image2);
        frame2.setTitle("Area Averaging (awt)");
        frame2.setVisible(true);
    }
}

/* */
