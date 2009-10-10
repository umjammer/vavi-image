/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.bmp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


/**
 * WindowsBitmapImageReaderSpi.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano initial version <br>
 *          0.01 021116 nsano refine <br>
 */
public class WindowsBitmapImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "http://www.vavisoft.com";
    private static final String version = "0.00";
    private static final String readerClassName =
        "vavi.imageio.bmp.WindowsBitmapImageReader";
    private static final String names[] = {
        "BMP"
    };
    private static final String suffixes[] = {
        "bmp", "BMP"
    };
    private static final String mimeTypes[] = {
        "image/x-bmp"
    };
    static final String writerSpiNames[] = {
        /*"vavi.imageio.bmp.WindowsBitmapImageWriterSpi"*/
    };
    private static final boolean supportsStandardStreamMetadataFormat = false;
    private static final String nativeStreamMetadataFormatName = null;
    private static final String nativeStreamMetadataFormatClassName = null;
    private static final String extraStreamMetadataFormatNames[] = null;
    private static final String extraStreamMetadataFormatClassNames[] = null;
    private static final boolean supportsStandardImageMetadataFormat = false;
    private static final String nativeImageMetadataFormatName = "bmp";
    private static final String nativeImageMetadataFormatClassName =
        /*"vavi.imageio.bmp.WindowsBitmapMetaData"*/ null;
    private static final String extraImageMetadataFormatNames[] = null;
    private static final String extraImageMetadataFormatClassNames[] = null;
    
    /** */
    public WindowsBitmapImageReaderSpi() {
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
        return "Windows Bitmap Image";
    }
    
    /* TODO InputStream */
    public boolean canDecodeInput(Object obj)
        throws IOException {
        if (obj instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) obj;
            byte bytes[] = new byte[2];
            try {
                is.mark();
                is.readFully(bytes);
                is.reset();
            } catch (IOException e) {
                return false;
            }
            return bytes[0] == 'B' && bytes[1] == 'M';
        } else {
            return false;
        }
    }
    
    /* */
    public ImageReader createReaderInstance(Object obj) {
        return new WindowsBitmapImageReader(this);
    }
}

/* */
