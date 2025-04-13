/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vavi.awt.image.quantization;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.OutputStream;


/**
 * <p>
 * NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994. See
 * "Kohonen neural networks for optimal colour quantization" in "Network:
 * Computation in Neural Systems" Vol. 5 (1994) pp 351-367. for a discussion of
 * the algorithm. See also http://www.acm.org/~dekker/NEUQUANT.HTML
 * </p>
 * Current revision $Revision: 1.1.1.1 $ On branch $Name: $ Latest change by
 * $Author: jelmer $ on $Date: 2005/08/18 15:46:26 $
 *
 * @author Anthony Dekker
 */
public class NeuralNetQuantizer {

    /** no. of learning cycles */
    public static final int nCycles = 100;

    /** number of colours used */
    public int netSize = 256;

    /** number of reserved colours used */
    public static final int specials = 3;

    /** reserved background colour */
    public static final int bgColour = specials - 1;

    public int cutNetSize;

    public int maxNetPos;

    /** for 256 cols, radius starts at 32 */
    public int initRad;

    public static final int radiusBiasShift = 6;

    public static final int radiusBias = 1 << radiusBiasShift;

    public int initBiasRadius;

    /** factor of 1/30 each cycle */
    public static final int radiusDec = 30;

    /** alpha starts at 1 */
    public static final int alphaBiasShift = 10;

    /** biased by 10 bits */
    public static final int initAlpha = 1 << alphaBiasShift;

    public static final double gamma = 1024.0;

    public static final double beta = 1.0 / 1024.0;

    public static final double betaGamma = beta * gamma;

    /** the network itself */
    private double[][] network;

    /** the network itself */
    protected int[][] colorMap;

    /** for network lookup - really 256 */
    private int[] netIndex;

    /** bias and freq arrays for learning */
    private double[] bias;

    private double[] freq;

    // four primes near 500 - assume no image has a length so large
    // that it is divisible by all four primes

    public static final int prime1 = 499;

    public static final int prime2 = 491;

    public static final int prime3 = 487;

    public static final int prime4 = 503;

    public static final int maxPrime = prime4;

    protected int[] pixels = null;

    private final int sampleFac;

    /** */
    public NeuralNetQuantizer(Image image, int w, int h, int netSize) throws IOException {
        this(1);
        init0();
        setPixels(image, w, h);
        setUpArrays();
        this.netSize = netSize;
        init();
    }

    /** */
    public NeuralNetQuantizer(Image image, int w, int h) throws IOException {
        this(1);
        init0();
        setPixels(image, w, h);
        setUpArrays();
        init();
    }

    /** */
    public NeuralNetQuantizer(int sample, Image image, int w, int h) throws IOException {
        this(sample);
        init0();
        setPixels(image, w, h);
        setUpArrays();
        init();
    }

    /** */
    public NeuralNetQuantizer(Image image, ImageObserver observer) throws IOException {
        this(1);
        init0();
        setPixels(image, observer);
        setUpArrays();
        init();
    }

    /**
     * @param sample must be 1..30
     */
    private NeuralNetQuantizer(int sample) throws IOException {
        if (sample < 1 || sample > 30) {
            throw new IllegalArgumentException("Sample must be 1..30");
        }
        sampleFac = sample;
        // rest later
    }

    /** */
    public NeuralNetQuantizer(int sample, Image image, ImageObserver observer) throws IOException {
        this(sample);
        init0();
        setPixels(image, observer);
        setUpArrays();
        init();
    }

    /** */
    public int getColorCount() {
        return netSize;
    }

    /** */
    public Color getColor(int i) {
        if (i < 0 || i >= netSize) {
            return null;
        }
        int bb = colorMap[i][0];
        int gg = colorMap[i][1];
        int rr = colorMap[i][2];
        return new Color(rr, gg, bb);
    }

    /** */
    public int writeColourMap(boolean rgb, OutputStream out) throws IOException {
        for (int i = 0; i < netSize; i++) {
            int bb = colorMap[i][0];
            int gg = colorMap[i][1];
            int rr = colorMap[i][2];
            out.write(rgb ? rr : bb);
            out.write(gg);
            out.write(rgb ? bb : rr);
        }
        return netSize;
    }

    /** */
    protected void setUpArrays() {
        network[0][0] = 0.0; // black
        network[0][1] = 0.0;
        network[0][2] = 0.0;

        network[1][0] = 1.0; // white
        network[1][1] = 1.0;
        network[1][2] = 1.0;

        // RESERVED bgColour // background

        for (int i = 0; i < specials; i++) {
            freq[i] = 1.0 / netSize;
            bias[i] = 0.0;
        }

        for (int i = specials; i < netSize; i++) {
            double[] p = network[i];
            p[0] = (256.0 * (i - specials)) / cutNetSize;
            p[1] = (256.0 * (i - specials)) / cutNetSize;
            p[2] = (256.0 * (i - specials)) / cutNetSize;

            freq[i] = 1.0 / netSize;
            bias[i] = 0.0;
        }
    }

