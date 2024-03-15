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
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;

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
    private GifImage gifImage;

    /** */
    public NonLzwGifImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IIOException {
        return gifImage.getNumImages();
    }

    @Override
    public int getWidth(int imageIndex) throws IIOException {
        return gifImage.getWidth(imageIndex);
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        return gifImage.getHeight(imageIndex);
    }

    /** */
    public BufferedImage readImage(int imageIndex, InputStream is) throws IOException {

        ColorModel cm = gifImage.getColorModel(imageIndex);

        int width = gifImage.getWidth(imageIndex);
        int height = gifImage.getHeight(imageIndex);

        int pixelSize = cm.getPixelSize();

        byte[] vram;
        switch (pixelSize) {
        case 1:
            vram = gifImage.loadMonoColor(imageIndex);
            break;
        case 2:
        case 3:
        case 4:
            vram = gifImage.load16Color(imageIndex);
            break;
        default:
        case 8:
            vram = gifImage.load256Color(imageIndex);
            break;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel) cm);
        image.getRaster().setDataElements(0, 0, width, height, vram);

        return image;
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
Debug.println("unsupported input: " + input);
        }

        try {
            if (gifImage == null) {
                gifImage = GifImage.readFrom(is);
            }
            return readImage(imageIndex, is);
        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
        }
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IIOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {

        return new IIOMetadata() {
            @Override public void reset() {
                throw new UnsupportedOperationException();
            }
            @Override public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
                throw new UnsupportedOperationException();
            }
            @Override public boolean isReadOnly() {
                return true;
            }
            @Override public Node getAsTree(String formatName) {
                IIOMetadataNode rootNode = new IIOMetadataNode(NonLzwGifImageReaderSpi.NativeImageMetadataFormatName);
                GifImage.ImageDescriptor imageDescriptor = gifImage.getImageDescriptor(imageIndex);
                IIOMetadataNode imageDescriptorNode = new IIOMetadataNode("ImageDescriptor");
                imageDescriptorNode.setAttribute("imageLeftPosition", String.valueOf(imageDescriptor.left));
                imageDescriptorNode.setAttribute("imageTopPosition", String.valueOf(imageDescriptor.top));
                imageDescriptorNode.setAttribute("imageWidth", String.valueOf(imageDescriptor.width));
                imageDescriptorNode.setAttribute("imageHeight", String.valueOf(imageDescriptor.height));
                GifImage.GraphicControlExtension graphicControlExtension = gifImage.getGraphicControlExtension(imageIndex);
                IIOMetadataNode graphicControlExtensionNode = new IIOMetadataNode("GraphicControlExtension");
                graphicControlExtensionNode.setAttribute("disposalMethod", getDisposalMethod(graphicControlExtension.getDisposalMethod()));
                graphicControlExtensionNode.setAttribute("delayTime", String.valueOf(graphicControlExtension.delayTime));
                rootNode.appendChild(imageDescriptorNode);
                rootNode.appendChild(graphicControlExtensionNode);
                return rootNode;
            }
            @Override public String getNativeMetadataFormatName() {
                return NonLzwGifImageReaderSpi.NativeImageMetadataFormatName;
            }
            String getDisposalMethod(int disposalMethod) {
                switch (disposalMethod) {
                case 0: return "none";
                case 1: return "doNotDispose";
                case 2: return "restoreToBackgroundColor";
                case 3: return "restoreToPrevious";
                default: return "notSpecified";
                }
            }
        };
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        ImageTypeSpecifier specifier = null;
        List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}
