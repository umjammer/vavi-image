/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vavi.awt.ImageComponent;
import vavi.awt.image.blur.GaussianBlurOp;
import vavi.imageio.IIOUtil;
import vavi.imageio.ImageConverter;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Jpeg quality, blur.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 061012 nsano initial version <br>
 */
@PropsEntity(url = "file://${user.dir}/local.properties")
public class JpegQualityBlur {

    @Property(name = "image.writer.class", value = "com.sun.imageio.plugins.jpeg.JPEGImageWriter")
    String className;

    {
        try {
            PropsEntity.Util.bind(this);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        new JpegQualityBlur(args);
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
    }

    Image rightImage;
    JSlider qualitySlider;
    ImageComponent rightImageComponent;
    JLabel statusLabel;

    JpegQualityBlur(String[] args) throws Exception {
System.err.println(args[0]);
        BufferedImage image = ImageIO.read(new File(args[0]));
        int w = image.getWidth();
        int h = image.getHeight();
System.err.println(w + ", " + h);

        Image leftImage = image.getScaledInstance(w / 6, h / 6, Image.SCALE_AREA_AVERAGING);
        rightImage = image.getScaledInstance(w / 6, h / 6, Image.SCALE_AREA_AVERAGING);
        qualitySlider = new JSlider();
        qualitySlider.setMaximum(95);
        qualitySlider.setMinimum(5);
        qualitySlider.setValue(75);
        qualitySlider.addChangeListener(new ChangeListener() {
            ImageConverter converter = ImageConverter.getInstance();
            ImageWriter iw;
            {
                iw = IIOUtil.getImageWriter("JPEG", className);
                //
                converter.setColorModelType(BufferedImage.TYPE_3BYTE_BGR);
            }
            public void stateChanged(ChangeEvent event) {
                JSlider source = (JSlider) event.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                float quality = source.getValue() / 100f;

                try {
                    //
                    BufferedImage image = converter.toBufferedImage(rightImage);

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
                    // "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4957775"
                    BufferedImage tmpImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                    tmpImage.getGraphics().drawImage(image, 0, 0, null);

//                    BufferedImageOp filter = new ConvolveOp(new Kernel(3, 3, elements), ConvolveOp.EDGE_NO_OP, null);
                    BufferedImageOp filter = new GaussianBlurOp(1.3f);
                    BufferedImage bluredImage = filter.filter(tmpImage, null);

                    //
                    iw.write(null, new IIOImage(bluredImage, null, null), iwp);

                    //
                    BufferedImage processedImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

                    //
                    rightImageComponent.setImage(processedImage);
                    rightImageComponent.repaint();

System.err.println("quality: " + quality + ", size: " + baos.size());
                    statusLabel.setText("quality: " + quality + ", size: " + baos.size());
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        });

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        basePanel.add(qualitySlider, BorderLayout.NORTH);

        ImageComponent leftImageComponent = new ImageComponent();
        leftImageComponent.setImage(leftImage);

        rightImageComponent = new ImageComponent();
        rightImageComponent.setImage(rightImage);

        JSplitPane split = new JSplitPane();
        split.setLeftComponent(leftImageComponent);
        split.setRightComponent(rightImageComponent);
        split.setPreferredSize(new Dimension(800, 600));

        basePanel.add(split, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(basePanel);

        statusLabel = new JLabel();
        statusLabel.setText("original");
        basePanel.add(statusLabel, BorderLayout.SOUTH);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.pack();
        split.setDividerLocation(0.5);
        frame.setVisible(true);
    }
}

/* */
