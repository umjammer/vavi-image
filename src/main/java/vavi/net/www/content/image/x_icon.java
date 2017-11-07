/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.content.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.URLConnection;

import vavi.awt.image.bmp.WindowsIconImageSource;


/**
 * Content Handler for Windows Icon.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970928 nsano initial version <br>
 *          1.00 010814 nsano repackage <br>
 */
public class x_icon extends ContentHandler {

    public Object getContent(URLConnection connection) throws IOException {

        InputStream in = connection.getInputStream();
        return new WindowsIconImageSource(in);
    }

//    public x_icon() {}
}

/* */
