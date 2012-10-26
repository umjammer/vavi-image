/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;

import vavi.io.LittleEndianDataInputStream;


/**
 * アイコンの大きさ等の情報を表すクラスです．
 * 
 * <pre>
 * 
 *  BYTE  width
 *  BYTE  height
 *  BYTE  colors - 16: 16 colors, 0: 256 colors
 *  BYTE  reserved - must be 0
 *  WORD  hotspot x
 *  WORD  hotspot y
 *  DWORD size
 *  DWORD offset - ヘッダ(6) + デバイス(16) x 個数 + size x #
 *  
 * </pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 */
public class WindowsIconDevice {

    /** 幅 */
    private int width;

    /** 高さ */
    private int height;

    /** 色数 */
    private int colors;

    /** X ホットスポット */
    private int hotspotX;

    /** Y ホットスポット */
    private int hotspotY;

    /** サイズ */
    private int size;

    /** オフセット */
    private int offset;

    /** #readFrom 用 */
    private WindowsIconDevice() {
    }

    /** アイコンを作成します． */
    public WindowsIconDevice(int width, int height, int colors) {
        this.width = width;
        this.height = height;
        this.colors = colors;
    }

    /** 幅を取得します． */
    public int getWidth() {
        return width;
    }

    /** 高さを取得します． */
    public int getHeight() {
        return height;
    }

    /** 色数を取得します． */
    public int getColors() {
        return colors;
    }

    /** 色数を設定します． */
    public void setColors(int colors) {
        this.colors = colors;
    }

    /** サイズを取得します． */
    public int getSize() {
        return size;
    }

    /** オフセットを取得します． */
    public int getOffset() {
        return offset;
    }

    /** ホットスポットを取得します． */
    public Point getHotspot() {
        return new Point(hotspotX, hotspotY);
    }

    /** for debug */
    public void print() {
        System.err.println(" width: " + width);
        System.err.println(" height: " + height);
        System.err.println(" colors: " + colors);
        System.err.println(" hotspot x: " + hotspotX);
        System.err.println(" hotspot y: " + hotspotY);
        System.err.println(" size: " + size);
        System.err.println(" offset: " + offset);
    }

    /**
     * アイコンのデバイスのインスタンスをストリームから指定した個数作成します．
     */
    public static WindowsIconDevice[] readFrom(InputStream in, int number) throws IOException {

        WindowsIconDevice iconDevices[] = new WindowsIconDevice[number];

        LittleEndianDataInputStream iin = new LittleEndianDataInputStream(in);

        for (int i = 0; i < number; i++) {
            iconDevices[i] = new WindowsIconDevice();

            @SuppressWarnings("unused")
            int dummy;

            // read 16 bytes
            iconDevices[i].width = iin.read();
            iconDevices[i].height = iin.read();
            iconDevices[i].colors = iin.read();
            dummy = iin.read();
            iconDevices[i].hotspotX = iin.readShort();
            iconDevices[i].hotspotY = iin.readShort();
            iconDevices[i].size = iin.readInt();
            iconDevices[i].offset = iin.readInt();

            // 書き出しのときは 0 に戻す！
//          if (iconDevices[i].colors == 0) {
// Debug.println("set color 0 -> 256");
//              iconDevices[i].colors = 256;
//          }

// Debug.println("device [" + i + "]");
//          iconDevices[i].print();
        }

        return iconDevices;
    }
}

/* */
