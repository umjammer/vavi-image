/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.bmp;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
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

import vavi.awt.image.bmp.WindowsBitmap;
import vavi.imageio.WrappedImageInputStream;
import vavi.util.Debug;


/**
 * WindowsBitmapImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano initial version <br>
 *          0.01 021116 nsano refine <br>
 */
public class WindowsBitmapImageReader extends ImageReader {
    /** */
    private WindowsBitmap windowsBitmap;
    /** */
    private IIOMetadata metadata;

    /** */
    public WindowsBitmapImageReader(ImageReaderSpi originatingProvider) {
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
        return windowsBitmap.getWidth();
    }

    /** @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return windowsBitmap.getHeight();
    }

    /** */
    public static BufferedImage readImage(InputStream is) throws IOException {
        WindowsBitmap windowsBitmap = WindowsBitmap.readFrom(is);

        // インデックスカラー用イメージバッファ
        byte[] vram = null;
        // フルカラー用イメージ用バッファ
        int[] ivram = null;

        int bits = windowsBitmap.getBits();
        int compression = windowsBitmap.getCompression();
        int width = windowsBitmap.getWidth();
        int height = windowsBitmap.getHeight();

        switch (bits) {
        case 1:
            vram = windowsBitmap.getMonoColorData();
            break;
        case 4:
            if (compression == WindowsBitmap.Type.RLE4.ordinal()) {
                vram = windowsBitmap.get16ColorRleData();
            } else {
                vram = windowsBitmap.get16ColorData();
            }
            break;
        case 8:
            if (compression == WindowsBitmap.Type.RLE8.ordinal()) {
                vram = windowsBitmap.get256ColorRleData();
            } else {
                vram = windowsBitmap.get256ColorData();
            }
            break;
        case 24:
            ivram = windowsBitmap.get24BitColorData();
            break;
        case 32:
            ivram = windowsBitmap.get32BitColorData();
            break;
        }

        BufferedImage image = null;
        if (bits == 24) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
//Debug.println(image.getType() + ", " + image.getColorModel());
            image.getRaster().setDataElements(0, 0, width, height, ivram);
        } else if (bits == 32) { // TODO test
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
Debug.println(image.getType() + ", " + image.getColorModel());
            image.getRaster().setDataElements(0, 0, width, height, ivram);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel) windowsBitmap.getColorModel());
//Debug.println(image.getType() + ", " + image.getColorModel());
            image.getRaster().setDataElements(0, 0, width, height, vram);
        }

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
Debug.println(input);
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
//    private IIOMetadata readMetadata() throws IIOException {
//        return metadata;
//    }

    /** */
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

/* */
