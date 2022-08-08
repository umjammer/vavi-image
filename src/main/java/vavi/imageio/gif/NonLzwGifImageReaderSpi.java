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
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 1.10
 */
public class NonLzwGifImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "1.00";
    private static final String ReaderClassName =
        "vavi.imageio.gif.NonLzwGifImageReader";
    private static final String[] Names = {
        "GIF"
    };
    private static final String[] Suffixes = {
        "gif", "GIF"
    };
    private static final String[] mimeTypes = {
        "image/gif"
    };
    static final String[] WriterSpiNames = {
        /* "vavi.imageio.NonLzwGifImageWriterSpi" */
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    static final String NativeImageMetadataFormatName = "javax_imageio_gif_image_1.0";
    private static final String NativeImageMetadataFormatClassName =
        /* "vavi.imageio.NonLzwGifMetaData" */ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public NonLzwGifImageReaderSpi() {
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

    /** @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale) */
    public String getDescription(Locale locale) {
        return "Non LZW GIF Decoder";
    }

    /** @see javax.imageio.spi.ImageReaderSpi#canDecodeInput(java.lang.Object) */
    public boolean canDecodeInput(Object source) throws IOException {
        if (source instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) source;
            byte[] bytes = new byte[4];
            try {
                is.mark();
                is.readFully(bytes);
                is.reset();
            } catch (IOException e) {
Debug.printStackTrace(e);
                return false;
            }
            return bytes[0] == 'G' && // 識別子 = "GIF8" + ("7a" or "9a")
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
