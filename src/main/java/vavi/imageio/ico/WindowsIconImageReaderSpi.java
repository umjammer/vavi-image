/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ico;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.Properties;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import static java.lang.System.getLogger;


/**
 * WindowsIconImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class WindowsIconImageReaderSpi extends ImageReaderSpi {

    private static final Logger logger = getLogger(WindowsIconImageReaderSpi.class.getName());

    static {
        try {
            try (InputStream is = WindowsIconImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image/pom.properties")) {
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
        "vavi.imageio.ico.WindowsIconImageReader";
    private static final String[] Names = {
        "ICO"
    };
    private static final String[] Suffixes = {
        "ico", "ICO"
    };
    private static final String[] mimeTypes = {
        "image/x-icon"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.ico.WindowsIconWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "ico";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.ico.WindowsIconMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public WindowsIconImageReaderSpi() {
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
        return "Windows Icon Image";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream is) {
            int type, count;
            byte[] bytes = new byte[2];
            try {
                is.mark();
                ByteOrder byteOrder = is.getByteOrder();
                is.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                is.readFully(bytes);
                type = is.readShort();
                count = is.readShort();
                is.setByteOrder(byteOrder);
                is.reset();
            } catch (IOException e) {
logger.log(Level.ERROR, e.getMessage(), e);
                return false;
            }
//logger.log(Level.TRACE, type + ", " + count + ", " + (bytes[0] == 0 && bytes[1] == 0 && type == 1 && count > 0));
            return bytes[0] == 0 && bytes[1] == 0 && type == 1 && count > 0;
        } else {
logger.log(Level.DEBUG, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new WindowsIconImageReader(this);
    }
}
