/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ppm;

import java.io.IOException;
import java.io.InputStream;


/**
 * Ppm.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano initial version <br>
 *          0.01 021116 nsano refine <br>
 */
public class Ppm {

    public static final int BINARY_PBM = 4;
    public static final int BINARY_PGM = 5;
    public static final int BINARY_PPM = 6;
    private int imageType;
    private int width;
    private int height;
    @SuppressWarnings("unused")
    private int colorType;
    private boolean gotHeader;

    public Ppm() {
        gotHeader = false;
    }

    public int getImageType() {
        return imageType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void readHeader(InputStream is) throws IOException {
        if (gotHeader)
            return;

        byte[] signature = new byte[2];
        is.read(signature, 0, 2);

        if (signature[0] == 80) {
            imageType = signature[1] - 48;
            if (imageType < BINARY_PBM || imageType > BINARY_PPM) {
                throw new IOException("Bad PBM/PGM/PPM signature!");
            }
        } else {
            throw new IOException("Bad PBM/PGM/PPM signature!");
        }

        width = readInt(is);
        height = readInt(is);
        if (imageType != BINARY_PBM) {
            colorType = readInt(is);
        }

        gotHeader = true;
    }

    /** */
    private static char readChar(InputStream is) throws IOException {
        char c = (char) is.read();
        if (c == '#') {
            do {
                c = (char) is.read();
            } while(c != '\n' && c != '\r');
        }
        return c;
    }

    /** */
    private static char readNonwhiteChar(InputStream is) throws IOException {

        char c;
        do {
            c = readChar(is);
        } while (c == ' ' || c == '\t' || c == '\n' || c == '\r');
        return c;
    }

    /** */
    private static int readInt(InputStream is) throws IOException {

        char c = readNonwhiteChar(is);
        if (c < '0' || c > '9') {
            throw new IOException("junk in file where integer should be");
        }
        int i = 0;
        do {
            i = (i * 10 + c) - 48;
            c = readChar(is);
        } while (c >= '0' && c <= '9');

        return i;
    }
}
