/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.mag;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import static java.lang.System.getLogger;


/**
 * MagImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 221025 nsano initial version <br>
 */
public class MagImageReaderSpi extends ImageReaderSpi {

    private static final Logger logger = getLogger(MagImageReaderSpi.class.getName());

    static {
        try {
            try (InputStream is = MagImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image/pom.properties")) {
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

    private static final String VendorName = "http://www.vavi.com";
    private static final String Version;
    private static final String ReaderClassName =
        "vavi.imageio.mag.MagImageReader";
    private static final String[] Names = {
        "MAG", "mag"
    };
    private static final String[] Suffixes = {
        "mag", "MAG"
    };
    private static final String[] mimeTypes = {
        "image/x-mag"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.mag.MagImageWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "mag";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.mag.MagImageMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public MagImageReaderSpi() {
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
        return "MAKI-chan Graphic loader";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        byte[] header = "MAKI02  ".getBytes();

        if (obj instanceof ImageInputStream is) {
            byte[] bytes = new byte[header.length];
            try {
                is.mark();
                is.readFully(bytes);
                is.reset();
            } catch (IOException e) {
logger.log(Level.ERROR, e.getMessage(), e);
                return false;
            }
//logger.log(Level.TRACE, StringUtil.getDump(bytes);
            return Arrays.equals(header, bytes);
        } else {
logger.log(Level.DEBUG, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new MagImageReader(this);
    }
}
