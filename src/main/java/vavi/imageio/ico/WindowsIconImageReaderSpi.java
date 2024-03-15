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
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class WindowsIconImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavisoft.com";
    private static final String Version = "0.00";
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

        if (obj instanceof ImageInputStream) {
            ImageInputStream is = (ImageInputStream) obj;
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
Debug.println(e);
                return false;
            }
//Debug.println(type + ", " + count + ", " + (bytes[0] == 0 && bytes[1] == 0 && type == 1 && count > 0));
            return bytes[0] == 0 && bytes[1] == 0 && type == 1 && count > 0;
        } else {
Debug.println(obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new WindowsIconImageReader(this);
    }
}
