/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.am88;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.awt.image.am88.ArtMasterImage;
import vavi.awt.image.am88.ArtMasterImageSource;
import vavi.imageio.ImageConverter;
import vavi.imageio.WrappedImageInputStream;
import vavi.util.Debug;


/**
 * ArtMasterImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class ArtMasterImageReader extends ImageReader {

    /** */
    private IIOMetadata metadata;

    /** TODO eliminate */
    private ArtMasterImageSource imageSource;

    /** */
    public ArtMasterImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IIOException {
        return ArtMasterImage.W;
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        return ArtMasterImage.H;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        InputStream is = null;

        if (input instanceof ImageInputStream) {
            is = new WrappedImageInputStream((ImageInputStream) input);
        } else if (input instanceof InputStream) {
            is = (InputStream) input;
        } else {
Debug.println(input);
        }

        Toolkit t = Toolkit.getDefaultToolkit();
        try {
            imageSource = new ArtMasterImageSource(is);
            Image image = t.createImage(imageSource);
//Debug.println(w + ", " + h + ": " + image.getClass().getName() + "[" + imageIndex + "]");
            return ImageConverter.getInstance().toBufferedImage(image);
        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
        }
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IIOException {
        return metadata;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        return metadata;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
Debug.println(Level.FINE, "here");
        ImageTypeSpecifier specifier = null;
        java.util.List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}

/* */
