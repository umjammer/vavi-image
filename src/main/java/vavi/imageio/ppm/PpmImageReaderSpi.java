/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ppm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


/**
 * PpmImageReaderSpi.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano port from <br>
 *          0.01 021116 nsano refine <br>
 */
public class PpmImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "http://www.vavisoft.com";
    private static final String version = "0.00";
    private static final String readerClassName =
        "vavi.imageio.ppm.PpmImageReader";
    private static final String names[] = {
        "PPM"
    };
    private static final String suffixes[] = {
        "ppm"
    };
    private static final String mimeTypes[] = {
        "image/x-ppm", "image/x-pnm"
    };
    static final String writerSpiNames[] = {
        /*"vavi.imageio.ppm.PpmImageWriterSpi"*/
    };
    private static final boolean supportsStandardStreamMetadataFormat = false;
    private static final String nativeStreamMetadataFormatName = null;
    private static final String nativeStreamMetadataFormatClassName = null;
    private static final String extraStreamMetadataFormatNames[] = null;
    private static final String extraStreamMetadataFormatClassNames[] = null;
    private static final boolean supportsStandardImageMetadataFormat = false;
    private static final String nativeImageMetadataFormatName = "ppm";
    private static final String nativeImageMetadataFormatClassName =
        /*"vavi.imageio.ppm.PpmMetaData"*/ null;
    private static final String extraImageMetadataFormatNames[] = null;
    private static final String extraImageMetadataFormatClassNames[] = null;

    /** */
    public PpmImageReaderSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              mimeTypes,
              readerClassName,
              new Class[] { ImageInputStream.class, InputStream.class },
              writerSpiNames,
              supportsStandardStreamMetadataFormat,
              nativeStreamMetadataFormatName,
              nativeStreamMetadataFormatClassName,
              extraStreamMetadataFormatNames,
              extraStreamMetadataFormatClassNames,
              supportsStandardImageMetadataFormat,
              nativeImageMetadataFormatName,
              nativeImageMetadataFormatClassName,
              extraImageMetadataFormatNames,
              extraImageMetadataFormatClassNames);
    }

    /* */
    public String getDescription(Locale locale) {
        return "PPM Image";
    }

    /* TODO InputStream */
    public boolean canDecodeInput(Object obj)
        throws IOException {
        if (obj instanceof ImageInputStream) {
            ImageInputStream imageinputstream = (ImageInputStream) obj;
            byte abyte0[] = new byte[4];
            try {
                imageinputstream.mark();
                imageinputstream.readFully(abyte0);
                imageinputstream.reset();
            } catch (IOException e) {
                return false;
            }
            return abyte0[0] == 80 && abyte0[1] == 54 && abyte0[1] == 10;
        } else {
            return false;
        }
    }

    /* */
    public ImageReader createReaderInstance(Object obj) {
        return new PpmImageReader(this);
    }
}

/* */
