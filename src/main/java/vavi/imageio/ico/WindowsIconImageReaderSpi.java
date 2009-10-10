/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ico;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * WindowsIconImageReaderSpi.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class WindowsIconImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "http://www.vavisoft.com";
    private static final String version = "0.00";
    private static final String readerClassName =
        "vavi.imageio.ico.WindowsIconImageReader";
    private static final String names[] = {
        "ICO"
    };
    private static final String suffixes[] = {
        "ico", "ICO"
    };
    private static final String mimeTypes[] = {
        "image/x-icon"
    };
    static final String writerSpiNames[] = {
        /*"vavi.imageio.ico.WindowsIconWriterSpi"*/
    };
    private static final boolean supportsStandardStreamMetadataFormat = false;
    private static final String nativeStreamMetadataFormatName = null;
    private static final String nativeStreamMetadataFormatClassName = null;
    private static final String extraStreamMetadataFormatNames[] = null;
    private static final String extraStreamMetadataFormatClassNames[] = null;
    private static final boolean supportsStandardImageMetadataFormat = false;
    private static final String nativeImageMetadataFormatName = "ico";
    private static final String nativeImageMetadataFormatClassName =
        /*"vavi.imageio.ico.WindowsIconMetaData"*/ null;
    private static final String extraImageMetadataFormatNames[] = null;
    private static final String extraImageMetadataFormatClassNames[] = null;
    
    /** */
    public WindowsIconImageReaderSpi() {
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
        return "Windows Icon Image";
    }
    
    /* TODO InputStream */
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) obj;
            int type;
            try {
                is.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                is.mark();
                is.skipBytes(2);
                type = is.readShort();
                is.reset();
            } catch (IOException e) {
Debug.println(e);
                return false;
            }
//Debug.println(type);
            return type == 1;
        } else {
Debug.println(obj);
            return false;
        }
    }
    
    /* */
    public ImageReader createReaderInstance(Object obj) {
        return new WindowsIconImageReader(this);
    }
}

/* */
