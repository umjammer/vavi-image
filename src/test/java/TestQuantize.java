/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import vavi.awt.image.quantization.ImageMagickQuantizer;
import vavi.imageio.ImageConverter;


/**
 * Test color quantization of an image.
 *
 * <p><b>Usage: Test [image file] [# colors] [# colors] ...</b><p>
 *
 * For example:
 *
 *   <pre>java quantize.TestQuantize gub.jpg 100 50 20 10</pre>
 *
 * will display gub.jpg with 100, 50, 20, and 10 colors.
 *
 * @version 0.90 19 Sep 2000
 * @author <a href="http://www.gurge.com/amd/">Adam Doppelt</a>
 */
public class TestQuantize {

    /**
     * Snag the pixels from an image.
     */
    static int[][] getPixels(Image image) throws IOException {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        int[] pix = new int[w * h];
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, w, h, pix, 0, w);

        try {
            if (!grabber.grabPixels()) {
                throw new IOException("Grabber returned false: " + grabber.status());
            }
        } catch (InterruptedException ignore) {
        }

        int[][] pixels = new int[w][h];
        for (int x = w; x-- > 0; ) {
            for (int y = h; y-- > 0; ) {
                pixels[x][y] = pix[y * w + x];
            }
        }

        return pixels;
    }

    /**
     * @param args 0: jpeg
     */
    public static void main(String[] args) throws Exception {
        ImageConverter converter = ImageConverter.getInstance();
        ImageWriter iw = ImageIO.getImageWritersByFormatName("JPEG").next(); // sloppy?
        float quality = 0.75f;

        ImageFrame originalFrame = new ImageFrame();
        originalFrame.setImage(new File(args[0]));
        originalFrame.setTitle("original");

        int x = 100;
        int y = 100;
        originalFrame.setLocation(x, y);

        for (int i = 1; i < args.length; ++i) {
            x += 20;
            y += 20;
            int[][] pixels = getPixels(originalFrame.getImage());
            long tm = System.currentTimeMillis();

            // quant
            int[] palette = ImageMagickQuantizer.quantizeImage(pixels, Integer.parseInt(args[i]));
            tm = System.currentTimeMillis() - tm;
System.out.println("reduced to " + args[i] + " in " + tm + "ms using ImageMagickQuantizer direct");
            ImageFrame filteredFrame = new ImageFrame();
            filteredFrame.setImage(palette, pixels);

            filteredFrame.setTitle(args[i] + " colors");
            filteredFrame.setLocation(x, y);

            //
            converter.setColorModelType(BufferedImage.TYPE_3BYTE_BGR);
            BufferedImage image = converter.toBufferedImage(filteredFrame.getImage());

            //
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            iw.setOutput(ios);

            ImageWriteParam iwp = iw.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
            iw.write(null, new IIOImage(image, null, null), iwp);
System.out.println("size: " + baos.size());
        }

        iw.dispose();
    }
}
