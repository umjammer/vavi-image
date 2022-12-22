/*
 * http://raimeiji.s1006.xrea.com/labo/pmloader/index.html
 */

package vavi.awt.image.pic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import vavi.util.ByteUtil;
import vavi.util.Debug;


/**
 * PIC/Pi/MAG/MAKI file loader.
 *
 * @author Asuka Raimeiji（@raimeiji）
 * @version 2014-04-28 1.00
 *          2014-05-01 1.50 enabled to show comment using encoding converter library
 *          2014-05-19 1.51 performance up
 */
public class Retro {

    private Color[][] pixels;
    private int width, height;
    private String comment;
    private float xScale;
    private float yScalse;

    /** entry point */
    public BufferedImage mainProcess(String filename) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filename));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String s = "00000000" + Integer.toBinaryString(b);
            sb.append(s.substring(s.length() - 8));
        }
        comment = "";
        xScale = 1;
        yScalse = 1;
        String type = getType(sb.substring(0, 64));
        switch (type) {
        case "pic":
            loadPic(sb.toString());
            break;
        case "pi":
            loadPi(sb.toString());
            break;
        case "mag":
            loadMag(sb.toString());
            break;
        case "maki":
            loadMaki(sb.toString());
            break;
        }
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
Debug.println("COMMENT:\n" + comment);
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
        int platform = Integer.parseInt(s, 2);
Debug.printf(Level.INFO, "platform: %d, mode: %d", platform, mode);
        boolean isHR;
        boolean pseudo256;
        switch (platform) {
        case 0x0: // X68k 16 -> 1:1 16< -> 16:9 GGGGGRRRRRBBBBBI
            xScale = 1.5f;
            break;
        case 0x1: // PC-88VA 256 -> GGGRRRBB, 4096 -> GGGGRRRRBBBB, 65536 -> GGGGGGRRRRRBBBBB
            isHR = (mode & 0x01) != 0;
            pseudo256 = (mode & 0x02) != 0;
Debug.printf(Level.INFO, "isHR: %s, pseudo256: %s", isHR, pseudo256);
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
Debug.println(Level.INFO, "depth: " + depth);
        if (depth != 15 && depth != 16) {
            throw new IllegalArgumentException("unsupported color depth: " + depth);
        }
        pos += 16;
        // size
        width = Integer.parseInt(data.substring(pos, pos + 16), 2);
        pos += 16;
        height = Integer.parseInt(data.substring(pos, pos + 16), 2);
        pos += 16;
Debug.printf(Level.INFO, "size: %dx%d", width, height);
        int pixels = width * height;
        if (pixels == 0) {
            throw new IllegalArgumentException("no pixels");
        }
        if (platform == 1 && height == 200) {
            height = 400;
        }
        // skip extension part
        int xs, ys, rx, ry, pbl; // TODO
        if (hasHdExtension) {
            pos += 48;
        }

        // allocate buffer
        bufferEnsure();
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
                    lngs = new StringBuilder();
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
                        r = Integer.parseInt(col.substring(0, 3), 2);
                        g = Integer.parseInt(col.substring(3, 6), 2);
                        b = Integer.parseInt(col.substring(6, 8), 2);
                        color = getG3R3B2Color(r, g, b);
                        r = Integer.parseInt(col.substring(8, 11), 2);
                        g = Integer.parseInt(col.substring(11, 14), 2);
                        b = Integer.parseInt(col.substring(14, 16), 2);
                        color2 = getG3R3B2Color(r, g, b);
//Debug.printf("%s, %s", color, color2);
                    } else {
                        r = convertColor(col.substring(5, 10) + col.substring(15), depth);
                        g = convertColor(col.substring(0, 5) + col.substring(15), depth);
                        b = convertColor(col.substring(10, 15) + col.substring(15), depth);
                        color = new Color(r, g, b);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("depath: " + depth);
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
                if (platform == 1) {
                    int x = length % (width / 2);
                    int y = (int) Math.floor(length / (width / 2f));
                    this.pixels[x * 2][y] = color;
                    this.pixels[x * 2 + 1][y] = color2;
                    for (int chain : chains) {
                        x += chain;
                        y++;
                        this.pixels[x * 2][y] = color;
                        this.pixels[x * 2 + 1][y] = color2;
                    }
                } else {
                    int x = length % width;
                    int y = (int) Math.floor(length / (float) width);
                    this.pixels[x][y] = color;
                    for (int chain : chains) {
                        x += chain;
                        y++;
                        this.pixels[x][y] = color;
                    }
                }
                chains = new ArrayList<>();
                state = 0;
                break;
            }
            if (length >= pixels) {
                break;
            }
        }

        // fill buffer
        if (platform == 1) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x += 2) {
                    if (this.pixels[x][y] == null) {
                        this.pixels[x][y] = color;
                        this.pixels[x + 1][y] = color2;
                    } else {
                        color = this.pixels[x][y];
                        color2 = this.pixels[x + 1][y];
                    }
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (this.pixels[x][y] == null) {
//Debug.printf("here: %d, %d, %s", x, y, color);
                        this.pixels[x][y] = color;
                    } else {
                        color = this.pixels[x][y];
                    }
                }
            }
        }
    }

    /** fill list blank until index before set value */
    private static void set(List<Integer> l, int index, int value) {
        for (int i = l.size(); i < index; i++) {
            l.add(i, null);
        }
        l.add(index, value);
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

    private static Color getG3R3B2Color(int r, int g, int b) {
        return new Color((r & 0x1c) * 73 >> 3,(g >> 5) * 73 >> 1,(b & 3) * 85);
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
        if (commentBuffer.size() != 0) {
            comment = comment(ByteUtil.toByteArray(commentBuffer));
Debug.println("COMMENT:\n" + comment);
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
Debug.println("SAVER:\n" + comment);
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
        bufferEnsure();
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
Debug.println("SAVER/USER:\n" + comment);
        }
        if (commentBufC.size() != 0) {
            comment = comment(ByteUtil.toByteArray(commentBufC));
Debug.println("COMMENT:\n" + comment);
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
Debug.printf(Level.FINE, "width: %d, height: %d", width, height);
        bufferEnsure();

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
Debug.println("\n" + comment);
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
        bufferEnsure();
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
        BufferedImage image = new BufferedImage((int) (width * xScale), (int) (height * yScalse), BufferedImage.TYPE_INT_RGB);
Debug.printf(Level.FINE, "image: %dx%d (%dx%d)", width, height, image.getWidth(), image.getHeight());
        Graphics2D g2d = image.createGraphics();
        g2d.scale(xScale, yScalse);
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
    private void bufferEnsure() {
        pixels = new Color[width][];
        for (int x = 0; x < width; x++) {
            pixels[x] = new Color[height];
        }
    }

    /** Gets image type. */
    private static String getType(String s) {
        switch (s.substring(0, 8)) {
        case "01010000": // P
            if (s.startsWith("01101001", 8)) {
                return "pi";
            } else if (s.startsWith("0100100101000011", 8)) {
                return "pic";
            }
            break;
        case "01001101": // M
            if (s.startsWith("01000001010010110100100100110000", 8)) {
                switch (s.substring(40, 64)) {
                case "001100100010000000100000":
                    return "mag";
                case "001100010100000100100000":
                case "001100010100001000100000":
                    return "maki";
                }
            }
            break;
        }
        throw new IllegalStateException("unsupported type");
    }
}