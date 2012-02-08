/*
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.io.IOException;
import java.io.InputStream;

import vavi.io.LittleEndianDataInputStream;


/**
 * Windows �� Icon �`���ł��D
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
 *  	   :
 *   
 *  
 * </code></pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970713 nsano initial version <br>
 *          1.00 010731 nsano move readFrom here <br>
 *          1.01 010817 nsano use Header class <br>
 *          1.10 010901 nsano refine <br>
 *          1.11 021104 nsano fix color related <br>
 */
public class WindowsIcon {

    /** �A�C�R���f�o�C�X */
    private WindowsIconDevice device;

    /** �A�C�R���̃r�b�g�}�b�v */
    private WindowsBitmap bitmap;

    /** �}�X�N */
    private byte mask[];

    /** �A�C�R�����쐬���܂��D */
    public WindowsIcon(WindowsIconDevice device, WindowsBitmap bitmap) {
        this.device = device;
        this.bitmap = bitmap;
        createMask();
    }

    /** �A�C�R���f�o�C�X���擾���܂��D */
    public WindowsIconDevice getDevice() {
        return device;
    }

    /** �A�C�R���̃r�b�g�}�b�v���擾���܂��D */
    public WindowsBitmap getBitmap() {
        return bitmap;
    }

    /** �A�C�R���̃}�X�N���擾���܂��D */
    public byte[] getMask() {
        return mask;
    }

    /**
     * �}�X�N���쐬���܂��D �}�X�N�̃f�[�^�̕��� 4 �̔{���ɂȂ�悤�Ƀp�f�B���O����Ă���D Y ���͋t�]���ē����Ă�D
     */
    private void createMask() {
        int off = bitmap.getImageSize() + bitmap.getOffset();
        int size = bitmap.getSize() - off;
// Debug.println("mask: " + size);
// Debug.println(bitmap.getWidth() + ", " + bitmap.getHeight());

        byte[] buf = new byte[size]; // �f�[�^�̂̃}�X�N�T�C�Y
        for (int i = 0; i < size; i++) {
            buf[i] = bitmap.getBitmap()[bitmap.getImageSize() + i];
        }

// Debug.dump(buf);

        // �p�f�B���O����菜�����}�X�N
        mask = new byte[(bitmap.getWidth() / 8) * bitmap.getHeight()];
// Debug.println("real: " + (bitmap.getWidth() / 8) * bitmap.getHeight());

        int m = (((bitmap.getWidth() / 8) + 3) / 4) * 4; // �p�f�B���O����̕�
// Debug.println("x: " + m);
        int i = 0;
        top: for (int y = bitmap.getHeight() - 1; y >= 0; y--) { // Y �����t�]
            for (int x = 0; x < m; x++) {
                if (x < bitmap.getWidth() / 8) {
// Debug.println(y * m + x);
                    if (i < size) {
                        mask[i++] = buf[y * m + x];
// System.err.print(Debug.toBits(mask[i-1]));
                    } else {
                        break top;
                    }
                }
            }
// System.err.println();
        }
    }

    /**
     * �A�C�R���t�@�C���̃w�b�_��\���N���X�ł��D
     * 
     * <pre>
     * 
     *  WORD  dummy
     *  WORD  type	1: icon
     *  WORD  count
     *  
     * </pre>
     */
    private static final class Header {
        /** �^�C�v */
        int type;

        /** �A�C�R���f�o�C�X�̐� */
        int number;

        /**
         * �X�g���[������t�@�C���w�b�_�̃C���X�^���X���쐬���܂��D
         */
        static final Header readFrom(InputStream in) throws IOException {

            Header h = new Header();

            LittleEndianDataInputStream iin = new LittleEndianDataInputStream(in);

            @SuppressWarnings("unused")
            int dummy;

            // 6 bytes
            dummy = iin.read();
            dummy = iin.read();
            h.type = iin.readShort();
            h.number = iin.readShort();

            return h;
        }

        /** for debug */
        @SuppressWarnings("unused")
        final void print() {
            System.err.println("type: " + type);
            System.err.println("has: " + number);
        }
    }

    /**
     * �w�肵���f�o�C�X�̃A�C�R�����X�g���[������ǂݍ��݂܂��D
     */
    private static WindowsIcon readIcon(InputStream in, WindowsIconDevice iconDevice) throws IOException {

        int offset = iconDevice.getOffset();
        int size = iconDevice.getSize();

        return new WindowsIcon(iconDevice, WindowsBitmap.readFrom(in, offset, size));
    }

    /**
     * �X�g���[������A�C�R���̃C���X�^���X���쐬���܂��D
     */
    public static WindowsIcon[] readFrom(InputStream in) throws IOException {

        WindowsIcon icons[];
        WindowsIconDevice iconDevices[];

        Header h = Header.readFrom(in);
// h.print();
        icons = new WindowsIcon[h.number];
        iconDevices = WindowsIconDevice.readFrom(in, h.number);

        for (int i = 0; i < h.number; i++) {
            icons[i] = readIcon(in, iconDevices[i]);
// if (iconDevices[i].getColors() == 0) {
// icons[i].bitmap.setUsedColor(256);
// icons[i].bitmap.setBits(8);
// }
        }

        return icons;
    }
}

/* */
