/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ico;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.awt.image.ico.WindowsIconImageSource;
import vavi.imageio.ImageConverter;
import vavi.imageio.WrappedImageInputStream;
import vavi.util.Debug;


/**
 * WindowsIconImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class WindowsIconImageReader extends ImageReader {

    /** */
    private IIOMetadata metadata;

    /** TODO eliminate */
    private WindowsIconImageSource imageSource;

    /** */
    public WindowsIconImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /* @see ImageReader */
    public int getNumImages(boolean allowSearch) throws IIOException {
        return imageSource.getDeviceCount();
    }

    /* @see ImageReader */
    public int getWidth(int imageIndex) throws IIOException {
        imageSource.changeDevice(imageIndex);
        return imageSource.getWindowsBitmap().getWidth();
    }

    /* @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        imageSource.changeDevice(imageIndex);
        return imageSource.getWindowsBitmap().getHeight();
    }

    /** */
    private WindowsIconImageSource load() throws IOException {
        InputStream is = null;

        if (input instanceof ImageInputStream) {
            is = new WrappedImageInputStream((ImageInputStream) input);
        } else if (input instanceof InputStream) {
            is = (InputStream) input;
        } else {
Debug.println(input);
        }

        return new WindowsIconImageSource(is);
    }

    /** @see ImageReader */
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        if (imageSource == null) {
            try {
                imageSource = load();
            } catch (IOException e) {
                throw new IIOException(e.getMessage(), e);
            }
        }

        imageSource.changeDevice(imageIndex);
        Toolkit t = Toolkit.getDefaultToolkit();
        Image image = t.createImage(imageSource);
//Debug.println(w + ", " + h + ": " + image.getClass().getName() + "[" + imageIndex + "]");
        return ImageConverter.getInstance().toBufferedImage(image);
    }

    /* @see ImageReader */
    public IIOMetadata getStreamMetadata() throws IIOException {
        return metadata;
    }

    /* @see ImageReader */
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        return metadata;
    }

    /* */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
Debug.println("here");
        ImageTypeSpecifier specifier = null;
        java.util.List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}

/* */
