/*
 * Copyright (c) 1997-2001 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;


/**
 * Windows の bitmap 形式を表すオブジェクトです．
 *
 * <pre><code>
 *
 *  Top of File
 *
 *   Header         16
 *   BitmapHeader   40
 *   palette        4 x colors (if index color)
 *   bitmap         BitmapHeader.imageSize
 *
 *  End of File
 *
 * </code></pre>
 *
 * @see "Windows TM 3.1 グラフィックプログラミング ISBN4-8443-4628-8 p.187"
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 *          1.01 010817 nsano use Header class <br>
 *          1.02 010819 nsano fix read buffer <br>
 *          1.03 021104 nsano add #setUsedColor for icon <br>
 *          1.04 021104 nsano change getPalettes <br>
 *          1.05 021104 nsano add #setBits for icon <br>
 */
public class WindowsBitmap {

    /** */
    public enum Type {
        RGB,
        /** 圧縮 */
        RLE8,
        /** 圧縮 */
        RLE4,
        /** */
        BITFIELDS
    }

    /** ファイルのヘッダ */
    private Header header;

    /** ビットマップヘッダ */
    private WindowsBitmapHeader bitmapHeader;

    /** イメージのバッファ */
    private byte[] bitmap;

    /** サイズを取得します． */
    public int getSize() {
        return header.bitmapSize;
    }

    /** イメージへのオフセットを取得します． */
    public int getOffset() {
        return header.bitmapOffset;
    }

    /** ビットマップヘッダを取得します． */
    public WindowsBitmapHeader getBitmapHeader() {
        return bitmapHeader;
    }

    /** イメージのバッファを取得します． */
    public byte[] getBitmap() {
        return bitmap;
    }

