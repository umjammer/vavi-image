/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.am88;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.util.Debug;


/**
 * ArtMasterImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class ArtMasterImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavi.com";
    private static final String Version = "1.0.10";
    private static final String ReaderClassName =
        "vavi.imageio.am88.ArtMasterImageReader";
    private static final String[] Names = {
        "AM88"
    };
    private static final String[] Suffixes = {
        "img", "IMG"
    };
    private static final String[] mimeTypes = {
        "image/x-am88"
    };
    static final String[] WriterSpiNames = {
        /*"vavi.imageio.ico.ArtMasterWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "am88";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.am88.ArtMasterMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public ArtMasterImageReaderSpi() {
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
        return "Art Master 88 Image";
    }

    /* TODO InputStream */
    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        byte[] header = {
            'S', 'S', '_', 'S', 'I', 'F', ' ', ' ', ' ', ' ', '0', '.', '0', '0', 0x1a, 0
        };

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
Debug.println(obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new ArtMasterImageReader(this);
    }
}

/* */
