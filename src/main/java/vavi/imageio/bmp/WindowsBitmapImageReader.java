/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.bmp;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.awt.image.bmp.WindowsBitmapImageSource;
import vavi.imageio.ImageConverter;
import vavi.imageio.WrappedImageInputStream;
import vavi.util.Debug;


/**
 * WindowsBitmapImageReader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 020603 nsano initial version <br>
 *          0.01 021116 nsano refine <br>
 */
public class WindowsBitmapImageReader extends ImageReader {
    /** */
    private WindowsBitmapImageSource imageSource;
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
        return imageSource.getWindowsBitmap().getWidth();
    }

    /** @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(imageIndex + "/" + 1);
        }
        return imageSource.getWindowsBitmap().getHeight();
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
            imageSource = new WindowsBitmapImageSource(is);
//            int w = imageSource.getWindowsBitmap().getWidth();
//            int h = imageSource.getWindowsBitmap().getHeight();
            Toolkit t = Toolkit.getDefaultToolkit();
            Image image = t.createImage(imageSource);
//Debug.println(w + ", " + h + ": " + image.getClass().getName() + "[" + imageIndex + "]");
            return ImageConverter.getInstance().toBufferedImage(image);
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
        java.util.List<ImageTypeSpecifier> l = new ArrayList<ImageTypeSpecifier>();
        l.add(specifier);
        return l.iterator();
    }

    //----

    /** */
    public static void main(final String[] args) throws IOException {
System.err.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("BMP");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
System.err.println("ImageReader: " + tmpIr.getClass().getName());
            if (tmpIr.getClass().getName().equals(WindowsBitmapImageReader.class.getName())) {
                ir = tmpIr;
System.err.println("found ImageReader: " + ir.getClass().getName());
                break;
            }
        }
        ir.setInput(new FileInputStream(args[0]));
        final Image image = ir.read(0);

        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
