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

import vavi.awt.image.bmp.WindowsBitmapImageSource;


/**
 * Content Handler for Windows Bitmap.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970928 nsano initial version <br>
 *          1.00 010814 nsano repackage <br>
 */
public class x_bmp extends ContentHandler {

    @Override
    public Object getContent(URLConnection connection) throws IOException {
        InputStream in = connection.getInputStream();
        return new WindowsBitmapImageSource(in);
    }
}
