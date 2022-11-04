/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.logging.Level;
import javax.imageio.stream.ImageInputStream;

import vavi.io.SeekableDataInput;
import vavi.util.Debug;


/**
 * SeekableDataInputImageInputStream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-25 nsano initial version <br>
 */
public class SeekableDataInputImageInputStream implements SeekableDataInput<ImageInputStream> {

    private ImageInputStream iis;

    public SeekableDataInputImageInputStream(ImageInputStream iis, ByteOrder byteOrder) {
        this.iis = iis;
        this.iis.setByteOrder(byteOrder);
        this.iis.mark();
    }

    @Override
    public ImageInputStream origin() {
        return iis;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        iis.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        int r = iis.read(b, off, len);
        if (r < 0) { // TODO maybe different from spec.
            throw new EOFException();
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return iis.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return iis.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return iis.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return iis.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return iis.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return iis.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return iis.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return iis.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return iis.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return iis.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return iis.readDouble();
    }

    @Override
    public String readLine() throws IOException {
        return iis.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return iis.readUTF();
    }

    @Override
    public void position(long l) throws IOException {
        iis.reset();
        iis.seek(l);
Debug.printf(Level.FINER, "%d, %d", l, position());
    }

    @Override
    public long position() throws IOException {
        return iis.getStreamPosition();
    }
}
