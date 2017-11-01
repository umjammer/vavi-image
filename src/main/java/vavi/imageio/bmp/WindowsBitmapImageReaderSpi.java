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

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "0.00";
    private static final String ReaderClassName =
        "vavi.imageio.bmp.WindowsBitmapImageReader";
    private static final String[] Names = {
        "BMP"
    };
    private static final String[] Suffixes = {
        "bmp", "BMP"
    };
    private static final String[] mimeTypes = {
        "image/x-bmp"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.bmp.WindowsBitmapImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "bmp";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.bmp.WindowsBitmapMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;
    
    /** */
    public WindowsBitmapImageReaderSpi() {
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
