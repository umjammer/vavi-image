/*
 * Copyright (c) 2004  by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * NonLzwGifDecoderSPI.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.10
 */
public class NonLzwGifImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "http://www.vavisoft.com";
    private static final String version = "1.00";
    private static final String readerClassName =
        "vavi.imageio.NonLzwGifImageReader";
    private static final String names[] = {
        "GIF"
    };
    private static final String suffixes[] = {
        "gif", "GIF"
    };
    private static final String mimeTypes[] = {
        "image/gif"
    };
    static final String writerSpiNames[] = {
        /* "vavi.imageio.NonLzwGifImageWriterSpi" */
    };
    private static final boolean supportsStandardStreamMetadataFormat = false;
    private static final String nativeStreamMetadataFormatName = null;
    private static final String nativeStreamMetadataFormatClassName = null;
    private static final String extraStreamMetadataFormatNames[] = null;
    private static final String extraStreamMetadataFormatClassNames[] = null;
    private static final boolean supportsStandardImageMetadataFormat = false;
    private static final String nativeImageMetadataFormatName = "gif";
    private static final String nativeImageMetadataFormatClassName =
        /* "vavi.imageio.NonLzwGifMetaData" */ null;
    private static final String extraImageMetadataFormatNames[] = null;
    private static final String extraImageMetadataFormatClassNames[] = null;

    /** */
    public NonLzwGifImageReaderSpi() {
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

    /** @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale) */
    public String getDescription(Locale locale) {
        return "Non LZW GIF Decoder";
    }

    /** @see javax.imageio.spi.ImageReaderSpi#canDecodeInput(java.lang.Object) */
    public boolean canDecodeInput(Object source) throws IOException {
        if (source instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) source;
            byte bytes[] = new byte[4];
            try {
                is.mark();
                is.readFully(bytes);
                is.reset();
            } catch (IOException e) {
Debug.printStackTrace(e);
                return false;
            }
            return bytes[0] == 'G' && // Ž¯•ÊŽq = "GIF8" + ("7a" or "9a")
    	           bytes[1] == 'I' &&
    	           bytes[2] == 'F' &&
    	           bytes[3] == '8';
        } else {
Debug.println("unsupported input: " + source);
            return false;
        }
    }

    /** @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object) */
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new NonLzwGifImageReader(this);
    }
}

/* */
