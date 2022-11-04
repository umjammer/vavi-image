/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.ico;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import vavi.awt.image.bmp.WindowsBitmap;
import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;


/**
 * Windows の Icon 形式です．
 *
 * <pre><code>
 *
 *
 *   --- TOF FILE header ( 6 bytes ) --- ... Header
 *   [a header]
 *   --- 1 / count of ICON/CURSOR header ( 16 bytes ) --- ... WindowsIconDevice
 *   [one device]
 *   --- 2 / count ---
 *   [one device]
 *         :
 *         :
 *
 *
 * </code></pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 *          1.01 010817 nsano use Header class <br>
 *          1.10 010901 nsano refine <br>
 *          1.11 021104 nsano fix color related <br>
 */
public class WindowsIcon {

    /** アイコンデバイス */
    private WindowsIconDevice device;

    /** アイコンのビットマップ */
    private WindowsBitmap bitmap;

    /** アイコンのビットマップ */
    private BufferedImage image;

    /** マスク */
    private byte mask[];

    /** アイコンを作成します． */
    public WindowsIcon(WindowsIconDevice device, WindowsBitmap bitmap) {
        this.device = device;
        this.bitmap = bitmap;
        createMask();
    }

    /** アイコンを作成します． */
    public WindowsIcon(WindowsIconDevice device, BufferedImage image) {
        this.device = device;
        this.image = image;
    }

    /** アイコンデバイスを取得します． */
    public WindowsIconDevice getDevice() {
        return device;
    }

    /** アイコンのビットマップを取得します． */
    public BufferedImage getImage() {
        return image;
    }

    /** アイコンのビットマップを取得します． */
    public WindowsBitmap getBitmap() {
        return bitmap;
    }

    /** アイコンのマスクを取得します． */
    public byte[] getMask() {
        return mask;
    }

    /**
     * マスクを作成します． マスクのデータの幅は 4 の倍数になるようにパディングされている． Y 軸は逆転して入ってる．
     */
    private void createMask() {
        int off = bitmap.getImageSize() + bitmap.getOffset();
        int size = bitmap.getSize() - off;
//Debug.println("mask: " + size);
//Debug.println(bitmap.getWidth() + ", " + bitmap.getHeight());

        byte[] buf = new byte[size]; // データののマスクサイズ
        for (int i = 0; i < size; i++) {
            buf[i] = bitmap.getBitmap()[bitmap.getImageSize() + i];
        }

//Debug.dump(buf);

        // パディングを取り除いたマスク
        mask = new byte[(bitmap.getWidth() / 8) * bitmap.getHeight()];
//Debug.println("real: " + (bitmap.getWidth() / 8) * bitmap.getHeight());

        int m = (((bitmap.getWidth() / 8) + 3) / 4) * 4; // パディング入りの幅
//Debug.println("x: " + m);
        int i = 0;
top:    for (int y = bitmap.getHeight() - 1; y >= 0; y--) { // Y 軸を逆転
            for (int x = 0; x < m; x++) {
                if (x < bitmap.getWidth() / 8) {
//Debug.println(y * m + x);
                    if (i < size) {
                        mask[i++] = buf[y * m + x];
//System.err.print(Debug.toBits(mask[i-1]));
                    } else {
                        break top;
                    }
                }
            }
// System.err.println();
        }
    }

    /**
     * アイコンファイルのヘッダを表すクラスです．
     *
     * <pre>
     *
     *  WORD  dummy
     *  WORD  type  1: icon
     *  WORD  count
     *
     * </pre>
     */
    private static final class Header {
        /** タイプ */
        int type;

        /** アイコンデバイスの数 */
        int number;

        /**
         * ストリームからファイルヘッダのインスタンスを作成します．
         */
        static Header readFrom(LittleEndianDataInputStream lin) throws IOException {

            Header h = new Header();

            @SuppressWarnings("unused")
            int dummy;

            // 6 bytes
            dummy = lin.readByte();
            dummy = lin.readByte();
            h.type = lin.readShort();
            h.number = lin.readShort();

            return h;
        }

        /** for debug */
        public String toString() {
            return "type: " + type +
                    ", has: " + number;
        }
    }

    /**
     * 指定したデバイスのアイコンをストリームから読み込みます．
     */
    private static WindowsIcon readIcon(LittleEndianDataInputStream lin, WindowsIconDevice iconDevice) throws IOException {

        int offset = iconDevice.getOffset();
        int size = iconDevice.getSize();

        if (iconDevice.getWidth() == 0 && iconDevice.getHeight() == 0) {
            byte[] buf = new byte[size];
            DataInputStream dis = new DataInputStream(lin);
            dis.readFully(buf);
//System.err.println(StringUtil.getDump(buf, 128));
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            BufferedImage image = ImageIO.read(bais);
Debug.println(image);

            return new WindowsIcon(iconDevice, image);
        } else {
            return new WindowsIcon(iconDevice, WindowsBitmap.readFrom(lin, offset, size));
        }
    }

    /**
     * ストリームからアイコンのインスタンスを作成します．
     */
    public static WindowsIcon[] readFrom(InputStream in) throws IOException {

        LittleEndianDataInputStream lin = new LittleEndianDataInputStream(in);

        WindowsIcon[] icons;
        WindowsIconDevice[] iconDevices;

        Header h = Header.readFrom(lin);
Debug.println(h);
        icons = new WindowsIcon[h.number];
        iconDevices = WindowsIconDevice.readFrom(lin, h.number);

        for (int i = 0; i < h.number; i++) {
            icons[i] = readIcon(lin, iconDevices[i]);
//if (iconDevices[i].getColors() == 0) {
// icons[i].bitmap.setUsedColor(256);
// icons[i].bitmap.setBits(8);
//}
        }

        return icons;
    }
}

/* */
