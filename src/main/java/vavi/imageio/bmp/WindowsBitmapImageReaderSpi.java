/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.bmp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


/**
 * WindowsBitmapImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano initial version <br>
 *          0.01 021116 nsano refine <br>
 */
public class WindowsBitmapImageReaderSpi extends ImageReaderSpi {

    static {
        try {
            try (InputStream is = WindowsBitmapImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image/pom.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    Version = props.getProperty("version", "undefined in pom.properties");
                } else {
                    Version = System.getProperty("vavi.test.version", "undefined");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version;
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

    @Override
    public String getDescription(Locale locale) {
        return "Windows Bitmap Image";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj)
        throws IOException {
        if (obj instanceof ImageInputStream is) {
            byte[] bytes = new byte[2];
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

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new WindowsBitmapImageReader(this);
    }
}
