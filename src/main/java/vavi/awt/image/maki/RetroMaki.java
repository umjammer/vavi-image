/*
 * http://raimeiji.s1006.xrea.com/labo/pmloader/index.html
 */

package vavi.awt.image.maki;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import vavi.util.ByteUtil;
import vavi.util.Debug;


/**
 * MAKI file loader.
 *
 * @author Asuka Raimeiji（@raimeiji）
 * @version 2014-04-28 1.00
 *          2014-05-01 1.50 enabled to show comment using encoding converter library
 *          2014-05-19 1.51 performance up
 */
public class RetroMaki {

    private Color[][] pixels;
    private int width, height;
    private String comment;
    private float xScale;
    private float yScale;

    /** entry point */
    public BufferedImage mainProcess(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = is.read()) != -1) {
            String s = "00000000" + Integer.toBinaryString(b);
            sb.append(s.substring(s.length() - 8));
        }
        comment = "";
        xScale = 1;
        yScale = 1;
        if (!isMaki(sb.substring(0, 64))) {
            throw new IllegalArgumentException("unsupported type");
        }
        loadMaki(sb.toString());
        return drawImage();
    }

    /** Makes comment string */
    private static String comment(byte[] ba) {
        int p = ByteUtil.indexOf(ba, (byte) 0);
        return new String(ba, 0, p == -1 ? ba.length : p, Charset.forName("Shift_JIS"));
    }

    /** Loads MAKI file. */
    private void loadMaki(String data) {

        // header
        // line for XOR
        int xorLine;
        if (data.startsWith("01000001", 48)) {
            xorLine = 2;
        } else {
            xorLine = 4;
        }
        // type, user
        List<Byte> commentBuffer = new ArrayList<>();
        int pos;
        for (pos = 64; pos < 256; pos += 8) {
            String b = data.substring(pos, pos + 8);
            if (b.equals("00011010")) {
                break;
            }
            commentBuffer.add((byte) Integer.parseInt(b, 2));
        }
        if (commentBuffer.size() != 0) {
            comment = "SAVER/USER:\n" + comment(ByteUtil.toByteArray(commentBuffer));
Debug.println(Level.FINE, "\n" + comment);
        }
        // flag B, pixel data start position
        int flagB = 8768;
        int pixel = flagB + Integer.parseInt(data.substring(256, 272), 2) * 8;
        // extra flag, display position
        if (data.charAt(319) != '0') {
            throw new IllegalStateException("wrong 319-320");
        }
        if (!data.startsWith("00000000000000000000000000000000", 320)) {
            throw new IllegalStateException("wrong 320-336");
        }
        if (!data.startsWith("00000010100000000000000110010000", 352)) {
            throw new IllegalStateException("wrong 352-382");
        }

        // allocate buffer
        width = 640;
        height = 400;
        ensureBuffer();
        Color[] colorPalette = new Color[16];

        // read palette
        pos = 384;
        for (int i = 0; i < 16; i++) {
            int r, g, b;
            r = Integer.parseInt(data.substring(pos + 8, pos + 12), 2) * 17;
            g = Integer.parseInt(data.substring(pos, pos + 4), 2) * 17;
            b = Integer.parseInt(data.substring(pos + 16, pos + 20), 2) * 17;
            colorPalette[i] = new Color(r, g, b);
            pos += 24;
        }

        String[][] fieldBuffer = new String[width][height];

        // compression
        String[] lines = new String[] {"", "", "", ""};
        int x = 0;
        int y = 0;
        for (int i = 768; i < 8768; i++) {
            // flags
            if (data.charAt(i) == '0') {
                for (int l = 0; l < 4; l++) {
                    lines[l] += "0000";
                }
            } else {
                for (int l = 0; l < 4; l++) {
                    lines[l] += data.substring(flagB, flagB + 4);
                    flagB += 4;
                }
            }
            if (lines[0].length() == 320) {
                // pixels
                for (int l = 0; l < 4; l++) {
                    for (int p = 0; p < 320; p++) {
                        if (lines[l].charAt(p) == '0') {
                            fieldBuffer[x][y + l] = fieldBuffer[x + 1][y + l] = "0000";
                        } else {
                            fieldBuffer[x][y + l] = data.substring(pixel, pixel + 4);
                            pixel += 4;
                            fieldBuffer[x + 1][y + l] = data.substring(pixel, pixel + 4);
                            pixel += 4;
                        }
                        x += 2;
                    }
                    x = 0;
                }
                y += 4;
                lines = new String[] {"", "", "", ""};
            }
        }

        // XOR above line
        for (y = xorLine; y < height; y++) {
            for (x = 0; x < width; x++) {
                int xorBuf = Integer.parseInt(fieldBuffer[x][y], 2) ^ Integer.parseInt(fieldBuffer[x][y - xorLine], 2);
                String s = "0000" + Integer.toBinaryString(xorBuf);
                fieldBuffer[x][y] = s.substring(s.length() - 4);
            }
        }

        // set colors to buffer
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                this.pixels[x][y] = colorPalette[Integer.parseInt(fieldBuffer[x][y], 2)];
            }
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
    private static boolean isMaki(String s) {
        if (s.startsWith("01001101")) { // M
            if (s.startsWith("01000001010010110100100100110000", 8)) {
                String sub = s.substring(40, 64);
                return sub.equals("001100010100000100100000") ||
                       sub.equals("001100010100001000100000");
            }
        }
        return false;
    }
}