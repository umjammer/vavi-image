/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import vavi.awt.image.quantization.ImageMagickQuantizeOp;


/**
 * TestQuantize3. (ImageMagik)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 061012 nsano initial version <br>
 */
public class TestQuantize3 {

    /**
     * @param args in out colors...
     */
    public static void main(String[] args) throws Exception {

        ImageWriter iw = ImageIO.getImageWritersByFormatName("JPEG").next(); // sloppy?
        float quality = 0.75f;

        ImageFrame originalFrame = new ImageFrame();
        File file = new File(args[0]);
System.err.println("original size: " + file.length());
        BufferedImage image = ImageIO.read(file);
        originalFrame.setImage(image);
        originalFrame.setTitle("original");

        int x = 100;
        int y = 100;
        originalFrame.setLocation(x, y);

        for (int i = 2; i < args.length; ++i) {
            x += 20;
            y += 20;
            BufferedImageOp filter = new ImageMagickQuantizeOp(Integer.parseInt(args[i]));
System.err.println(image.getType());
long tm = System.currentTimeMillis();
            BufferedImage filteredImage = filter.filter(image, null);
tm = System.currentTimeMillis() - tm;
            ImageFrame filteredFrame = new ImageFrame();
            filteredFrame.setImage(filteredImage);

            filteredFrame.setTitle(args[i] + " colors (ImageMagik)");
            filteredFrame.setLocation(x, y);

            //
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            iw.setOutput(ios);

            ImageWriteParam iwp = iw.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
            iw.write(null, new IIOImage(filteredImage, null, null), iwp);
System.err.println("reduced to " + args[i] + " in " + tm + "ms, size " + baos.size() + " using " + filter.getClass().getName());

//            ImageIO.write(filteredImage, "GIF", new FileOutputStream(new File(args[1] + "_" + args[i] + ".gif")));
        }

        iw.dispose();
    }
}
