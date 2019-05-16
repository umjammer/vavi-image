/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.util.Iterator;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import org.junit.jupiter.api.Test;

import vavi.imageio.IIOUtil;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * IIOUtilTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/02/08 umjammer initial version <br>
 */
public class IIOUtilTest {

    @Test
    public void test() {
        String p1 = "com.sixlegs.png.iio.PngImageReaderSpi";
        String p2 = "com.sun.imageio.plugins.png.PNGImageReaderSpi";
        IIOUtil.setOrder(ImageReaderSpi.class, p1, p2);

        int p1pos = -1, p2pos = -1;
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        Iterator<ImageReaderSpi> i = iioRegistry.getServiceProviders(ImageReaderSpi.class, true);
        int pos = 0;
        while (i.hasNext()) {
            ImageReaderSpi p = i.next();
            if (p1.equals(p.getClass().getName())) {
                p1pos = pos;
            } else if (p2.equals(p.getClass().getName())) {
                p2pos = pos;
            }
            pos++;
        }
        assertTrue(p1pos < p2pos);
    }
}

/* */
