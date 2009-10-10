/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
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

import vavi.imageio.ImageConverter;
import vavi.swing.JImageComponent;


/**
 * Jpeg quality, .
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 07xxxx nsano initial version <br>
 */
public class t146_14 {

    public static void main(String[] args) throws Exception {
        new t146_14(args);
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

    Image rightImage;

    Image leftImage;

    JSlider qualitySlider;

    JImageComponent rightImageComponent;

    JImageComponent leftImageComponent;

    JLabel statusLabel;

    t146_14(String[] args) throws Exception {
System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));
        int w = image.getWidth();
        int h = image.getHeight();
System.err.println(w + ", " + h);

        final int S = 3;
        leftImage = image.getScaledInstance(w / S, h / S, Image.SCALE_AREA_AVERAGING);
        rightImage = image.getScaledInstance(w / S, h / S, Image.SCALE_AREA_AVERAGING);
        qualitySlider = new JSlider();
        qualitySlider.setMaximum(95);
        qualitySlider.setMinimum(5);
        qualitySlider.setValue(75);
        qualitySlider.addChangeListener(new ChangeListener() {
            ImageConverter converter = ImageConverter.getInstance();
            ImageWriter iwL;
            ImageWriter iwR;
            {
                Properties props = new Properties();
                try {
                    props.load(t146_14.class.getResourceAsStream("local.properties"));
                } catch (Exception e) {
e.printStackTrace(System.err);
                }

                String classNameL = props.getProperty("image.writer.class", "com.sun.imageio.plugins.jpeg.JPEGImageWriter");
                String classNameR = props.getProperty("image.writer.class2", "com.sun.imageio.plugins.jpeg.JPEGImageWriter");
                Class<?> classL;
                Class<?> classR;
                try {
                    classL = Class.forName(classNameL);
                    classR = Class.forName(classNameR);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("no such ImageWriter: " + classNameL + " or " + classNameR);
                }
                Iterator<ImageWriter> iws = ImageIO.getImageWritersByFormatName("JPEG");
                while (iws.hasNext()) {
                    ImageWriter tmpIw = iws.next();
System.err.println("ImageWriter: " + tmpIw.getClass());
                    // BUG? JPEG ‚Ì ImageWriter ‚ª Thread Safe ‚¶‚á‚È‚¢‹C‚ª‚·‚é
                    if (classL.isInstance(tmpIw)) {
                        iwL = tmpIw;
System.err.println("ImageWriter L: " + iwL.getClass());
                        break;
                    }
                }
                iws = ImageIO.getImageWritersByFormatName("JPEG");
                while (iws.hasNext()) {
                    ImageWriter tmpIw = iws.next();
                    if (classR.isInstance(tmpIw)) {
                        iwR = tmpIw;
System.err.println("ImageWriter R: " + iwR.getClass());
                        break;
                    }
                }
                if (iwL == null || iwR == null ) {
                    throw new IllegalStateException("no suitable ImageWriter");
                }

                //
                converter.setColorModelType(BufferedImage.TYPE_INT_RGB);
            }
            public void stateChanged(ChangeEvent event) {
                JSlider source = (JSlider) event.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                float quality = source.getValue() / 100f;

                try {
                    // L
                    BufferedImage image = converter.toBufferedImage(leftImage);
    
                    //
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                    iwL.setOutput(ios);
    
                    ImageWriteParam iwp = iwL.getDefaultWriteParam();
                    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    iwp.setCompressionQuality(quality);
//System.err.println(iwp.getClass().getName());
                    if (JPEGImageWriteParam.class.isInstance(iwp)) {
                        JPEGImageWriteParam.class.cast(iwp).setOptimizeHuffmanTables(true);
                    }
//System.err.println(StringUtil.paramString(iwp.getCompressionTypes()));

                    //                    
                    iwL.write(null, new IIOImage(image, null, null), iwp);
                    ios.flush();
                    ios.close();

                    //
                    BufferedImage processedImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

                    //
                    leftImageComponent.setImage(processedImage);
                    leftImageComponent.repaint();

                    int sizeL = baos.size();

                    // R
                    image = converter.toBufferedImage(rightImage);
    
                    //
                    baos = new ByteArrayOutputStream();
                    ios = ImageIO.createImageOutputStream(baos);
                    iwR.setOutput(ios);
    
                    iwp = iwR.getDefaultWriteParam();
                    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    iwp.setCompressionQuality(quality);
                    if (JPEGImageWriteParam.class.isInstance(iwp)) {
                        JPEGImageWriteParam.class.cast(iwp).setOptimizeHuffmanTables(true);
                    }
//System.err.println(StringUtil.paramString(iwp));

                    //                    
//iwR.write(image);
                    iwR.write(null, new IIOImage(image, null, null), iwp);
                    ios.flush();
                    ios.close();
//System.err.println("quality: " + quality + ", size: " + baos.size());

                    //
                    processedImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

                    //
                    rightImageComponent.setImage(processedImage);
                    rightImageComponent.repaint();

                    int sizeR = baos.size();
System.err.println("quality: " + quality + ", L size: " + sizeL + ", R size: " + sizeR + " " + iwL + ", " + iwR);
                    statusLabel.setText("quality: " + quality + ", L size: " + sizeL + ", R size: " + sizeR);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        });

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        basePanel.add(qualitySlider, BorderLayout.NORTH);

        leftImageComponent = new JImageComponent();
        leftImageComponent.setImage(leftImage);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(w, h));
        leftPanel.add(leftImageComponent, BorderLayout.CENTER);

        rightImageComponent = new JImageComponent();
        rightImageComponent.setImage(rightImage);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(w, h));
        rightPanel.add(rightImageComponent, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane();
        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        split.setPreferredSize(new Dimension(800, 600));

        basePanel.add(split, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(basePanel);

        statusLabel = new JLabel();
        statusLabel.setText("original");
        basePanel.add(statusLabel, BorderLayout.SOUTH);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        split.setDividerLocation(0.5);
        frame.setVisible(true);
    }
}

/* */
