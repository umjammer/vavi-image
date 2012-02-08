/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;


/**
 * ImageIO display.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 021117 nsano initial version <br>
 */
public class t146_3 {

    public static void main(String[] args) throws Exception {
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("PNG");
        System.err.println("-- PNG reader --");
        while (irs.hasNext()) {
            System.err.println(irs.next().getClass().getName());
        }
    }
}

/* */
