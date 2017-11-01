/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vavi.awt.image.resample.AwtResampleOp;
import vavi.swing.JImageComponent;


/**
 * Scaling. (awt, java2d)
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 061012 nsano initial version <br>
 */
public class t146_9 {

    public static void main(String[] args) throws Exception {
        new t146_9(args);
    }

    BufferedImage rightImage;

    BufferedImage leftImage;

    JSlider slider;

    JImageComponent rightImageComponent;

    JImageComponent leftImageComponent;

    JLabel statusLabel;

    t146_9(String[] args) throws Exception {
System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));
        int w = image.getWidth();
        int h = image.getHeight();
System.err.println(w + ", " + h);

        leftImage = image;
        rightImage = image;
        slider = new JSlider();
        slider.setMaximum(100);
        slider.setMinimum(1);
        slider.setValue(100);
        slider.addChangeListener(new ChangeListener() {
            ImageWriter iw = ImageIO.getImageWritersByFormatName("JPEG").next(); // ちょっと適当か？
            {
                Properties props = new Properties();
                try {
                    props.load(new FileInputStream("local.properties"));
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
                    // BUG? JPEG の ImageWriter が Thread Safe じゃない気がする
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
            public void stateChanged(ChangeEvent event) {
                JSlider source = (JSlider) event.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                float scale = source.getValue() / 100f;

                // left
                BufferedImage image = leftImage;
                BufferedImageOp filter = new AwtResampleOp(scale, scale);
long t = System.currentTimeMillis();
                BufferedImage filteredImage = filter.filter(image, null);
System.err.println("left: " + (System.currentTimeMillis() - t) + "ms");
                leftImageComponent.setImage(filteredImage);
                leftImageComponent.repaint();

                // right
                image = rightImage;
//                filter = new java.awt.image.AffineTransformOp(java.awt.geom.AffineTransform.getScaleInstance(scale, scale), java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                filter = new vavi.awt.image.resample.Lanczos3ResampleOp(scale, scale);
//                filter = new vavi.awt.image.resample.G2dResampleOp(scale, scale);
t = System.currentTimeMillis();
                filteredImage = filter.filter(image, null); 
System.err.println("right: " + (System.currentTimeMillis() - t) + "ms");
                rightImageComponent.setImage(filteredImage);
                rightImageComponent.repaint();

System.err.println("scale: " + scale);
                statusLabel.setText("scale: " + scale);
            }
        });

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        basePanel.add(slider, BorderLayout.NORTH);

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
        frame.setTitle("AwtResampleOp | Lanczos3ResampleOp");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        split.setDividerLocation(0.5);
        frame.setVisible(true);
    }
}

/* */