    /** */
    private void setPixels(Image image, ImageObserver observer) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Image is null");
        }
        int w = image.getWidth(observer);
        int h = image.getHeight(observer);
        setPixels(image, w, h);
    }

    /** */
    private void setPixels(Image image, int w, int h) throws IOException {
        if (w * h < maxPrime) {
            throw new IllegalArgumentException("Image is too small");
        }
        pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException ignored) {
        }
        if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
            throw new IOException("Image pixel grab aborted or errored");
        }
    }

    /** */
    private void init() {
        learn();
        fix();
        inxBuild();
    }

    /** */
    private void init0() {
        this.cutNetSize = netSize - specials;
        this.maxNetPos = netSize - 1;
        this.initRad = netSize / 8;
        this.initBiasRadius = initRad * radiusBias;
        this.network = new double[netSize][3];
        this.colorMap = new int[netSize][4];
        this.netIndex = new int[netSize];
        this.bias = new double[netSize];
        this.freq = new double[netSize];
    }

    /** Move neuron i towards biased (b,g,r) by factor alpha */
    private void alterSingle(double alpha, int i, double b, double g, double r) {
        double[] n = network[i]; // alter hit neuron
        n[0] -= (alpha * (n[0] - b));
        n[1] -= (alpha * (n[1] - g));
        n[2] -= (alpha * (n[2] - r));
    }

    /** */
    private void alterNeigh(double alpha, int rad, int i, double b, double g, double r) {

        int lo = i - rad;
        if (lo < specials - 1) {
            lo = specials - 1;
        }
        int hi = i + rad;
        if (hi > netSize) {
            hi = netSize;
        }

        int j = i + 1;
        int k = i - 1;
        int q = 0;
        while ((j < hi) || (k > lo)) {
            double a = (alpha * (rad * rad - q * q)) / (rad * rad);
            q++;
            if (j < hi) {
                double[] p = network[j];
                p[0] -= (a * (p[0] - b));
                p[1] -= (a * (p[1] - g));
                p[2] -= (a * (p[2] - r));
                j++;
            }
            if (k > lo) {
                double[] p = network[k];
                p[0] -= (a * (p[0] - b));
                p[1] -= (a * (p[1] - g));
                p[2] -= (a * (p[2] - r));
                k--;
            }
        }
    }

    /**
     * Search for biased BGR values. finds closest neuron (min dist) and updates
     * freq finds best neuron (min dist-bias) and returns position for
     * frequently chosen neurons, freq[i] is high and bias[i] is negative
     * bias[i] = gamma*((1/netsize)-freq[i])
     */
    private int contest(double b, double g, double r) {

        double bestD = Float.MAX_VALUE;
        double bestBiasd = bestD;
        int bestPos = -1;
        int bestBiasPos = bestPos;

        for (int i = specials; i < netSize; i++) {
            double[] n = network[i];
            double dist = n[0] - b;
            if (dist < 0) {
                dist = -dist;
            }
            double a = n[1] - g;
            if (a < 0) {
                a = -a;
            }
            dist += a;
            a = n[2] - r;
            if (a < 0) {
                a = -a;
            }
            dist += a;
            if (dist < bestD) {
                bestD = dist;
                bestPos = i;
            }
            double biasDist = dist - bias[i];
            if (biasDist < bestBiasd) {
                bestBiasd = biasDist;
                bestBiasPos = i;
            }
            freq[i] -= beta * freq[i];
            bias[i] += betaGamma * freq[i];
        }
        freq[bestPos] += beta;
        bias[bestPos] -= betaGamma;
        return bestBiasPos;
    }

    /** */
    private int specialFind(double b, double g, double r) {
        for (int i = 0; i < specials; i++) {
            double[] n = network[i];
            if (n[0] == b && n[1] == g && n[2] == r) {
                return i;
            }
        }
        return -1;
    }

    /** */
    private void learn() {
        int biasRadius = initBiasRadius;
        int alphaDec = 30 + ((sampleFac - 1) / 3);
        int lengthCount = pixels.length;
        int samplePixels = lengthCount / sampleFac;
        int delta = samplePixels / nCycles;
        int alpha = initAlpha;

        int i;
        int rad = biasRadius >> radiusBiasShift;
        if (rad <= 1) {
            rad = 0;
        }

        int step;
        int pos = 0;

        if ((lengthCount % prime1) != 0) {
            step = prime1;
        } else {
            if ((lengthCount % prime2) != 0) {
                step = prime2;
            } else {
                if ((lengthCount % prime3) != 0) {
                    step = prime3;
                } else {
                    step = prime4;
                }
            }
        }

        i = 0;
        while (i < samplePixels) {
            int p = pixels[pos];
            int red = (p >> 16) & 0xff;
            int green = (p >> 8) & 0xff;
            int blue = (p) & 0xff;

            double b = blue;
            double g = green;
            double r = red;

            if (i == 0) { // remember background colour
                network[bgColour][0] = blue;
                network[bgColour][1] = green;
                network[bgColour][2] = red;
            }

            int j = specialFind(b, g, r);
            j = j < 0 ? contest(b, g, r) : j;

            if (j >= specials) { // don't learn for specials
                double a = (1.0 * alpha) / initAlpha;
                alterSingle(a, j, b, g, r);
                if (rad > 0) {
                    alterNeigh(a, rad, j, b, g, r); // alter neighbours
                }
            }

            pos += step;
            while (pos >= lengthCount) {
                pos -= lengthCount;
            }

            i++;
            if (i % delta == 0) {
                alpha -= alpha / alphaDec;
                biasRadius -= biasRadius / radiusDec;
                rad = biasRadius >> radiusBiasShift;
                if (rad <= 1) {
                    rad = 0;
                }
            }
        }
    }

    /** */
    private void fix() {
        for (int i = 0; i < netSize; i++) {
            for (int j = 0; j < 3; j++) {
                int x = (int) (0.5 + network[i][j]);
                if (x < 0) {
                    x = 0;
                }
                if (x > 255) {
                    x = 255;
                }
                colorMap[i][j] = x;
            }
            colorMap[i][3] = i;
        }
    }

    /** Insertion sort of network and building of netindex[0..255] */
    private void inxBuild() {
        int previousCol = 0;
        int startPos = 0;

        for (int i = 0; i < netSize; i++) {
            int[] p = colorMap[i];
            int[] q;
            int smallPos = i;
            int smallVal = p[1]; // index on g
            // find smallest in i..netsize-1
            for (int j = i + 1; j < netSize; j++) {
                q = colorMap[j];
                if (q[1] < smallVal) { // index on g
                    smallPos = j;
                    smallVal = q[1]; // index on g
                }
            }
            q = colorMap[smallPos];
            // swap p (i) and q (smallpos) entries
            if (i != smallPos) {
                int j = q[0];
                q[0] = p[0];
                p[0] = j;
                j = q[1];
                q[1] = p[1];
                p[1] = j;
                j = q[2];
                q[2] = p[2];
                p[2] = j;
                j = q[3];
                q[3] = p[3];
                p[3] = j;
            }
            // smallval entry is now in position i
            if (smallVal != previousCol) {
                netIndex[previousCol] = (startPos + i) >> 1;
                for (int j = previousCol + 1; j < smallVal; j++) {
                    netIndex[j] = i;
                }
                previousCol = smallVal;
                startPos = i;
            }
        }
        netIndex[previousCol] = (startPos + maxNetPos) >> 1;
        for (int j = previousCol + 1; j < 256; j++) {
            netIndex[j] = maxNetPos; // really 256
        }
    }

    /** */
    public int convert(int pixel) {
        int alfa = (pixel >> 24) & 0xff;
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        int i = inxSearch(b, g, r);
        int bb = colorMap[i][0];
        int gg = colorMap[i][1];
        int rr = colorMap[i][2];
        return (alfa << 24) | (rr << 16) | (gg << 8) | (bb);
    }

    /** */
    public int lookup(int pixel) {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return inxSearch(b, g, r);
    }

    /** */
    public int lookup(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return inxSearch(b, g, r);
    }

    /** */
    public int lookup(boolean rgb, int x, int g, int y) {
        return rgb ? inxSearch(y, g, x) : inxSearch(x, g, y);
    }

    /** Search for BGR values 0..255 and return colour index */
    protected int inxSearch(int b, int g, int r) {
        int bestD = 1000; // biggest possible dist is 256*3
        int best = -1;
        int i = netIndex[g]; // index on g
        int j = i - 1; // start at netindex[g] and work outwards

        while ((i < netSize) || (j >= 0)) {
            if (i < netSize) {
                int[] p = colorMap[i];
                int dist = p[1] - g; // inx key
                if (dist >= bestD) {
                    i = netSize; // stop iter
                } else {
                    if (dist < 0) {
                        dist = -dist;
                    }
                    int a = p[0] - b;
                    if (a < 0) {
                        a = -a;
                    }
                    dist += a;
                    if (dist < bestD) {
                        a = p[2] - r;
                        if (a < 0) {
                            a = -a;
                        }
                        dist += a;
                        if (dist < bestD) {
                            bestD = dist;
                            best = i;
                        }
                    }
                    i++;
                }
            }
            if (j >= 0) {
                int[] p = colorMap[j];
                int dist = g - p[1]; // inx key - reverse dif
                if (dist >= bestD) {
                    j = -1; // stop iter
                } else {
                    if (dist < 0) {
                        dist = -dist;
                    }
                    int a = p[0] - b;
                    if (a < 0) {
                        a = -a;
                    }
                    dist += a;
                    if (dist < bestD) {
                        a = p[2] - r;
                        if (a < 0) {
                            a = -a;
                        }
                        dist += a;
                        if (dist < bestD) {
                            bestD = dist;
                            best = j;
                        }
                    }
                    j--;
                }
            }
        }

        return best;
    }
}
