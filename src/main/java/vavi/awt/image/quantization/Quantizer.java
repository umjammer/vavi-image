/*
 * Copyright (C) Jerry Huxtable 1998-2001. All rights reserved.
 */

package vavi.awt.image.quantization;


/**
 * The interface for an image quantizer. The addColor method is called (repeatedly
 * if necessary) with all the image pixels. A color table can then be returned by
 * calling the buildColorTable method.
 */
public interface Quantizer {
    /**
     * Initialize the quantizer. This should be called before adding any pixels.
     * @param numColors the number of colors we're quantizing to.
     */
    void setup(int numColors);

    /**
     * Add pixels to the quantizer.
     * @param pixels the array of ARGB pixels
     * @param offset the offset into the array
     * @param count the count of pixels
     */
    void addPixels(int[] pixels, int offset, int count);

    /*
     * Build a color table from the added pixels.
     * @param
     * @return an array of ARGB pixels representing a color table
     */
    int[] buildColorTable();

    /*
     * Using the previously-built color table, return the index into that table for a pixel.
     * This is guaranteed to return a valid index - returning the index of a color closer
     * to that requested if necessary.
     * @param rgb the pixel to find
     * @return the pixel's index in the color table
     */
    int getIndexForColor(int rgb);
}
