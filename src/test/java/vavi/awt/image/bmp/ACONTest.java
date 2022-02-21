/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import vavi.awt.image.ico.ACON;

import static org.junit.jupiter.api.Assertions.*;


/**
 * ACONTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/10 umjammer initial version <br>
 */
class ACONTest {

    @Test
    void test() {
        fail("Not yet implemented");
    }

    // ----

    /** */
    public static void main(String[] args) throws Exception {
        InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
        ACON ani = (ACON) ACON.readFrom(is);
System.err.println("ACON: " + ani);
    }
}

/* */
