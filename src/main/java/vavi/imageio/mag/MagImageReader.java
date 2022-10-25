/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.mag;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.awt.image.am88.ArtMasterImage;
import vavi.awt.image.mag.Mag;
import vavi.imageio.ImageConverter;
import vavi.imageio.WrappedImageInputStream;
import vavi.io.LittleEndianSeekableDataInputStream;
import vavi.io.SeekableDataInput;
import vavi.io.SeekableDataInputStream;
import vavi.util.Debug;


/**
 * MagImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class MagImageReader extends ImageReader {

    /** */
    private IIOMetadata metadata;

    /** */
    private BufferedImage image;

    /** */
    public MagImageReader(ImageReaderSpi originatingProvider) {
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

        ImageInputStream iis = null;

        try {
            if (input instanceof ImageInputStream) {
                iis = (ImageInputStream) input;
            } else if (input instanceof InputStream) {
                iis = ImageIO.createImageInputStream(input);
            } else {
                throw new UnsupportedOperationException(input.getClass().getName());
            }

            image = Mag.load(new SeekableDataInputImageInputStream(iis, ByteOrder.LITTLE_ENDIAN));
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
