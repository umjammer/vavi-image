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

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vavi.awt.image.blur.GaussianBlurOp;
import vavi.awt.image.resample.AwtResampleOp;
import vavi.swing.JImageComponent;


/**
 * Gaussian Blur.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 061012 nsano initial version <br>
 */
public class t146_10 {

    public static void main(String[] args) throws Exception {
        new t146_10(args);
    }

    BufferedImage rightImage;
    BufferedImage leftImage;

    JSlider slider;
    JImageComponent rightImageComponent;
    JImageComponent leftImageComponent;
    JLabel statusLabel;

    t146_10(String[] args) throws Exception {
System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));
        int w = image.getWidth();
        int h = image.getHeight();
System.err.println(w + ", " + h);

        float scale = 0.17f;
        BufferedImageOp filter = new AwtResampleOp(scale, scale);
        leftImage = filter.filter(image, null);
        rightImage = filter.filter(image, null);
        slider = new JSlider();
        slider.setMaximum(100);
        slider.setMinimum(1);
        slider.setValue(10);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider source = (JSlider) event.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                float radius = source.getValue() / 10f;

                // right
                BufferedImage image = rightImage;
                BufferedImageOp filter = new GaussianBlurOp(radius);
long t = System.currentTimeMillis();
                BufferedImage filteredImage = filter.filter(image, null); 
System.err.println("right: " + (System.currentTimeMillis() - t) + "ms");
                rightImageComponent.setImage(filteredImage);
                rightImageComponent.repaint();

                statusLabel.setText("radius: " + radius);
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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        split.setDividerLocation(0.5);
        frame.setVisible(true);
    }
}

/* */
