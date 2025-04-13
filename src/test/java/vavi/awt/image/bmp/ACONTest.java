/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import vavi.awt.image.ico.ACON;
import vavi.util.Debug;


/**
 * ACONTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/10 umjammer initial version <br>
 */
class ACONTest {

    @Test
    void test() throws Exception {
        InputStream is = new BufferedInputStream(ACON.class.getResourceAsStream("/test.ico"));
        ACON ani = ACON.readFrom(is, ACON.class);
Debug.println("ACON: " + ani);
    }

    // ----

    /** */
    public static void main(String[] args) throws Exception {
        InputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(args[0])));
        ACON ani = ACON.readFrom(is, ACON.class);
Debug.println("ACON: " + ani);
    }
}
