/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.am88;

import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;


/**
 * Represents ArtMaster88 formated image.
 *
 * TODO palette control
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano refine access mode, messages <br>
 *          1.01 010903 nsano fix read bug <br>
 *          1.02 020413 nsano optimize color model <br>
 */
public class ArtMasterImageSource implements ImageProducer {

    /** */
    private ArtMasterImage image;

    /** */
    private ImageConsumer ic;

    @Override
    public synchronized void addConsumer(ImageConsumer ic) {
        this.ic = ic;
        if (this.ic != null) {
            loadPixel();
        }
        this.ic = null;
    }

    @Override
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return ic == this.ic;
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (this.ic == ic)
            this.ic = null;
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    /**
     * Creates ArtMaster88 formated image.
     * @throws IllegalArgumentException when header is wrong
     */
    public ArtMasterImageSource(InputStream in) throws IOException {
        image = new ArtMasterImage(in);
    }

    /**
     * Loads pixels.
     */
    private void loadPixel() {
        ic.setDimensions(image.getWidth(), image.getHeight());
        ic.setProperties(new Hashtable<>());
        ic.setColorModel(image.cm);

        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME);

        ic.setPixels(0, 0, image.getWidth(), image.getHeight(), image.cm, image.getPixels(), 0, image.getWidth());

        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
}
