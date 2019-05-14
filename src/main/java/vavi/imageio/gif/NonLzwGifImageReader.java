/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.awt.image.gif.GifImage;
import vavi.imageio.WrappedImageInputStream;
import vavi.util.Debug;


/**
 * NonLzwGifImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 040914 nsano initial version <br>
 */
public class NonLzwGifImageReader extends ImageReader {
    /** */
    private GifImage imageSource;
    /** */
    private IIOMetadata metadata;

    /** */
    public NonLzwGifImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /** @see ImageReader */
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    /** @see ImageReader */
    public int getWidth(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return imageSource.getWidth();
    }

    /** @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return imageSource.getHeight();
    }

    /** */
    public static BufferedImage readImage(InputStream is) throws IOException {
        GifImage gifImage = GifImage.readFrom(is);

        ColorModel cm = gifImage.getColorModel();

        int width = gifImage.getWidth();
        int height = gifImage.getHeight();

        int pixelSize = cm.getPixelSize();

        byte[] vram = null;
        switch (pixelSize) {
        case 1:
            vram = gifImage.loadMonoColor();
            break;
        case 2:
        case 3:
        case 4:
            vram = gifImage.load16Color();
            break;
        default:
        case 8:
            vram = gifImage.load256Color();
            break;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel) cm);
        image.getRaster().setDataElements(0, 0, width, height, vram);

        return image;
    }

    /** @see ImageReader */
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        InputStream is = null;

        if (input instanceof ImageInputStream) {
            is = new WrappedImageInputStream((ImageInputStream) input);
        } else if (input instanceof InputStream) {
            is = (InputStream) input;
        } else {
Debug.println("unsupported input: " + input);
        }

        try {
            return readImage(is);
        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
        }
    }

    /** @see ImageReader */
    public IIOMetadata getStreamMetadata() throws IIOException {
        return metadata;
    }

    /** @see ImageReader */
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        return metadata;
    }

    /** */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        ImageTypeSpecifier specifier = null;
        List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}

/* */
