/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.ico;

import java.awt.Point;
import java.io.IOException;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;


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
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
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
    public String toString() {
        return " width: " + width +
                ", height: " + height +
                ", colors: " + colors +
                ", hotspot x: " + hotspotX +
                ", hotspot y: " + hotspotY +
                ", size: " + size +
                ", offset: " + offset;
    }

    /**
     * アイコンのデバイスのインスタンスをストリームから指定した個数作成します．
     */
    public static WindowsIconDevice[] readFrom(LittleEndianDataInputStream lin, int number) throws IOException {

        WindowsIconDevice[] iconDevices = new WindowsIconDevice[number];

        for (int i = 0; i < number; i++) {
            iconDevices[i] = new WindowsIconDevice();

            // read 16 bytes
            iconDevices[i].width = lin.read();
            iconDevices[i].height = lin.read();
            iconDevices[i].colors = lin.read();
            lin.readByte();
            iconDevices[i].hotspotX = lin.readShort();
            iconDevices[i].hotspotY = lin.readShort();
            iconDevices[i].size = lin.readInt();
            iconDevices[i].offset = lin.readInt();

            // 書き出しのときは 0 に戻す！
//          if (iconDevices[i].colors == 0) {
// Debug.println("set color 0 -> 256");
//              iconDevices[i].colors = 256;
//          }

// Debug.println("device [" + i + "]");
Debug.println(iconDevices[i]);
        }

        return iconDevices;
    }
}
