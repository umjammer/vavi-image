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
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vavi.awt.image.resample.AwtResampleOp;
import vavi.awt.image.resample.FfmpegResampleOp;
import vavi.swing.JImageComponent;


/**
 * Scaling. (awt, ffmpeg)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 061012 nsano initial version <br>
 */
public class Scaling_awt_ffmpeg {

    static {
        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.getDefaults().put("TextField.background", UIManager.getColor("Panel.background"));
        UIManager.getDefaults().put("TextField.border", BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 4));
        UIManager.getDefaults().put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }

    public static void main(String[] args) throws Exception {
        new Scaling_awt_ffmpeg(args);
    }

    BufferedImage rightImage;
    BufferedImage leftImage;
    JSlider slider;
    JImageComponent rightImageComponent;
    JImageComponent leftImageComponent;
    JLabel statusLabel;

    Scaling_awt_ffmpeg(String[] args) throws Exception {
System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));
        int w = image.getWidth();
        int h = image.getHeight();
System.err.println(w + ", " + h);

//ImageConverter ic = ImageConverter.getInstance();
//ic.setColorModelType(BufferedImage.TYPE_INT_RGB);
        leftImage = image;
//        rightImage = ic.toBufferedImage(image);
        rightImage = image;
        slider = new JSlider();
        slider.setMaximum(100);
        slider.setMinimum(1);
        slider.setValue(100);
        slider.addChangeListener(event -> {
            JSlider source = (JSlider) event.getSource();
            if (source.getValueIsAdjusting()) {
                return;
            }
            float scale = source.getValue() / 100f;

            // left
            {
                BufferedImage image1 = leftImage;
                BufferedImageOp filter = new AwtResampleOp(scale, scale);
long t = System.currentTimeMillis();
                BufferedImage filteredImage = filter.filter(image1, null);
System.err.println("left: " + (System.currentTimeMillis() - t) + "ms");
                leftImageComponent.setImage(filteredImage);
                leftImageComponent.repaint();
            }

            // right
            {
                BufferedImage image1 = rightImage;
                BufferedImageOp filter = new FfmpegResampleOp(scale, scale, FfmpegResampleOp.Hint.LANCZOS);
long t = System.currentTimeMillis();
                BufferedImage filteredImage = filter.filter(image1, null);
System.err.println("right: " + (System.currentTimeMillis() - t) + "ms");
//System.err.println("image: " + filteredImage);
                rightImageComponent.setImage(filteredImage);
                rightImageComponent.repaint();
            }

System.err.println("scale: " + scale);
            statusLabel.setText("scale: " + scale);
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
        basePanel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent event) {
                split.setDividerLocation(0.5);
            }
            public void componentResized(ComponentEvent event) {
                split.setDividerLocation(0.5);
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(basePanel);

        statusLabel = new JLabel();
        statusLabel.setText("original");
        basePanel.add(statusLabel, BorderLayout.SOUTH);

        JFrame frame = new JFrame();
        frame.setTitle("AWT (Area Averaging) | FFMPEG (Lanczos)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        split.setDividerLocation(0.5);
        frame.setVisible(true);
    }
}

/* */
