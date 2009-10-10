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
import java.util.Iterator;
import java.util.Properties;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vavi.awt.image.AbstractBufferedImageOp;
import vavi.awt.image.blur.GaussianBlurOp;
import vavi.awt.image.resample.AwtResampleOp;
import vavi.swing.JImageComponent;


/**
 * Jpeg quality, blur.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 061013 nsano initial version <br>
 */
public class t146_11 {

    public static void main(String[] args) throws Exception {
        new t146_11(args);
    }

    static final float[] elements;
    
    static {
        int N = 3;
        elements = new float[N * N];
        float center = .4f;
        float othes = (1 - center) / (N * N - 1);
        for (int i = 0; i < N * N; i++) {
            elements[i] = othes;
        }
        elements[N * N / 2] = center;
    };

    BufferedImage rightImage;

    BufferedImage leftImage;

    JSlider qualitySlider;

    JSlider blurSlider;

    JImageComponent rightImageComponent;

    JImageComponent leftImageComponent;

    JLabel statusLabel;

    t146_11(String[] args) throws Exception {
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
        qualitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider source = (JSlider) event.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }

                updateLeftImage();
                updateRightImage();
            }
        });

        blurSlider = new JSlider();
        blurSlider.setMaximum(300);
        blurSlider.setMinimum(80);
        blurSlider.setValue(100);
        blurSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider source = (JSlider) event.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }

                updateLeftImage();
                updateRightImage();
            }
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

        final JSplitPane split = new JSplitPane();
        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        split.setPreferredSize(new Dimension(800, 600));

        basePanel.add(split, BorderLayout.CENTER);
        basePanel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent event) {
                split.setDividerLocation(0.5);
            }
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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
    static class JpegCompressOp extends AbstractBufferedImageOp {
        static ImageWriter iw = null;
        static {
            Properties props = new Properties();
            try {
                props.load(t146_11.class.getResourceAsStream("local.properties"));
            } catch (Exception e) {
e.printStackTrace(System.err);
            }
    
            String className = props.getProperty("image.writer.class", "com.sun.imageio.plugins.jpeg.JPEGImageWriter");
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("no such ImageWriter: " + className);
            }
            Iterator<ImageWriter> iws = ImageIO.getImageWritersByFormatName("JPEG");
            while (iws.hasNext()) {
                ImageWriter tmpIw = iws.next();
                // BUG? JPEG ‚Ì ImageWriter ‚ª Thread Safe ‚¶‚á‚È‚¢‹C‚ª‚·‚é
                if (clazz.isInstance(tmpIw)) {
                    iw = tmpIw;
System.err.println("ImageWriter: " + iw.getClass());
                    break;
                }
            }
            if (iw == null) {
                throw new IllegalStateException("no suitable ImageWriter");
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
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }
        int getSize() {
            return size;
        }
    }
}

/* */