    /** イメージのバッファを設定します． */
    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }

    /** イメージのヘッダサイズを取得します． */
    public int getHeaderSize() {
        return bitmapHeader.headerSize;
    }

    /** 幅を取得します． */
    public int getWidth() {
        return bitmapHeader.width;
    }

    /** 高さを取得します． */
    public int getHeight() {
        return bitmapHeader.height;
    }

    /** bit of color depth を取得します． */
    public int getBits() {
        return bitmapHeader.bits;
    }

    /** bit of color depth を設定します． */
    public void setBits(int bits) {
        bitmapHeader.bits = bits;
    }

    /** 圧縮方法を取得します． */
    public int getCompression() {
        return bitmapHeader.compression;
    }

    /** イメージのサイズを取得します． */
    public int getImageSize() {
        return bitmapHeader.imageSize;
    }

    /** 使用している色数 0(full)，2, 16, 256 を取得します． */
    public int getUsedColor() {
        return bitmapHeader.usedColor;
    }

    /** 使用している色数 0(full)，2, 16, 256 を設定します． */
    public void setUsedColor(int colors) {
        bitmapHeader.usedColor = colors;
    }

    /** */
    private static final ColorModel system256IndexColorModel;

    /** */
    private static final ColorModel systemBWIndexColorModel;

    /** */
    private static final ColorModel system16IndexColorModel;

    /* */
    static {
        // 2
        byte[] reds = new byte[2];
        byte[] greens = new byte[2];
        byte[] blues = new byte[2];

        reds[0] = (byte) 0x00; greens[0] = (byte) 0x00; blues[1] = (byte) 0x00;
        reds[1] = (byte) 0xff; greens[1] = (byte) 0xff; blues[1] = (byte) 0xff;

        systemBWIndexColorModel = new IndexColorModel(2, 2, reds, greens, blues);

        // 16
        reds = new byte[16];
        greens = new byte[16];
        blues = new byte[16];

        reds[ 0] = (byte) 0x00; greens[ 0] = (byte) 0x00; blues[ 0] = (byte) 0x00;
        reds[ 1] = (byte) 0x00; greens[ 1] = (byte) 0x00; blues[ 1] = (byte) 0xff;
        reds[ 2] = (byte) 0xff; greens[ 2] = (byte) 0x00; blues[ 2] = (byte) 0x00;
        reds[ 3] = (byte) 0xff; greens[ 3] = (byte) 0x00; blues[ 3] = (byte) 0xff;
        reds[ 4] = (byte) 0x00; greens[ 4] = (byte) 0xff; blues[ 4] = (byte) 0x00;
        reds[ 5] = (byte) 0x00; greens[ 5] = (byte) 0xff; blues[ 5] = (byte) 0xff;
        reds[ 6] = (byte) 0xff; greens[ 6] = (byte) 0xff; blues[ 6] = (byte) 0x00;
        reds[ 7] = (byte) 0xff; greens[ 7] = (byte) 0xff; blues[ 7] = (byte) 0xff;
        reds[ 8] = (byte) 0x00; greens[ 8] = (byte) 0x00; blues[ 8] = (byte) 0x7f;
        reds[ 9] = (byte) 0x7f; greens[ 9] = (byte) 0x00; blues[ 9] = (byte) 0x00;
        reds[10] = (byte) 0x7f; greens[10] = (byte) 0x00; blues[10] = (byte) 0x7f;
        reds[11] = (byte) 0x00; greens[11] = (byte) 0x00; blues[11] = (byte) 0x00;
        reds[12] = (byte) 0x00; greens[12] = (byte) 0x7f; blues[12] = (byte) 0x7f;
        reds[13] = (byte) 0x7f; greens[13] = (byte) 0x7f; blues[13] = (byte) 0x00;
        reds[14] = (byte) 0x7f; greens[14] = (byte) 0x7f; blues[14] = (byte) 0x7f;
        reds[15] = (byte) 0x00; greens[15] = (byte) 0x7f; blues[15] = (byte) 0x00;

        system16IndexColorModel = new IndexColorModel(4, 16, reds, greens, blues);

        // 256
        String colorTablePath = null;
//        if ("".equals(System.getProperty("os.type"))) {
            colorTablePath = "/vavi/awt/image/resources/WindowsSystem.ACT";
//        }

        reds = new byte[256];
        greens = new byte[256];
        blues = new byte[256];

        try {
            for (int i = 0; i < 256; i++) {
                InputStream is = WindowsBitmap.class.getResourceAsStream(colorTablePath);

                reds[i] = (byte) is.read();
                greens[i] = (byte) is.read();
                blues[i] = (byte) is.read();
            }
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
        }

        system256IndexColorModel = new IndexColorModel(8, 256, reds, greens, blues);
    }

    /** */
    public ColorModel getColorModel() {
        return bitmapHeader.palette;
    }

    // -------------------------------------------------------------------------

    /**
     * Bitmap ファイルのヘッダ
     *
     * <pre><code>
     *
     *  0  BYTE  'B'
     *  1  BYTE  'M'
     *  2  DWORD size
     *  6  WORD  reserved always 0
     *  8  WORD  reserved always 0
     *  10 DWORD offset
     *
     * </code></pre>
     */
    private static final class Header {
        int bitmapOffset;

        int bitmapSize;

        /**
         * ビットマップファイルのヘッダを読み込みます．
         * @throws IllegalArgumentException not a windows bitmap
         */
        static Header readFrom(LittleEndianDataInputStream lin) throws IOException {

            Header header = new Header();

            byte[] signature = new byte[2];

            // 14 bytes

            lin.readFully(signature);
            if (signature[0] != 'B' || signature[1] != 'M') {
                throw new IllegalArgumentException("not a windows bitmap");
            }
            header.bitmapSize = lin.readInt();
            lin.readShort();
            lin.readShort();
            header.bitmapOffset = lin.readInt();

            return header;
        }

        @SuppressWarnings("unused")
        static int size() {
            return 14;
        }

        public String toString() {
            return "size: " + bitmapSize +
                    ", offset: " + bitmapOffset;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Windows の bitmap header を表すオブジェクトです．
     * 
     * <pre><code>
     * 
     *  DWORD   size            ヘッダのバイト数
     *  LONG    width           幅
     *  LONG    height          高さ
     *  WORD    planes          常に1
     *  WORD    bitCount        1: mono, 4: 16 colors, 8: 256 colors, 24: full colors
     *  DWORD   compression     0: no compression, 1: 8bit/pixel RLE, 2: 4bit/pixel RLE
     *  DWORD   sizeImage       イメージのサイズ
     *  LONG    XPelsPerMeter   X pixels/meter
     *  LONG    YPelsPerMeter   Y pixels/meter
     *  DWORD   ClrUsed         0: see bitCount, else: &lt;24:??? =24:???
     *  DWORD   ClrImportant    表示に必要なインデックス数 0: すべて
     *  
     * </code></pre>
     * 
     * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
     * @version 0.00 970713 nsano initial version <br>
     *          1.00 010731 nsano move readFrom here <br>
     *          1.10 010901 nsano be inner class <br>
     */
    private static final class WindowsBitmapHeader {
        /** ヘッダのサイズ */
        int headerSize;
        /** 幅 */
        int width;
        /** 高さ */
        int height;
        /**  */
        int planes;
        /** bit of color depth */
        int bits;
        /** 圧縮方法 */
        int compression;
        /** イメージのサイズ */
        int imageSize;
        /** 横 pixel/m */
        int ppmX;
        /** 縦 pixel/m */
        int ppmY;
        /** 使用している色数 0(full)，2, 16, 256 */
        int usedColor;
        /**  */
        int importantColor;
        /** パレット */
        ColorModel palette;

        /** for debug */
        public String toString() {
            return "header size: " + headerSize +
                    ", width: " + width +
                    ", height: " + height +
                    ", planes: " + planes +
                    ", bits: " + bits +
                    ", compression: " + compression +
                    ", image size: " + imageSize +
                    ", ppm x: " + ppmX +
                    ", ppm y: " + ppmY +
                    ", color used: " + usedColor +
                    ", color important: " + importantColor;
        }

        /**
         * ストリームからビットマップヘッダのインスタンスを作成します．
         */
        static WindowsBitmapHeader readFrom(LittleEndianDataInputStream lin) throws IOException {

            WindowsBitmapHeader bh = new WindowsBitmapHeader();

            // read 40 bytes
            bh.headerSize = lin.readInt();
            bh.width = lin.readInt();
            bh.height = lin.readInt();
            bh.planes = lin.readShort();
            bh.bits = lin.readShort();
            bh.compression = lin.readInt();
            bh.imageSize = lin.readInt();
            bh.ppmX = lin.readInt();
            bh.ppmY = lin.readInt();
            bh.usedColor = lin.readInt();
            bh.importantColor = lin.readInt();

//Debug.println("bitmap");
//Debug.print(bh);

            if (bh.usedColor == 0) {
                switch (bh.bits) {
                case 1:
                    bh.usedColor = 2;
                    break;
                case 4:
                    bh.usedColor = 16;
                    break;
                case 8:
                    bh.usedColor = 256;
                    break;
                case 24:
                case 32:
                    // DO NOT SET
                    break;
                default:
Debug.println("unknown bits: " + bh.bits);
                    break;
                }
//Debug.println("usedColor: " + bh.usedColor);
            }

//Debug.println("usedColor: " + bh.usedColor);
//Debug.println("bits: " + bh.bits);
            if (bh.usedColor != 0) {
                byte[] reds = new byte[bh.usedColor];
                byte[] greens = new byte[bh.usedColor];
                byte[] blues = new byte[bh.usedColor];
                byte[] alphas = new byte[bh.usedColor];

                for (int i = 0; i < bh.usedColor; i++) {
                    blues[i] = lin.readByte();
                    greens[i] = lin.readByte();
                    reds[i] = lin.readByte();
                    alphas[i] = lin.readByte();
//System.err.print("(" + i + ")");
//bh.palette[i].print();
                }

                bh.palette = new IndexColorModel(bh.bits, bh.usedColor, reds, greens, blues);
            } else { // DirectColorModel
                 if (bh.bits == 24) {
                     bh.palette = new DirectColorModel(bh.bits, 0x0000ff, 0x00ff00, 0xff0000);
                 } else if (bh.bits == 32) {
                     bh.palette = new DirectColorModel(bh.bits, 0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000);
                 } else {
Debug.println("unknown bits: " + bh.bits);
                 }
            }

            if (bh.palette == null) { // IndexColorModel
                 switch (bh.usedColor) {
                 case 2:
//Debug.println("use default bw");
                     bh.palette = systemBWIndexColorModel;
                 case 16:
//Debug.println("use default 16 color");
                     bh.palette = system16IndexColorModel;
                 case 256:
//Debug.println("use system 256 color ");
                     bh.palette = system256IndexColorModel;
                 default:
Debug.println("unknown color size: " + bh.usedColor);
                 }
            }

            lin.skipBytes(bh.headerSize - 40);
if (bh.headerSize - 40 > 0) {
 Debug.println("skip: " + (bh.headerSize - 40));
}

            return bh;
        }
    }

    //----

    /** 24 Bit フルカラービットマップを作成します． */
    public int[] get24BitColorData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        int[] ivram = new int[width * height];

        int count = 0;
        int skip = width * 3 % 4 != 0 ? 4 - (width * 3 % 4) : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            for (int i = 0; i < width; i++) {
                int ub;
                ub = (buffer[count++] & 0xff) << 16; // b
                ub |= (buffer[count++] & 0xff) << 8; // g
                ub |= (buffer[count++] & 0xff); // r
                ivram[ofs + i] = ub;
            }
            count += skip;
        }

        return ivram;
    }

    /** 32 Bit フルカラービットマップを作成します． */
    public int[] get32BitColorData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        int[] ivram = new int[width * height];

        int count = 0;
        int skip = (width * 4) % 4 != 0 ? 4 - (width * 4 % 4) : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            for (int i = 0; i < width; i++) {
                int ub;
                ub = (buffer[count++] & 0xff) << 16; // b
                ub |= (buffer[count++] & 0xff) << 8; // g
                ub |= (buffer[count++] & 0xff); // r
                ub |= (buffer[count++] & 0xff) << 24; // alpha
                ivram[ofs + i] = ub;
            }
            count += skip;
        }

        return ivram;
    }

    /** モノカラービットマップを作成します． */
    public byte[] getMonoColorData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        byte[] vram = new byte[width * height];

        int count = 0;
        int skip = ((width + 7) / 8) % 4 != 0 ? 4 - ((width + 7) / 8) % 4 : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            int d = 0;
            for (int i = 0; i < width / 8; i++) {
                byte b = buffer[count++];
                int mask = 0x80;
                for (int k = 0; k < 8; k++) {
                    vram[ofs + d++] = (byte) ((b & mask) >> (7 - k));
                    mask >>= 1;
                }
            }
            if (width % 8 != 0) {
                byte b = buffer[count++];
                int mask = 0x80;
                for (int k = 0; k < width % 8; k++) {
                    vram[ofs + d++] = (byte) ((b & mask) >> (7 - k));
                    mask >>= 1;
                }
            }
            count += skip;
        }

        return vram;
    }

    /** 16 色ビットマップを作成します． */
    public byte[] get16ColorData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        byte[] vram = new byte[width * height];

        int count = 0;
        int skip = ((width + 1) / 2) % 4 != 0 ? 4 - ((width + 1) / 2) % 4 : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            int d = 0;
            for (int i = 0; i < width / 2; i++) {
                int b = buffer[count++];
                vram[ofs + d++] = (byte) ((b & 0xf0) >> 4);
                vram[ofs + d++] = (byte) (b & 0x0f);
            }
            if (width % 2 != 0) {
                int b = buffer[count++];
                vram[ofs + d] = (byte) ((b & 0xf0) >> 4);
            }
            count += skip;
        }

        return vram;
    }

    /** 16 色圧縮ビットマップを作成します． */
    public byte[] get16ColorRleData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        byte[] vram = new byte[width * height];

        int count = 0;
        int d = 0;
        int ofs = (height-- - 1) * width;

        while (count < getImageSize()) {
            int b1 = buffer[count++] & 0xff;
            int b2 = buffer[count++] & 0xff;
            if (b1 > 0) {
                for (int j = 0; j < b1; j++) {
                    vram[ofs + d++] = (j % 2 == 0) ? (byte) ((b2 & 0xf0) >> 4) : (byte) (b2 & 0x0f);
                }
            } else {
                if (b2 == 0) { // Line End
                    ofs = (height-- - 1) * width;
                    d = 0;
                    if (height < 0) {
                        break;
                    }
                } else if (b2 == 1) { // Bitmap End
                    break;
                } else if (b2 == 2) { // Point Move
                    int b3 = buffer[count++] & 0xff;
                    int b4 = buffer[count++] & 0xff;
                    if (b3 > 0) {
                        d += b3 - 1;
                    }
                    if (b4 > 0) {
                        ofs -= width * b4;
                        height -= b4;
                    }
                } else {
                    int b3 = 0;
                    for (int j = 0; j < b2; j++) {
                        if (j % 2 == 0) {
                            b3 = buffer[count++];
                        }
                        vram[ofs + d++] = (j % 2 == 0) ? (byte) ((b3 & 0xf0) >> 4) : (byte) (b3 & 0x0f);
                    }
                    if (((b2 + 1) / 2) % 2 != 0) {
                        count++;
                    }
                }
            }
        }

        return vram;
    }

    /** 256 色ビットマップを作成します． */
    public byte[] get256ColorData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        byte[] vram = new byte[width * height];

        int count = 0;
        int skip = (width % 4 != 0) ? 4 - width % 4 : 0;

        for (int j = 0; j < height; j++) {
            int ofs = (height - 1 - j) * width;
            for (int i = 0; i < width; i++) {
                vram[ofs + i] = buffer[count++];
            }
            count += skip;
        }

        return vram;
    }

    /** 256 色圧縮ビットマップを作成します． */
    public byte[] get256ColorRleData() {

        int width = getWidth();
        int height = getHeight();
        byte[] buffer = getBitmap();
        byte[] vram = new byte[width * height];

        int count = 0;
        int d = 0;
        int ofs = (height-- - 1) * width;

        while (count < getImageSize()) {
            int b1 = buffer[count++] & 0xff;
            int b2 = buffer[count++] & 0xff;
            if (b1 > 0) {
                for (int j = 0; j < b1; j++) {
                    vram[ofs + d++] = (byte) b2;
                }
            } else {
                if (b2 == 0) { // Line End
                    ofs = (height-- - 1) * width;
                    d = 0;
                    if (height < 0) {
                        break;
                    }
                } else if (b2 == 1) { // Bitmap End
                    break;
                } else if (b2 == 2) { // Point Move
                    int b3 = buffer[count++] & 0xff;
                    int b4 = buffer[count++] & 0xff;
                    if (b3 > 0) {
                        d += b3 - 1;
                    }
                    if (b4 > 0) {
                        ofs -= width * b4;
                        height -= b4;
                    }
                } else {
                    for (int j = 0; j < b2; j++) {
                        vram[ofs + d++] = buffer[count++];
                    }
                    if (b2 % 2 != 0) {
                        count++;
                    }
                }
            }
        }

        return vram;
    }

    // -------------------------------------------------------------------------

    /** ビットマップイメージを読み込みます． */
    private static byte[] readBitmap(LittleEndianDataInputStream in, int num) throws IOException {
        byte[] buf = new byte[num];
        in.readFully(buf, 0, num);
        return buf;
    }

    /**
     * ビットマップをストリームから作成します．
     * <p>
     * 普通のビットマップ用
     * </p>
     */
    public static WindowsBitmap readFrom(InputStream in) throws IOException {

        LittleEndianDataInputStream lin = new LittleEndianDataInputStream(in);

        WindowsBitmap bitmap = new WindowsBitmap();

        bitmap.header = Header.readFrom(lin);
        bitmap.bitmapHeader = WindowsBitmapHeader.readFrom(lin);

        if (bitmap.bitmapHeader.imageSize == 0) {
            bitmap.bitmapHeader.imageSize = bitmap.header.bitmapSize - bitmap.header.bitmapOffset;
//Debug.println(b.bitmapHeader.imageSize);
        }

        bitmap.bitmap = readBitmap(lin, bitmap.bitmapHeader.imageSize);

        return bitmap;
    }

    /**
     * ビットマップをストリームから作成します．
     * <p>
     * アイコン用
     * </p>
     *
     * <pre><code>
     *
     *  Top of Block
     *   BitmapHeader   40
     *   palette        4 x colors (if index color)
     *   bitmap         BitmapHeader.imageSize
     *   mask           size - (header + bitmap + palette)
     *  End of File
     *
     * </code></pre>
     */
    public static WindowsBitmap readFrom(LittleEndianDataInputStream lin, int off, int size) throws IOException {

        WindowsBitmap bitmap = new WindowsBitmap();

        bitmap.header = new Header();

        bitmap.header.bitmapSize = size;

        bitmap.bitmapHeader = WindowsBitmapHeader.readFrom(lin);

        bitmap.bitmapHeader.height /= 2;
        bitmap.header.bitmapOffset = bitmap.bitmapHeader.headerSize + 4 * bitmap.bitmapHeader.usedColor;

//b.header.print();
//b.bitmapHeader.print();
//included mask
//Debug.println("read: " + (b.header.bitmapSize - b.header.bitmapOffset));
        bitmap.bitmap = readBitmap(lin, bitmap.header.bitmapSize - bitmap.header.bitmapOffset);

        return bitmap;
    }
}

/* */
