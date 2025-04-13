/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.pic;

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

import vavi.imageio.am88.ArtMasterImageReaderSpi;

import static java.lang.System.getLogger;


/**
 * PicImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 230323 nsano initial version <br>
 */
public class PicImageReaderSpi extends ImageReaderSpi {

    private static final Logger logger = getLogger(PicImageReaderSpi.class.getName());

    static {
        try {
            try (InputStream is = PicImageReaderSpi.class.getResourceAsStream("/META-INF/maven/vavi/vavi-image/pom.properties")) {
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
        "vavi.imageio.mag.PicImageReader";
    private static final String[] Names = {
        "PIC", "pic"
    };
    private static final String[] Suffixes = {
        "pic", "PIC"
    };
    private static final String[] mimeTypes = {
        "image/x-pic"
    };
    static final String[] WriterSpiNames = {
        /* "vavi.imageio.pic.PicImageWriterSpi" */
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "pic";
    private static final String NativeImageMetadataFormatClassName =
        /* "vavi.imageio.pic.PicImageMetaData" */ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public PicImageReaderSpi() {
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
        return "PIC Yanagisawa Image";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        byte[] header = "PIC".getBytes();

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
//logger.log(Level.TRACE, StingUtil.getDump(bytes));
            return Arrays.equals(header, bytes);
        } else {
logger.log(Level.DEBUG, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new PicImageReader(this);
    }
}
