/*
 * http://raimeiji.s1006.xrea.com/labo/pmloader/index.html
 */

package vavi.awt.image.pi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * Pi file loader.
 *
 * @author Asuka Raimeiji（@raimeiji）
 * @version 2014-04-28 1.00
 *          2014-05-01 1.50 enabled to show comment using encoding converter library
 *          2014-05-19 1.51 performance up
 */
public class RetroPi {

    private static final Logger logger = getLogger(RetroPi.class.getName());

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
        if (!isPi(sb.substring(0, 64))) {
            throw new IllegalArgumentException("unsupported type");
        }
        loadPi(sb.toString());
        return drawImage();
    }

    /** Loads Pi file */
    private void loadPi(String data) {

        // header
        int[] pos = new int[] {8};
        List<Byte> commentBuffer = new ArrayList<>();
        int len = data.length();
        // comment
        while ((pos[0] += 8) <= len) {
            String b = data.substring(pos[0], pos[0] + 8);
            if (b.equals("00011010")) {
                break;
            }
            commentBuffer.add((byte) Integer.parseInt(b, 2));
        }
        if (!commentBuffer.isEmpty()) {
            comment = comment(ByteUtil.toByteArray(commentBuffer));
logger.log(Level.DEBUG, "COMMENT:\n" + comment);
        }
        while ((pos[0] += 8) <= len) {
            if (data.startsWith("00000000", pos[0])) {
                break;
            }
        }
        // platform
        String platform = data.substring(pos[0] += 8, pos[0] + 1);
        // xScale ratio
        String buf = data.substring(pos[0] += 8, pos[0] + 16);
        if (!buf.equals("0000000000000000") && !buf.equals("0000000100000001")) {
            throw new IllegalArgumentException("wrong xScale ratio: " + buf);
        }
        // plane count
        buf = data.substring(pos[0] += 16, pos[0] + 8);
        if (!buf.equals("00000100")) {
            throw new IllegalArgumentException("wrong plane number: " + buf);
        }
        // type
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append((char) Integer.parseInt(data.substring(pos[0] += 8, pos[0] + 8), 2));
        }
        comment = sb + comment;
