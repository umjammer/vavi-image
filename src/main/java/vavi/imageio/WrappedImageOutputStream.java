/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;


/**
 * WrappedImageOutputStream.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070628 nsano initial version <br>
 */
public class WrappedImageOutputStream extends OutputStream {
    /** */
    private ImageOutputStream os;

    /** */
    public WrappedImageOutputStream(ImageOutputStream os) {
        this.os = os;
    }

    /* */
    public void close() throws IOException {
        os.close();
    }

    /* */
    public void flush() throws IOException {
        os.flush();
    }

    /* */
    public void write(int b) throws IOException {
        os.write(b);
    }
}

/* */
