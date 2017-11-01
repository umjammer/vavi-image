/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageWriter;

import vavi.util.Debug;


/**
 * ImageIO conversion.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 021117 nsano initial version <br>
 */
public class t146_2 {

    /**
     * java t2 type from to
     */
    public static void main(String[] args) throws Exception {
        String[] writerMIMETypes = ImageIO.getWriterMIMETypes();
        String[] readerMIMETypes = ImageIO.getReaderMIMETypes();

        System.err.println("-- reader --");
        for (String readerMIMEType : readerMIMETypes) {
            System.err.println(readerMIMEType);
        }
        System.err.println("-- writer --");
        for (String writerMIMEType : writerMIMETypes) {
            System.err.println(writerMIMEType);
        }

        for (String readerMIMEType : readerMIMETypes) {
//Debug.println("reader: " + readerMIMEType);
            Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByMIMEType(readerMIMEType);
            while (imageReaders.hasNext()) {
                ImageReader imageReader = imageReaders.next(); 

                for (String writerMIMEType : writerMIMETypes) {
//Debug.println("writer: " + writerMIMEType);
                    Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(writerMIMEType);
                    while (imageWriters.hasNext()) {
                        ImageWriter imageWriter = imageWriters.next(); 

Debug.println("reader: " + readerMIMEType + ", writer: " + writerMIMEType);
                        Iterator<ImageTranscoder> imageTranscoders = ImageIO.getImageTranscoders(imageReader, imageWriter);
                        while (imageTranscoders.hasNext()) {
                            ImageTranscoder imageTranscoder = imageTranscoders.next(); 

Debug.println(imageReader + ", " + imageWriter + ", " + imageTranscoder);
                        }
                    }
                }
            }
        }

        BufferedImage image = ImageIO.read(new File(args[1]));
        ImageIO.write(image, args[0], new File(args[2]));
    }
}

/* */
