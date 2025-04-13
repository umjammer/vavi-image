/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.am88;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.awt.image.am88.ArtMasterImage;
import vavi.imageio.WrappedImageInputStream;

import static java.lang.System.getLogger;


/**
 * ArtMasterImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class ArtMasterImageReader extends ImageReader {

    private static final Logger logger = getLogger(ArtMasterImageReader.class.getName());

    /** */
    private IIOMetadata metadata;

    /** */
    private BufferedImage image;

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
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return image.getHeight();
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
logger.log(Level.WARNING, "unsupported input: " + input);
        }

        try {
            ArtMasterImage ami = new ArtMasterImage(is);
            image = new BufferedImage(ami.getWidth(), ami.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel) ami.getColorModel());
            image.getRaster().setDataElements(0, 0, ami.getWidth(), ami.getHeight(), ami.getPixels());
            return image;
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
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return metadata;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }

        ImageTypeSpecifier specifier = null;
        java.util.List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}
