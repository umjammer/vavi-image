/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import vavi.awt.image.quantize.NeuralNetQuantizeOp;


/**
 * Quantizer. (NeuralNet)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 070628 nsano initial version <br>
 */
public class Quantizer {

    /**
     *
     * @param args 0 in_file, 1: out_file, 2: colors
     */
    public static void main(String[] args) throws Exception {
        String inFile = args[0];
        String outFile = args[1];
        String type = outFile.substring(outFile.indexOf('.') + 1);
        int colors = Integer.parseInt(args[2]);

        BufferedImage image = ImageIO.read(new File(inFile));

        BufferedImageOp filter = new NeuralNetQuantizeOp(colors);
        BufferedImage filteredImage = filter.filter(image, null);

        ImageIO.write(filteredImage, type, Files.newOutputStream(new File(outFile).toPath()));
    }
}
