/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ppm;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.imageio.WrappedImageInputStream;


/**
 * PpmImageReader.
 * <li>TODO when input is InputStream
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano port from <br>
 *          0.01 021116 nsano refine <br>
 */
public class PpmImageReader extends ImageReader {

    private IIOMetadata metadata;
    private ImageInputStream stream;
    private Ppm ppm;

    /** */
    public PpmImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
        metadata = null;
        stream = null;
    }

    /** @see ImageReader */
    public void setInput(Object input, boolean isStreamable) {
        super.setInput(input, isStreamable);

        if (input == null) {
            stream = null;
            return;
        }
        if (input instanceof ImageInputStream) {
            stream = (ImageInputStream) input;
        } else {
            throw new IllegalArgumentException("bad input");
        }
    }

    /** @see ImageReader */
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    /** */
    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("bad index");
        } else {
            return;
        }
    }

    /** @see ImageReader */
    public int getWidth(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        readHeader();
        return ppm.getWidth();
    }

    /** @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        readHeader();
        return ppm.getHeight();
    }

    /** @see ImageReader */
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        readMetadata();

        int sourceXSubsampling = 1;
        int sourceYSubsampling = 1;
        int sourceBands[] = null;
        int destinationBands[] = null;
        Point destinationOffset = new Point(0, 0);
        Rectangle sourceRegion =
            new Rectangle(0, 0, ppm.getWidth(), ppm.getHeight());
        if (param != null) {
            sourceXSubsampling = param.getSourceXSubsampling();
            sourceYSubsampling = param.getSourceYSubsampling();
            sourceBands = param.getSourceBands();
            destinationBands = param.getDestinationBands();
            destinationOffset = param.getDestinationOffset();
            sourceRegion = ImageReader.getSourceRegion(param,
                                                       ppm.getWidth(),
                                                       ppm.getHeight());
        }
        BufferedImage dst = ImageReader.getDestination(param,
                                                       getImageTypes(0),
                                                       ppm.getWidth(),
                                                       ppm.getHeight());
        int inputBands = ppm.getImageType() != Ppm.BINARY_PPM ? 1 : 3;
        ImageReader.checkReadParamBandSettings(param,
                                           inputBands,
                                           dst.getSampleModel().getNumBands());
        int bandOffsets[] = new int[inputBands];
        for (int i = 0; i < inputBands; i++)
            bandOffsets[i] = i;

        int bytesPerRow = ppm.getWidth() * inputBands;
        WritableRaster rowRas;
        DataBufferByte rowDB;

        if (ppm.getImageType() != Ppm.BINARY_PBM) {
            rowDB = new DataBufferByte(bytesPerRow);
            rowRas = Raster.createInterleavedRaster(rowDB,
                                                    ppm.getWidth(),
                                                    1,
                                                    bytesPerRow,
                                                    inputBands,
                                                    bandOffsets,
                                                    new Point(0, 0));
        } else {
            rowDB = new DataBufferByte(bytesPerRow / 8);
            rowRas = Raster.createPackedRaster(rowDB,
                                               ppm.getWidth(),
                                               1,
                                               1,
                                               new Point(0, 0));
        }

        byte rowBuf[] = rowDB.getData();
        int pixel[] = rowRas.getPixel(0, 0, (int[])null);
        WritableRaster imRas = dst.getWritableTile(0, 0);
        int dstMinX = imRas.getMinX();
        int dstMaxX = (dstMinX + imRas.getWidth()) - 1;
        int dstMinY = imRas.getMinY();
        int dstMaxY = (dstMinY + imRas.getHeight()) - 1;

        if (sourceBands != null) {
            rowRas = rowRas.createWritableChild(0,
                                                0,
                                                ppm.getWidth(),
                                                1,
                                                0,
                                                0,
                                                sourceBands);
        }
        if (destinationBands != null) {
            imRas = imRas.createWritableChild(0,
                                              0,
                                              imRas.getWidth(),
                                              imRas.getHeight(),
                                              0,
                                              0,
                                              destinationBands);
        }

        for (int srcY = 0; srcY < ppm.getHeight(); srcY++) {
            try {
                stream.readFully(rowBuf);
            } catch (IOException e) {
                throw new IIOException("Error reading line " + srcY, e);
            }

            if (srcY < sourceRegion.y ||
                srcY >= sourceRegion.y + sourceRegion.height ||
                (srcY - sourceRegion.y) % sourceYSubsampling != 0) {
                continue;
            }
            int dstY = destinationOffset.y +
                (srcY - sourceRegion.y) / sourceYSubsampling;
            if (dstY < dstMinY) {
                continue;
            }
            if (dstY > dstMaxY) {
                break;
            }
            for (int srcX = sourceRegion.x;
                 srcX < sourceRegion.x + sourceRegion.width;
                 srcX++) {

                if ((srcX - sourceRegion.x) % sourceXSubsampling != 0) {
                    continue;
                }
                int dstX = destinationOffset.x +
                    (srcX - sourceRegion.x) / sourceXSubsampling;
                if (dstX < dstMinX) {
                    continue;
                }
                if (dstX > dstMaxX) {
                    break;
                }
                rowRas.getPixel(srcX, 0, pixel);
                imRas.setPixel(dstX, dstY, pixel);
            }
        }

        return dst;
    }

    /** @see ImageReader */
    public IIOMetadata getStreamMetadata() throws IIOException {
        return null;
    }

    /** @see ImageReader */
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        readMetadata();
        return metadata;
    }

    /** */
    private IIOMetadata readMetadata() throws IIOException {
        if (metadata != null) {
            return null;
        } else {
            readHeader();
            return null;
        }
    }

    /** */
    private void readHeader() throws IIOException {
        if (stream == null) {
            throw new IllegalStateException("No input stream");
        }

        try {
            if (ppm == null) {
                ppm = new Ppm();
            }
            ppm.readHeader(new WrappedImageInputStream(stream));
        } catch (IOException e) {
            throw new IIOException("Error reading header", e);
        }
    }

    /** */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        readHeader();
        ImageTypeSpecifier specifier = null;
        java.util.List<ImageTypeSpecifier> l = new ArrayList<>();
        switch (ppm.getImageType()) {
        case Ppm.BINARY_PBM:
            specifier = ImageTypeSpecifier.createGrayscale(1, 0, false);
            break;
        case Ppm.BINARY_PGM:
            specifier = ImageTypeSpecifier.createGrayscale(8, 0, false);
            break;
        case Ppm.BINARY_PPM:
            ColorSpace rgb = ColorSpace.getInstance(1000);
            int bandOffsets[] = new int[3];
            bandOffsets[0] = 0;
            bandOffsets[1] = 1;
            bandOffsets[2] = 2;
            specifier = ImageTypeSpecifier.createInterleaved(rgb,
                                                             bandOffsets,
                                                             0,
                                                             false,
                                                             false);
            break;
        }
        l.add(specifier);
        return l.iterator();
    }
}

/* */
