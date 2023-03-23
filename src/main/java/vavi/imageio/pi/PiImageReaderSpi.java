/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.pi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * PiImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 230323 nsano initial version <br>
 */
public class PiImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavi.com";
    private static final String Version = "1.0.11";
    private static final String ReaderClassName =
        "vavi.imageio.pi.PiImageReader";
    private static final String[] Names = {
        "PI", "pi"
    };
    private static final String[] Suffixes = {
        "pi", "PI"
    };
    private static final String[] mimeTypes = {
        "image/x-pi"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.pi.PiImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "pi";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.pi.PiImageMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public PiImageReaderSpi() {
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
        return "Pi Yanagisawa Image";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        byte[] header = "Pi".getBytes();

        if (obj instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) obj;
            byte[] bytes = new byte[header.length];
            try {
                is.mark();
                is.readFully(bytes);
                is.reset();
            } catch (IOException e) {
Debug.println(e);
                return false;
            }
//Debug.dump(bytes);
            return Arrays.equals(header, bytes);
        } else {
Debug.println(Level.FINE, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new PiImageReader(this);
    }
}

/* */