logger.log(Level.DEBUG, "SAVER:\n" + comment);
        int l = Integer.parseInt(data.substring(pos[0] += 8, pos[0] + 16), 2);
        pos[0] += 8;
        for (int i = 0; i < l; i++) {
            pos[0] += 8;
        }
        // size
        width = Integer.parseInt(data.substring(pos[0] += 8, pos[0] + 16), 2);
        height = Integer.parseInt(data.substring(pos[0] += 16, pos[0] + 16), 2);
        if (width <= 2) {
            throw new IllegalArgumentException("wrong x size");
        }
        int xxCount = width * height;
        if (xxCount == 0) {
            throw new IllegalArgumentException("wrong y size");
        }

        // allocate buffer
        height += 2;
        ensureBuffer();
        Color[] colorPalette = new Color[16];
        int[][] colorSub = new int[16][];
        for (int i = 0; i < 16; i++) {
            int t = i;
            colorSub[i] = new int[16];
            for (int j = 0; j < 16; j++) {
                colorSub[i][j] = t;
                if (--t == -1) {
                    t = 15;
                }
            }
        }

        // read palette
        if (platform.equals("0")) {
            pos[0] += 8;
            for (int c = 0; c < 16; c++) {
                int r, g, b;
                r = Integer.parseInt(data.substring(pos[0] += 8, pos[0] + 4), 2) * 17;
                g = Integer.parseInt(data.substring(pos[0] += 8, pos[0] + 4), 2) * 17;
                b = Integer.parseInt(data.substring(pos[0] += 8, pos[0] + 4), 2) * 17;
                colorPalette[c] = new Color(r, g, b);
            }
            pos[0] += 8;
        } else {
            pos[0] += 16;
            colorPalette[0] = new Color(0,0,0);
            colorPalette[1] = new Color(0,0,112);
            colorPalette[2] = new Color(112,0,0);
            colorPalette[3] = new Color(112,0,112);
            colorPalette[4] = new Color(0,112,0);
            colorPalette[5] = new Color(0,112,112);
            colorPalette[6] = new Color(12,112,0);
            colorPalette[7] = new Color(112,112,112);
            colorPalette[8] = new Color(0,0,0);
            colorPalette[9] = new Color(0,0,240);
            colorPalette[10] = new Color(240,0,0);
            colorPalette[11] = new Color(240,0,240);
            colorPalette[12] = new Color(0,240,0);
            colorPalette[13] = new Color(0,240,240);
            colorPalette[14] = new Color(240,240,0);
            colorPalette[15] = new Color(240,240,240);
        }

        int[][] fieldBuffer = new int[width][height];

        // compression part
        int xx = width * 2;
        xxCount += xx;
        StringBuilder lngs = new StringBuilder();
        int[] col = new int[2];
        int[] o00ck = new int[2];
        String prevPos = "";
        int state = 0;
        int[] prevColor = new int[1];
        // first dot color
        for (int i = 0; i < 2; i++) {
            col[i] = o00ck[i] = getPalette(data, pos, prevColor, colorSub);
            if (col[i] == -1) {
                throw new IllegalStateException("wrong color");
            }
        }
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < width; x += 2) {
                fieldBuffer[x][y] = col[0];
                fieldBuffer[x + 1][y] = col[1]; // out of bounds upper 2 line
            }
        }
        String position = null;
        while (pos[0] <= len) {
            switch (state) {
            case 0: // position
                position = data.substring(pos[0], pos[0] + 2);
                pos[0] += 2;
                if (position.equals("11")) {
                    position += data.substring(pos[0], pos[0] + 1);
                    pos[0]++;
                }
                if (!position.equals(prevPos)) {
                    state = 1;
                } else {
                    state = 2;
                }
                break;
            case 1: // length
                String b = data.substring(pos[0], pos[0] + 1);
                pos[0]++;
                lngs.append(b);
                if (b.equals("0")) {
                    int length;
                    if (lngs.toString().equals("0")) {
                        length = 1;
                    } else {
                        int ll = lngs.length() - 1;
                        length = (int) Math.pow(2, ll);
                        length += Integer.parseInt(data.substring(pos[0], pos[0] + ll), 2);
                        pos[0] += ll;
                    }
                    int offset = 0;
                    switch (position) {
                    case "00": // left
                        if (o00ck[0] == o00ck[1]) {
                            offset = -2;
                        } else {
                            offset = -4;
                        }
                        break;
                    case "01":
                        offset = -width;
                        break; // above
                    case "10":
                        offset = -width * 2;
                        break; // above 2
                    case "110":
                        offset = -width + 1;
                        break; // above right
                    case "111":
                        offset = -width - 1;
                        break; // above left
                    }
                    for (int i = 0; i < length * 2; i++) {
                        int x = (xx + i) % width;
                        int y = (int) Math.floor((xx + i) / (float) width);
                        int ox = (xx + i + offset) % width;
                        int oy = (int) Math.floor((xx + i + offset) / (float) width);
                        fieldBuffer[x][y] = fieldBuffer[ox][oy];
                        prevColor[0] = fieldBuffer[x][y];
                        o00ck[0] = o00ck[1];
                        o00ck[1] = fieldBuffer[x][y];
                    }
                    xx += length * 2;
                    lngs = new StringBuilder();
                    prevPos = position;
                    state = 0;
                }
                break;
            case 2: // color
                for (int i = 0; i < 2; i++) {
                    col[i] = o00ck[i] = getPalette(data, pos, prevColor, colorSub);
                    if (col[i] == -1) {
                        throw new IllegalStateException("wring color");
                    }
                    int x = xx % width;
                    int y = (int) Math.floor(xx / (float) width);
                    fieldBuffer[x][y] = col[i];
                    xx++;
                }
                prevPos = "";
                state = 3;
                break;
            case 3: // continue
                b = data.substring(pos[0], pos[0] + 1);
                pos[0]++;
                if (b.equals("0")) {
                    state = 0;
                } else {
                    state = 2;
                }
                break;
            }
            if (xx >= xxCount) {
                break;
            }
        }

        // set colors to buffer
        height -= 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.pixels[x][y] = colorPalette[fieldBuffer[x][y + 2]];
            }
        }
    }

    /** Gets palette number. */
    private static int getPalette(String data, int[] pos, int[] prevColor, int[][] colorSub) {
        int palette;
        String two = data.substring(pos[0], pos[0] + 2);
        pos[0] += 2;
        switch (two) {
        case "10":
            palette = 0;
            break;
        case "11":
            palette = 1;
            break;
        case "00":
            String b = data.substring(pos[0], pos[0] + 1);
            pos[0]++;
            if (b.equals("0")) {
                palette = 2;
            } else {
                palette = 3;
            }
            break;
        case "01":
            b = data.substring(pos[0], pos[0] + 1);
            pos[0]++;
            if (b.equals("0")) {
                palette = Integer.parseInt(data.substring(pos[0], pos[0] + 2), 2) + 4;
                pos[0] += 2;
            } else {
                palette = Integer.parseInt(data.substring(pos[0], pos[0] + 3), 2) + 8;
                pos[0] += 3;
            }
            break;
        default:
            throw new IllegalArgumentException("never happen");
        }
        int prev = colorSub[prevColor[0]][palette];
        colorSub[prevColor[0]] = splice(colorSub[prevColor[0]], palette, 1);
        colorSub[prevColor[0]] = unshift(colorSub[prevColor[0]], prev);
        prevColor[0] = prev;
        return prev;
    }

    /** ecmascript #splice() */
    private static int[] splice(int[] a, int start, int deleteCount) {
        int[] n = new int[a.length - deleteCount];
        System.arraycopy(a, 0, n, 0, start);
        System.arraycopy(a, start + deleteCount, n, start, a.length - (start + deleteCount));
        return n;
    }

    /** ecmascript #unshift() */
    private static int[] unshift(int[] a, int v) {
        int[] n = new int[a.length + 1];
        System.arraycopy(a, 0, n, 1, a.length);
        n[0] = v;
        return n;
    }

    /** Makes comment string */
    private static String comment(byte[] ba) {
        int p = ByteUtil.indexOf(ba, (byte) 0);
        return new String(ba, 0, p == -1 ? ba.length : p, Charset.forName("Shift_JIS"));
    }

    /** Draws image. */
    private BufferedImage drawImage() {
        BufferedImage image = new BufferedImage((int) (width * xScale), (int) (height * yScale), BufferedImage.TYPE_INT_RGB);
logger.log(Level.DEBUG, String.format("image: %dx%d (%dx%d)", width, height, image.getWidth(), image.getHeight()));
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
    private static boolean isPi(String s) {
        if (s.startsWith("01010000")) { // P
            return s.startsWith("01101001", 8);
        }
        return false;
    }
}