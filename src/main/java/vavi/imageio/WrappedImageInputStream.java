/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;


/**
 * WrappedImageInputStream.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 021115 nsano initial version <br>
 */
public class WrappedImageInputStream extends InputStream {
    /** */
    private ImageInputStream is;

    /** */
    public WrappedImageInputStream(ImageInputStream is) {
        this.is = is;
    }

    /* */
    public int available() throws IOException {
        return (int) (is.length() - is.getStreamPosition());
    }

    /* */
    public void close() throws IOException {
        is.close();
    }

    /* TODO */
    public void mark(int readlimit) {
        is.mark();
    }

    /* */
    public boolean markSupported() {
        return true;
    }

    /* */
    public int read() throws IOException {
        return is.read();
    }

    /* */
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    /* */
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    /* */
    public void reset() throws IOException {
        is.reset();
    }

    /* */
    public long skip(long n) throws IOException {
        return is.skipBytes(n);
    }
}

/* */
