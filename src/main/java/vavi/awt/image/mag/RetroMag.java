/*
 * http://raimeiji.s1006.xrea.com/labo/pmloader/index.html
 */

package vavi.awt.image.mag;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import vavi.util.ByteUtil;
import vavi.util.Debug;


/**
 * MAG file loader.
 *
 * @author Asuka Raimeiji（@raimeiji）
 * @version 2014-04-28 1.00
 *          2014-05-01 1.50 enabled to show comment using encoding converter library
 *          2014-05-19 1.51 performance up
 */
public class RetroMag {

    private Color[][] pixels;
    private int width, height;
    private String comment;
    private float xScale;
    private float yScale;

    /** entry point */
    public BufferedImage mainProcess(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String s = "00000000" + Integer.toBinaryString(b);
            sb.append(s.substring(s.length() - 8));
        }
        comment = "";
        xScale = 1;
        yScale = 1;
        if (!isMag(sb.substring(0, 64))) {
            throw new IllegalArgumentException("unsupported type");
        }
        loadMag(sb.toString());
        return drawImage();
    }

    /** Makes comment string */
    private static String comment(byte[] ba) {
        int p = ByteUtil.indexOf(ba, (byte) 0);
        return new String(ba, 0, p == -1 ? ba.length : p, Charset.forName("Shift_JIS"));
    }

    /** Loads MAG file */
    private void loadMag(String data) {
        // header
        // platform, comment
        int[] pos = new int[] {56};
        List<Byte> commentBufS = new ArrayList<>();
        List<Byte> commentBufC = new ArrayList<>();
        int len = data.length();
        while ((pos[0] += 8) <= len) {
            String b = data.substring(pos[0], pos[0] + 8);
            if (b.equals("00011010")) { // 0x1a
                break;
            }
            if (pos[0] < 256) {
                commentBufS.add((byte) Integer.parseInt(b, 2));
            } else {
                commentBufC.add((byte) Integer.parseInt(b, 2));
            }
        }
        if (commentBufS.size() != 0) {
            comment = comment(ByteUtil.toByteArray(commentBufS));
Debug.println(Level.FINE, "SAVER/USER:\n" + comment);
        }
        if (commentBufC.size() != 0) {
            comment = comment(ByteUtil.toByteArray(commentBufC));
Debug.println(Level.FINE, "COMMENT:\n" + comment);
        }
        int commentSize = pos[0] + 8;
        while ((pos[0] += 8) <= len) {
            if (data.startsWith("00000000", pos[0])) {
                break;
            }
        }
        pos[0] += 24;
        // screen mode
        if (data.charAt(pos[0]) == '1') {
            throw new IllegalStateException("unsupported screen mode: " + data.charAt(pos[0]) + ", " + pos[0]);
        }
        // this check refused some pictures in real world, so omit.
//        if (data.charAt(pos[0] + 7) == '1') {
//            throw new IllegalStateException(String.format("unsupported screen mode: %1$c, %2$d (0x%2$04x)", data.charAt(pos[0] + 7), pos[0] + 7));
//        }
        pos[0] += 8;
        // display location
        int xstart = wordConvert(16, data, pos);
        if (xstart % 8 != 0) {
            xstart -= xstart % 8;
        }
        int ystart = wordConvert(16, data, pos);
        int xend = wordConvert(16, data, pos);
        if (++xend % 8 != 0) {
            xend += (8 - xend % 8);
        }
        int yend = wordConvert(16, data, pos);
        yend++;
Debug.printf(Level.FINE, "xstart: %d, xend: %d, ystart: %d, yend: %d", xstart, ystart, xend, yend);
        // flag A, flag B, pixel data start position
        int flagA = commentSize + wordConvert(32, data, pos) * 8;
        int flagB;
        int flgaEnd = flagB = commentSize + wordConvert(32, data, pos) * 8;
        pos[0] += 32;
        int pixel = commentSize + wordConvert(32, data, pos) * 8;
        pos[0] += 32;

        // alloc buffer
        int vtlnsz = xend - xstart;
        width = xend;
        height = yend;
Debug.printf(Level.FINE, "width: %d, height: %d", width, height);
        ensureBuffer();

        Color[] colorPalette = new Color[16];

        // read palette
        for (int i = 0; i < 16; i++) {
            int r, g, b;
            r = Integer.parseInt(data.substring(pos[0] + 8, pos[0] + 12), 2) * 17;
            g = Integer.parseInt(data.substring(pos[0], pos[0] + 4), 2) * 17;
            b = Integer.parseInt(data.substring(pos[0] + 16, pos[0] + 20), 2) * 17;
            colorPalette[i] = new Color(r, g, b);
            pos[0] += 24;
        }

        // compression
        StringBuilder line = new StringBuilder();
        String prevLine = "";
        StringBuilder tempLine = new StringBuilder();
        int x = xstart;
        int y = ystart;
        for (int i = flagA; i < flgaEnd; i++) {
            // flags
            if (data.charAt(i) == '0') {
                line.append("00000000");
            } else {
                line.append(data, flagB, flagB + 8);
                flagB += 8;
            }
            int length = line.length();
            if (length >= vtlnsz) {
                if (y != ystart) {
                    // xor line above
                    for (int j = 0; j < length; j++) {
                        tempLine.append(line.charAt(j) ^ prevLine.charAt(j));
                    }
                    line = new StringBuilder(tempLine.toString());
                }
                // pixel data
                for (int j = 0; j < length; j += 4) {
                    switch (line.substring(j, j + 4)) {
                    case "0000":
                        String b = data.substring(pixel, pixel + 16);
                        pixel += 16;
                        for (int k = 0; k < 4; k++) {
                            pixels[x + k][y] = colorPalette[Integer.parseInt(b.substring(k * 4, k * 4 + 4), 2)];
                        }
                        break;
                    case "0001":
                        copyPixel(pixels, 4, 0, x, y);
                        break;
                    case "0010":
                        copyPixel(pixels, 8, 0, x, y);
                        break;
                    case "0011":
                        copyPixel(pixels, 16, 0, x, y);
                        break;
                    case "0100":
                        copyPixel(pixels, 0, 1, x, y);
                        break;
                    case "0101":
                        copyPixel(pixels, 4, 1, x, y);
                        break;
                    case "0110":
                        copyPixel(pixels, 0, 2, x, y);
                        break;
                    case "0111":
                        copyPixel(pixels, 4, 2, x, y);
                        break;
                    case "1000":
                        copyPixel(pixels, 8, 2, x, y);
                        break;
                    case "1001":
                        copyPixel(pixels, 0, 4, x, y);
                        break;
                    case "1010":
                        copyPixel(pixels, 4, 4, x, y);
                        break;
                    case "1011":
                        copyPixel(pixels, 8, 4, x, y);
                        break;
                    case "1100":
                        copyPixel(pixels, 0, 8, x, y);
                        break;
                    case "1101":
                        copyPixel(pixels, 4, 8, x, y);
                        break;
                    case "1110":
                        copyPixel(pixels, 8, 8, x, y);
                        break;
                    case "1111":
                        copyPixel(pixels, 0, 16, x, y);
                        break;
                    }
                    x += 4;
                }
                prevLine = line.toString();
                tempLine = new StringBuilder();
                line = new StringBuilder();
                x = xstart;
                y++;
            }
        }
    }

    /** Reads dword little endian. */
    private static int wordConvert(int n, String data, int[] pos) {
        String s = data.substring(pos[0], pos[0] + n);
        pos[0] += n;
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i > 0; i -= 8) {
            sb.append(s, i - 8, i);
        }
        return Integer.parseInt(sb.toString(), 2);
    }

    /** Copys pixel info. */
    private static void copyPixel(Color[][] fieldBuffer, int ox, int oy, int x, int y){
        for (int i = 0; i < 4; i++) {
            fieldBuffer[x + i][y] = fieldBuffer[x - ox + i][y - oy];
        }
    }

    /** Draws image. */
    private BufferedImage drawImage() {
        BufferedImage image = new BufferedImage((int) (width * xScale), (int) (height * yScale), BufferedImage.TYPE_INT_RGB);
Debug.printf(Level.FINE, "image: %dx%d (%dx%d)", width, height, image.getWidth(), image.getHeight());
        Graphics2D g2d = image.createGraphics();
        g2d.scale(xScale, yScale);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pixels[x][y] != null) {
                    g2d.setColor(pixels[x][y]);
                    g2d.fillRect(x, y, 1, 1);
                }
            }
        }
        return image;
    }

    /** Allocates buffer. */
    private void ensureBuffer() {
        pixels = new Color[width][];
        for (int x = 0; x < width; x++) {
            pixels[x] = new Color[height];
        }
    }

    /** Checks image type. */
    private static boolean isMag(String s) {
        if (s.startsWith("01001101")) { // M
            if (s.startsWith("01000001010010110100100100110000", 8)) {
                return s.startsWith("001100100010000000100000", 40);
            }
        }
        return false;
    }
}