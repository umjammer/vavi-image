/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;

import vavi.awt.image.AbstractBufferedImageOp;
import vavi.awt.image.blur.GaussianBlurOp;
import vavi.awt.image.resample.AwtResampleOp;
import vavi.imageio.IIOUtil;
import vavi.swing.JImageComponent;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Jpeg quality, blur.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 061013 nsano initial version <br>
 */
public class JpegBlur {

    /**
     * @param args 0: image
     */
    public static void main(String[] args) throws Exception {
        new JpegBlur(args);
    }

    BufferedImage rightImage;
    BufferedImage leftImage;
    JSlider qualitySlider;
    JSlider blurSlider;
    JImageComponent rightImageComponent;
    JImageComponent leftImageComponent;
    JLabel statusLabel;

    /**
     * @param args 0: image
     */
    JpegBlur(String[] args) throws Exception {
System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));

        float scale = 0.17f;
        BufferedImageOp filter = new AwtResampleOp(scale, scale);
        leftImage = filter.filter(image, null);
        rightImage = filter.filter(image, null);

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        qualitySlider = new JSlider();
        qualitySlider.setMaximum(95);
        qualitySlider.setMinimum(5);
        qualitySlider.setValue(75);
        qualitySlider.addChangeListener(event -> {
            JSlider source = (JSlider) event.getSource();
            if (source.getValueIsAdjusting()) {
                return;
            }

            updateLeftImage();
            updateRightImage();
        });

        blurSlider = new JSlider();
        blurSlider.setMaximum(300);
        blurSlider.setMinimum(80);
        blurSlider.setValue(100);
        blurSlider.addChangeListener(event -> {
            JSlider source = (JSlider) event.getSource();
            if (source.getValueIsAdjusting()) {
                return;
            }

            updateLeftImage();
            updateRightImage();
        });

        upperPanel.add(qualitySlider, BorderLayout.NORTH);
        upperPanel.add(blurSlider, BorderLayout.SOUTH);

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        basePanel.add(upperPanel, BorderLayout.NORTH);

        leftImageComponent = new JImageComponent();
        leftImageComponent.setImage(leftImage);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(leftImageComponent, BorderLayout.CENTER);

        rightImageComponent = new JImageComponent();
        rightImageComponent.setImage(rightImage);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(rightImageComponent, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane();
        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        split.setPreferredSize(new Dimension(800, 600));

        basePanel.add(split, BorderLayout.CENTER);
        basePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                split.setDividerLocation(0.5);
            }
            @Override
            public void componentResized(ComponentEvent event) {
                split.setDividerLocation(0.5);
            }
        });

        statusLabel = new JLabel();
        statusLabel.setText("original");
        basePanel.add(statusLabel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(basePanel);

        JFrame frame = new JFrame();
        frame.setTitle("normal | blur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    /** */
    void updateLeftImage() {
        float quality = qualitySlider.getValue() / 100f;

        //
        BufferedImage image = leftImage;
        BufferedImageOp filter = new JpegCompressOp(quality);
        BufferedImage filteredImage = filter.filter(image, null);

        //
        leftImageComponent.setImage(filteredImage);
        leftImageComponent.repaint();
    }

    /** */
    void updateRightImage() {
        float quality = qualitySlider.getValue() / 100f;
        float blur = blurSlider.getValue() / 100f;

        //
        BufferedImage image = rightImage;
//      BufferedImageOp filter = new ConvolveOp(new Kernel(3, 3, elements), ConvolveOp.EDGE_NO_OP, null);
        BufferedImageOp filter = new GaussianBlurOp(blur);
        BufferedImage filteredImage = filter.filter(image, null);

        //
        image = filteredImage;
        filter = new JpegCompressOp(quality);
        filteredImage = filter.filter(image, null);
        int size = ((JpegCompressOp) filter).getSize();

        //
        rightImageComponent.setImage(filteredImage);
        rightImageComponent.repaint();

        //
System.err.println("quality: " + quality + ", size: " + size + ", blur: " + blur);
        statusLabel.setText("quality: " + quality + ", size: " + size + ", blur: " + blur);
    }

    /** */
    @PropsEntity(url = "file://${user.dir}/local.properties")
    static class JpegCompressOp extends AbstractBufferedImageOp {
        @Property(name = "image.writer.class", value = "com.sun.imageio.plugins.jpeg.JPEGImageWriter")
        String className;

        final ImageWriter iw;
        {
            try {
                PropsEntity.Util.bind(this);
                // BUG? JPEG の ImageWriter が Thread Safe じゃない気がする
                iw = IIOUtil.getImageWriter("JPEG", className);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        float quality;
        int size;
        JpegCompressOp(float quality) {
            this.quality = quality;
        }
        /**
         * {@link #size} will be set.
         * @param dst not used
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dst) {
            try {
                //
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                iw.setOutput(ios);

                ImageWriteParam iwp = iw.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                ((JPEGImageWriteParam) iwp).setOptimizeHuffmanTables(true);
                iwp.setCompressionQuality(quality);
//System.err.println(StringUtil.paramString(iwp.getCompressionTypes()));

                //
                iw.write(null, new IIOImage(src, null, null), iwp);

                //
                dst = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                this.size = baos.size();
                return dst;

            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        int getSize() {
            return size;
        }
    }
}
