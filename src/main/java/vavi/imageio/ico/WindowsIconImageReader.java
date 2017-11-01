/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.ico;

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

import vavi.awt.image.bmp.WindowsIconImageSource;
import vavi.imageio.ImageConverter;
import vavi.imageio.WrappedImageInputStream;
import vavi.util.Debug;


/**
 * WindowsIconImageReader.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 021116 nsano initial version <br>
 */
public class WindowsIconImageReader extends ImageReader {
    /** */
    private IIOMetadata metadata;
    /** */
    private WindowsIconImageSource imageSource;

    /** */
    public WindowsIconImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /** @see ImageReader */
    public int getNumImages(boolean allowSearch) throws IIOException {
        return imageSource.getDeviceCount();
    }

    /** @see ImageReader */
    public int getWidth(int imageIndex) throws IIOException {
        imageSource.changeDevice(imageIndex);
        return imageSource.getWindowsBitmap().getWidth();
    }

    /** @see ImageReader */
    public int getHeight(int imageIndex) throws IIOException {
        imageSource.changeDevice(imageIndex);
        return imageSource.getWindowsBitmap().getHeight();
    }

    /** @see ImageReader */
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
            imageSource = new WindowsIconImageSource(is);
            imageSource.changeDevice(imageIndex);
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
        return metadata;
    }

    /** */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
Debug.println("here");
        ImageTypeSpecifier specifier = null;
        java.util.List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }

    //----

    /** */
    public static void main(final String[] args) throws IOException {
System.err.println(args[0]);
        ImageReader ir = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("ICO");
        while (irs.hasNext()) {
            ImageReader tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(WindowsIconImageReader.class.getName())) {
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
