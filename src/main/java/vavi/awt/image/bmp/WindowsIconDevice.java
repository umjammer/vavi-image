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
 * �A�C�R���̑傫�����̏���\���N���X�ł��D
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
 *  DWORD offset - �w�b�_(6) + �f�o�C�X(16) x �� + size x #
 *  
 * </pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 */
public class WindowsIconDevice {

    /** �� */
    private int width;

    /** ���� */
    private int height;

    /** �F�� */
    private int colors;

    /** X �z�b�g�X�|�b�g */
    private int hotspotX;

    /** Y �z�b�g�X�|�b�g */
    private int hotspotY;

    /** �T�C�Y */
    private int size;

    /** �I�t�Z�b�g */
    private int offset;

    /** #readFrom �p */
    private WindowsIconDevice() {
    }

    /** �A�C�R�����쐬���܂��D */
    public WindowsIconDevice(int width, int height, int colors) {
        this.width = width;
        this.height = height;
        this.colors = colors;
    }

    /** �����擾���܂��D */
    public int getWidth() {
        return width;
    }

    /** �������擾���܂��D */
    public int getHeight() {
        return height;
    }

    /** �F�����擾���܂��D */
    public int getColors() {
        return colors;
    }

    /** �F����ݒ肵�܂��D */
    public void setColors(int colors) {
        this.colors = colors;
    }

    /** �T�C�Y���擾���܂��D */
    public int getSize() {
        return size;
    }

    /** �I�t�Z�b�g���擾���܂��D */
    public int getOffset() {
        return offset;
    }

    /** �z�b�g�X�|�b�g���擾���܂��D */
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
     * �A�C�R���̃f�o�C�X�̃C���X�^���X���X�g���[������w�肵�����쐬���܂��D
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

            // �����o���̂Ƃ��� 0 �ɖ߂��I
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
