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
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano port from <br>
 *          0.01 021116 nsano refine <br>
 */
public class PpmImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "0.00";
    private static final String ReaderClassName =
        "vavi.imageio.ppm.PpmImageReader";
    private static final String[] Names = {
        "PPM"
    };
    private static final String[] Suffixes = {
        "ppm"
    };
    private static final String[] mimeTypes = {
        "image/x-ppm", "image/x-pnm"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.ppm.PpmImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "ppm";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.ppm.PpmMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public PpmImageReaderSpi() {
        super(VendorName,
              Version,
              Names,
              Suffixes,
              mimeTypes,
              ReaderClassName,
              new Class[] { ImageInputStream.class, InputStream.class },
              WriterSpiNames,
              SupportsStandardStreamMetadataFormat,
              NativeStreamMetadataFormatName,
              NativeStreamMetadataFormatClassName,
              ExtraStreamMetadataFormatNames,
              ExtraStreamMetadataFormatClassNames,
              SupportsStandardImageMetadataFormat,
              NativeImageMetadataFormatName,
              NativeImageMetadataFormatClassName,
              ExtraImageMetadataFormatNames,
              ExtraImageMetadataFormatClassNames);
    }

    @Override
    public String getDescription(Locale locale) {
        return "PPM Image";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj)
        throws IOException {
        if (obj instanceof ImageInputStream imageinputstream) {
            byte[] bytes = new byte[4];
            try {
                imageinputstream.mark();
                imageinputstream.readFully(bytes);
                imageinputstream.reset();
            } catch (IOException e) {
                return false;
            }
            return bytes[0] == 80 && bytes[1] == 54 && bytes[2] == 10;
        } else {
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new PpmImageReader(this);
    }
}
