/*
 * http://raimeiji.s1006.xrea.com/labo/pmloader/index.html
 */

package vavi.awt.image.pic;

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
 * PIC file loader.
 *
 * @author Asuka Raimeiji（@raimeiji）
 * @version 2014-04-28 1.00
 *          2014-05-01 1.50 enabled to show comment using encoding converter library
 *          2014-05-19 1.51 performance up
 */
public class RetroPic {

    private Object[][] pixels;
    private int width, height;
    private String comment;
    private float xScale;
    private float yScale;
    int platform;

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
        if (!isPic(sb.substring(0, 64))){
            throw new IllegalArgumentException("unsupported type");
        }
        loadPic(sb.toString());
        return drawImage();
    }

    /** Loads PIC file */
    private void loadPic(String data) {
        // header
        int pos = 16;
        List<Byte> commentBuffer = new ArrayList<>();
        boolean hasHdExtension = false;
        int len = data.length();
        // comment area
        String s;
        while ((pos += 8) <= len) {
            s = data.substring(pos, pos + 8);
            if (s.equals("00011010")) {
                break;
            }
            commentBuffer.add((byte) Integer.parseInt(s, 2));
        }
        if (commentBuffer.size() != 0) {
            comment = comment(ByteUtil.toByteArray(commentBuffer));
            if (comment.startsWith("/MM/")) {
                // extended comment
                if (comment.contains("XSS/")) {
                    xScale = 1;
                }
                comment = comment.substring(comment.indexOf(":") + 1);
            }
Debug.println(Level.FINE, "COMMENT:\n" + comment);
        }
        while ((pos += 8) <= len) {
            if (data.startsWith("00000000", pos)) {
                pos += 16;
                break;
            }
        }
        // platform, mode
        s = data.substring(pos, pos + 4);
        int mode = Integer.parseInt(s, 2);
        s = data.substring(pos + 4, pos + 8);
        platform = Integer.parseInt(s, 2);
Debug.printf(Level.FINE, "platform: %d, mode: %d", platform, mode);
        boolean isHR;
        boolean pseudo256;
        switch (platform) {
        case 0x0: // X68k 16 -> 1:1 16< -> 16:9 GGGGGRRRRRBBBBBI
            xScale = 1.5f;
            break;
        case 0x1: // PC-88VA 256 -> GGGRRRBB, 4096 -> GGGGRRRRBBBB, 65536 -> GGGGGGRRRRRBBBBB
            isHR = (mode & 0x01) != 0;
            pseudo256 = (mode & 0x02) != 0;
Debug.printf(Level.FINE, "isHR: %s, pseudo256: %s", isHR, pseudo256);
            break;
        case 0x2: // FM-TOWNS 1:1 32768 GGGGGRRRRRBBBBB
            break;
        case 0x3: // MAC 1:1 15 RRRRRGGGGGBBBBB
            break;
        case 0xf:
            hasHdExtension = true;
            if (mode == 0) {
                xScale = 1;
            }
            break;
        default:
            throw new IllegalArgumentException("unknown platform: " + s);
        }
        pos += 8;
        // color depth
        int depth = Integer.parseInt(data.substring(pos, pos + 16), 2);
Debug.println(Level.FINE, "depth: " + depth);
        if (depth != 15 && depth != 16) {
            throw new IllegalArgumentException("unsupported color depth: " + depth);
        }
        pos += 16;
        // size
        width = Integer.parseInt(data.substring(pos, pos + 16), 2);
        pos += 16;
        height = Integer.parseInt(data.substring(pos, pos + 16), 2);
        pos += 16;
Debug.printf(Level.FINE, "size: %dx%d", width, height);
        int pixels = width * height;
        if (pixels == 0) {
            throw new IllegalArgumentException("no pixels");
        }
        if (platform == 1) {
            if (height == 200) {
Debug.printf(Level.FINE, "set height 400");
                height = 400;
            }
        }
        // skip extension part
        int xs, ys, rx, ry, pbl; // TODO implement
        if (hasHdExtension) {
Debug.printf(Level.FINE, "has extension");
            pos += 48;
        }

        // allocate buffer
        ensureBuffer();
        String[] colorPalette = new String[128];
        int[] colorSub = new int[128];
        for (int i = 0; i < 128; i++) {
            colorPalette[i] = "";
            colorSub[i] = 0;
        }
        colorPalette[0] = "000000000000000";

        Color color = null;
        Color color2 = null;

        // compression
        int length = -1;
        StringBuilder lngs = new StringBuilder();
        int colnew = 0;
        List<Integer> chains = new ArrayList<>();
        int state = 0;
        while (pos < len - 1) {
            String one = data.substring(pos, pos + 1);
            pos++;
            switch (state) {
            case 0: // length
                lngs.append(one);
                if (one.equals("0")) {
                    int lt = 0;
                    int ll = lngs.length();
                    for (int i = 0; i < ll; i++) {
                        lt += (int) Math.pow(2, i);
                    }
                    lt += Integer.parseInt(data.substring(pos, pos + ll), 2);
                    length += lt;
                    pos += ll;
                    lngs.setLength(0);
                    state = 1;
                }
                break;
            case 1: // color
                colnew++;
                String col;
                if (one.equals("0")) {
                    col = data.substring(pos, pos + depth);
                    int colold = colorSub[0];
                    int c;
                    for (c = 0; c < 128; c++) {
                        if (colorPalette[c].isEmpty()) {
                            colorSub[c] = colnew;
                            colorPalette[c] = col;
                            break;
                        }
                        if (colorSub[c] < colold) {
                            colold = colorSub[c];
                        }
                    }
                    if (c == 128) {
                        for (c = 0; c < 128; c++) {
                            if (colorSub[c] == colold) {
                                colorSub[c] = colnew;
                                colorPalette[c] = col;
                                break;
                            }
                        }
                    }
                    pos += depth;
                } else {
                    col = data.substring(pos, pos + 7);
                    colorSub[Integer.parseInt(col, 2)] = colnew;
                    col = colorPalette[Integer.parseInt(col, 2)];
                    pos += 7;
                }
                int r, g, b;
                switch (depth) {
                case 15:
                    r = convertColor(col.substring(5, 10), depth);
                    g = convertColor(col.substring(0, 5), depth);
                    b = convertColor(col.substring(10, 15), depth);
                    color = new Color(r, g, b);
                    break;
                case 16:
                    if (platform == 1) {
                        color = getG3R3B2Color(Integer.parseInt(col.substring(0, 8), 2));
                        color2 = getG3R3B2Color(Integer.parseInt(col.substring(8, 16), 2));
                    } else {
                        r = convertColor(col.substring(5, 10) + col.substring(15), depth);
                        g = convertColor(col.substring(0, 5) + col.substring(15), depth);
                        b = convertColor(col.substring(10, 15) + col.substring(15), depth);
                        color = new Color(r, g, b);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("depth: " + depth);
                }
                state = 2;
                break;
            case 2: // chain
                if (one.equals("1")) {
                    int i = 0;
                    while (pos <= len) {
                        one = data.substring(pos, pos + 2);
                        pos += 2;
                        switch (one) {
                        case "01":
                            set(chains, i, -1);
                            break; // left1
                        case "10":
                            set(chains, i, 0);
                            break; // center
                        case "11":
                            set(chains, i, 1);
                            break; // right1
                        case "00":
                            one = data.substring(pos, pos + 1);
                            pos++;
                            if (one.equals("1")) {
                                switch (data.substring(pos, pos + 1)) {
                                case "0":
                                    set(chains, i, -2);
                                    break; // left2
                                case "1":
                                    set(chains, i, 2);
                                    break; // right2
                                }
                                pos++;
                            } else {
                                i = -1;
                            }
                            break;
                        }
                        if (i == -1) {
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                // set buffer
                int x = length % width;
                int y = (int) Math.floor(length / (float) width);
                if (platform == 1) {
                    this.pixels[x][y] = new Color[] {color, color2};
                } else {
                    this.pixels[x][y] = color;
                }
                for (int chain : chains) {
                    x += chain;
                    y++;
                    if (platform == 1) {
                        this.pixels[x][y] = new Color[] {color, color2};
                    } else {
                        this.pixels[x][y] = color;
                    }
                }
                chains.clear();
                state = 0;
                break;
            }
            if (length >= pixels) {
                break;
            }
        }
        // fill buffer
        Object prevColor = null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (this.pixels[x][y] == null) {
//Debug.printf("here: %d, %d, %s", x, y, prevColor);
                    this.pixels[x][y] = prevColor;
                } else {
                    prevColor = this.pixels[x][y];
                }
            }
        }
    }

    /** fill list blank until index before set value */
    private static void set(List<Integer> l, int index, int value) {
        if (index < l.size()) {
            l.set(index, value);
        } else {
            for (int i = l.size(); i < index; i++) {
                l.add(i, null);
            }
            l.add(index, value);
        }
    }

    /** Converts string to color */
    private static int convertColor(String data, int colorDepth) {
        switch (colorDepth) {
        case 15:
            return (int) Math.round(Integer.parseInt(data, 2) * 8.22);
        case 16:
            return (int) Math.round(Integer.parseInt(data, 2) * 4.04);
        default:
            throw new IllegalArgumentException(String.valueOf(colorDepth));
        }
    }

    /** */
    private static Color getG3R3B2Color(int c) {
        return new Color((c & 28) * 73 >> 3 << 16 | (c >> 5) * 73 >> 1 << 8 | (c & 3) * 85);
    }

    /** Makes comment string */
    private static String comment(byte[] ba) {
        int p = ByteUtil.indexOf(ba, (byte) 0);
        return new String(ba, 0, p == -1 ? ba.length : p, Charset.forName("Shift_JIS"));
    }

    /** Draws image. */
    private BufferedImage drawImage() {
        BufferedImage image = new BufferedImage((int) (width * xScale), (int) (height * yScale), BufferedImage.TYPE_INT_RGB);
Debug.printf(Level.FINE, "image: %dx%d (%dx%d)", width, height, image.getWidth(), image.getHeight());
        Graphics2D g2d = image.createGraphics();
        g2d.scale(xScale, yScale);
        if (platform == 1) {
            for (int y = 0; y < height / 2; y++) {
                for (int x = 0; x < width / 2; x++) {
                    g2d.setColor(((Color[]) pixels[x][y])[1]);
                    g2d.fillRect(x * 2, y * 2, 1, 1);
                    g2d.setColor(((Color[]) pixels[x][y])[0]);
                    g2d.fillRect(x * 2 + 1, y * 2, 1, 1);
                }
                for (int x = 0; x < width / 2; x++) {
                    g2d.setColor(((Color[]) pixels[width / 2 + x][y])[1]);
                    g2d.fillRect(x * 2, y * 2 + 1, 1, 1);
                    g2d.setColor(((Color[]) pixels[width / 2 + x][y])[0]);
                    g2d.fillRect(x * 2 + 1, y * 2 + 1, 1, 1);
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (pixels[x][y] != null) {
                        g2d.setColor((Color) pixels[x][y]);
                        g2d.fillRect(x, y, 1, 1);
                    }
                }
            }
        }
        return image;
    }

    /** Allocates buffer. */
    private void ensureBuffer() {
        pixels = new Object[width][];
        for (int x = 0; x < width; x++) {
            pixels[x] = new Object[height];
        }
    }

    /** Checks image type. */
    private static boolean isPic(String s) {
        if (s.startsWith("01010000")) { // P
            return s.startsWith("0100100101000011", 8);
        }
        return false;
    }
}