/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import vavi.util.Debug;


/**
 * �� LZW ���_ GIF �f�R�[�_�B
 * 
 * @author DJ.Uchi [H.Uchida]
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.20 xxxxxx huchida original version <br>
 * @version 2.00 040913 nsano port to java <br>
 */
public class NonLzwGifDecoder {
    /**
     * DIB �f�[�^�������ݗp�\����
     * DIB �ɉ摜�f�[�^���������ލۂɕK�v�ȏ����܂Ƃ߂����B
     */
    class RgbContext {
        /** �W�J�f�[�^�������݈ʒu(���ꃉ�C����ł̃I�t�Z�b�g�l) */
        int xPoint;
        /** �W�J�f�[�^�������݃I�t�Z�b�g(���C���� * �A���C�������g) */
        int offset;
        /** �� pixel �� */
        int width;
        /** �c pixel �� */
        int height;
        /** �A���C�������g�l(�P���C�����̃o�C�g��) */
        int bytesPerLine;
        /** �J���[�r�b�g�l */
        int colorDepth;
        /** �W�J�f�[�^�������݃��C��(�C���^���[�X���Ɏg�p) */
        int currentLine;
        /** �C���^���[�X�I�t�Z�b�g(���C����) */
        int interlaceOffset;
        /** �C���^���[�X�t���O true �ŃC���^�[���[�X */
        boolean interlaced;
    }

    /**
     * GIF �f�[�^��͗p�\����
     * GIF �̕������R�[�h�擾�ɕK�v�ȏ����܂Ƃ߂����B
     */
    class GifContext {
        /** RgbDecodeStatus �\���̎Q�Ɨp�|�C���^ */
        RgbContext rgb;
        /** �R�[�h�T�C�Y(CS) */
        int codeSize;
        /** �r�b�g�T�C�Y(CBL) */
        int bitSize;
        /** �N���A�R�[�h */
        int clearCode;
        /** �G���h�R�[�h */
        int endCode;
        /** ���G���g���� */
        int entry;
        /** ���r�b�g�ʒu */
        int bitPoint;
        /** ���u���b�N�J�n�ʒu */
        int nextBlock;
        /** �f�[�^�T�C�Y */
        int dataSize;
    }

    /**
     * GIF �摜(�ŏ��̈ꖇ�̂�)��W�J����B
     * ���͂� GIF �f�[�^�A�o�͂� RGB�B
     * �����ł͎�Ƀf�R�[�h�̑O�������s���܂��B
     * @return new allocated buffer (height * bytesPerLine)
     */
    public byte[] decode(byte[] data,
                         int width,
                         int height,
                         int bytesPerLine,
                         boolean interlaced,
                         int colorDepth,
                         int size) {

        byte[] vram = new byte[height * bytesPerLine];
        int[] lzw = new int[8192]; // ���k�f�[�^�i�[�p�z��(�����e�[�u���ł͂Ȃ��I)
        int code = 0; // �������R�[�h������

        // DIB �f�[�^�������ݗp�\���̏�����
        RgbContext rgb = new RgbContext();
        rgb.xPoint = 0; // �W�J�f�[�^�������݈ʒu������
        rgb.offset = (height - 1) * bytesPerLine; // �������݈ʒu�I�t�Z�b�g
        rgb.width = width; // �� pixel ���擾
        rgb.height = height; // �c pixel ���擾
        rgb.bytesPerLine = bytesPerLine; // �A���C�������g�l�擾
        rgb.colorDepth = colorDepth; // �J���[�r�b�g�l�擾
        rgb.currentLine = 0; // �W�J�f�[�^�������݃��C��������
        rgb.interlaceOffset = 8; // �C���^���[�X�I�t�Z�b�g������
        rgb.interlaced = interlaced; // �C���^���[�X�t���O�擾
//Debug.println(StringUtil.paramString(rgb));

        // GIF �f�[�^��͗p�\���̏�����
        GifContext gif = new GifContext();
        gif.rgb = rgb; // RgbDecodeStatus �\���̎Q�Ɨp�|�C���^�擾
        gif.codeSize = data[0] & 0xff; // �R�[�h�T�C�Y�擾
        gif.bitSize = gif.codeSize + 1; // CBL ������ (�R�[�h�T�C�Y + 1)
        gif.clearCode = 1 << gif.codeSize; // �N���A�R�[�h�ݒ� (2 ^ �R�[�h�T�C�Y)
        gif.endCode = gif.clearCode + 1; // �G���h�R�[�h�ݒ� (2 ^ �R�[�h�T�C�Y + 1)
        gif.entry = gif.endCode + 1; // �G���g���������� (2 ^ �R�[�h�T�C�Y + 2)
        gif.bitPoint = 8; // �r�b�g�|�C���^������ (8 bit = 1 byte)
        gif.nextBlock = 1; // ���u���b�N�ʒu������ (�擪�u���b�N)
        gif.dataSize = size; // GIF �f�[�^�T�C�Y�擾
//Debug.println(StringUtil.paramString(gif));

        int times = 0; // �W�J�񐔏�����
        int offset = 0; // �W�J�J�n�ʒu������

        // �G���h�R�[�h�������܂łЂ����烋�[�v
        while ((code = getCode(data, gif)) != gif.endCode) {
            // �N���A�R�[�h�����ꂽ�ꍇ
//System.err.println("code: " + code);
            if (code == gif.clearCode) {
                // �G���g���� & CBL ������
                gif.entry = gif.endCode + 1;
                gif.bitSize = gif.codeSize + 1;

                // �W�J�֐����R�[���B
                decodeLzw(vram, lzw, gif, times, offset);
                times = 0;
                offset = 0;
            } else {
                // int �T�C�Y�z��Ɏ擾�����������R�[�h�𗭂ߍ��ށB
                // ����͓W�J��Ƃ����~���ɍs���ׂł���A�����e�[�u���Ƃ͈قȂ�܂��B
                // �z��ɗ��ߍ��܂��Ɏ擾�����������R�[�h�𒼂ɓW�J���邱�Ƃ��o���܂����A
                // ���x�I�ɖ�肪����ׁA���̕��@���̗p���Ă��܂��B
                lzw[times++] = code & 0xffff;

                // ���܂ł��N���A�R�[�h������Ȃ��Ɣz�񂪈��邽�߁A
                // �������R�[�h�� 8192 ���܂������_�œW�J��Ƃ��s���B
                // �񈳏k GIF �΍􂩁H
                if (times == 8192) {
                    decodeLzw(vram, lzw, gif, times, offset);
                    times = 4096;
                    offset = 4096;
                }
                if (gif.entry < 4095) {
                    gif.entry++;
                }
            }
        }

        // �z��Ɏc���Ă��镄�����R�[�h��W�J����B
        decodeLzw(vram, lzw, gif, times, offset);

        return vram;
    }

    /**
     * �σr�b�g�����͊֐��B
     * �������R�[�h������o���āA�r�b�g�ʒu���C���N�������g����B
     */
    private int getCode(byte[] data, GifContext gif) {
        int code = 0; // �������R�[�h������
        int bytePoint = gif.bitPoint >> 3; // �ǂݍ��݈ʒu(�o�C�g�P��)�擾
//System.err.println("pt: " + bytePoint);

        // �T�C�Y�I�[�o�[�t���[�̏ꍇ�A�����I�ɃG���h�R�[�h��Ԃ�(�j���t�@�C���΍�)
        if ((bytePoint + 2) > gif.dataSize) {
Debug.println("maybe broken");
            return gif.endCode;
        }

        // �������R�[�h�擾
        int i = 0; // �ǂݍ��݃o�C�g��������
        while ((((gif.bitPoint + gif.bitSize) - 1) >> 3) >= bytePoint) {
            // �ǂݍ��ݒ��Ƀu���b�N���I�������ꍇ
            if (bytePoint == gif.nextBlock) {
                gif.nextBlock += ((data[bytePoint++] & 0xff) + 1); // ���u���b�N�ʒu�X�V
                gif.bitPoint += 8; // �r�b�g�|�C���^���P�o�C�g�����Z
            }
            code += ((data[bytePoint++] & 0xff) << i); // �R�[�h�擾
            i += 8;
        }

        // ����ꂽ�R�[�h�̗]���ȃr�b�g��؂�Ƃ΂��B(�}�X�L���O����)
        code = (code >> (gif.bitPoint & 0x07)) & ((1 << gif.bitSize) - 1);

        // �r�b�g�|�C���^�X�V
        gif.bitPoint += gif.bitSize;

        // CBL ���C���N�������g����K�v�����邩�ǂ����m�F����B
        if (gif.entry > ((1 << gif.bitSize) - 1)) {
            gif.bitSize++;
        }

        return code;
    }

    /**
     * �� LZW ���_�W�J�֐� (���C�����[�v)
     * int �T�C�Y�̔z��Ɋi�[���ꂽ�������R�[�h���f�R�[�h���܂��B
     */
    private void decodeLzw(byte[] vram, int[] lzw, GifContext gif, int times, int offset) {
        // �P�Ƀ��[�v���񂵂ēW�J�֐����Ă�ł邾���B
//Debug.println(" times: " + times + ", offset: " + offset);
        for (int i = offset; i < times; i++) {
            getLzwBytes(vram, lzw, gif, i);
        }
    }

    /**
     * �� LZW ���_�W�J�֐� (�R�A)
     * �� LZW ���_�̊j�B
     * �w�肳�ꂽ�������R�[�h�ɑ΂���W�J�f�[�^��Ԃ��܂��B
     */
    private void getLzwBytes(byte[] vram, int[] lzw, GifContext gif, int offset) {
        // �z�񂩂畄�����R�[�h������o���܂��B
        int code = lzw[offset];

        if (code < gif.clearCode) {
            // �������R�[�h�� "�F��" ��菬�����ꍇ
            // �������R�[�h�����̂܂� RGB �ɏ������ށB
            writeRgb(vram, gif, code);

        } else if (code > gif.endCode + offset--) {
            // �������R�[�h�����m�̂��̂ł���ꍇ

            // ��O�̓W�J�f�[�^
            getLzwBytes(vram, lzw, gif, offset);

            // ��O�̓W�J�f�[�^�̐擪���
            getLzwByte(vram, lzw, gif, offset);

        } else {
            // �������R�[�h�� "�F�� + 1" ���傫���ꍇ
            // ���Ȃ݂ɁA���̊֐��ɓ����Ă���R�[�h�ɃG���h�R�[�h��N���A�R�[�h��
            // ��΂Ɍ���܂���̂ŁA���̏ꍇ�̏����͍l������Ă��܂���B

            // (�������R�[�h - �F�� + 1) �̓W�J�f�[�^
            getLzwBytes(vram, lzw, gif, code - gif.endCode - 1);

            // (�������R�[�h - �F�� + 2) �̓W�J�f�[�^�̐擪���
            getLzwByte(vram, lzw, gif, code - gif.endCode);
        }
    }

    /**
     * �� LZW ���_�W�J�֐� (�T�u)
     * �� LZW ���_�̊j�B
     * �w�肳�ꂽ�������R�[�h�ɑ΂���W�J�f�[�^�̐擪�P��Ԃ��܂��B
     */
    private void getLzwByte(byte[] vram, int[] lzw, GifContext gif, int offset) {
        int code;

        // �z�񂩂畄�����R�[�h������o���A"�F��" ��菬�����ǂ����m�F�B
        while ((code = lzw[offset]) >= gif.clearCode) {
            // �������R�[�h�����m�̂��̂ł���ꍇ
            if (code > gif.endCode + offset) {
                // ��O�̓W�J�f�[�^�̐擪���
                offset--;

                // �������R�[�h�� "�F�� + 1" ���傫���ꍇ
            } else {
                // (�������R�[�h - �F�� + 1) �̓W�J�f�[�^�̐擪���
                offset = code - gif.endCode - 1;
            }
        }

        // ����ꂽ�W�J�f�[�^��DIB�ɏ������ށB
        writeRgb(vram, gif, code);
    }

    /**
     * RGB �摜�f�[�^�������݊֐��B
     * �W�J���ꂽ�摜�f�[�^�� RGB �Ƃ��ď������݂܂��B
     */
    private void writeRgb(byte[] rgb, GifContext gif, int code) {
        // RGB �ɉ摜�f�[�^���������݂܂��B
        // ���m�N���� 16 �F�̏ꍇ�̓r�b�g�P�ʂł̏������݂ɂȂ�ׁA�}�X�L���O�������s���܂��B
        int i;

        // RGB �ɉ摜�f�[�^���������݂܂��B
        // ���m�N���� 16 �F�̏ꍇ�̓r�b�g�P�ʂł̏������݂ɂȂ�ׁA�}�X�L���O�������s���܂��B
        int j;
//Debug.println("gif.rgb.color: " + gif.rgb.colors);
//Debug.println("rgb.offset: " + gif.rgb.offset + ", rgb.rgb.point: " + gif.rgb.xPoint + ", code: " + code);
//System.out.printf("%d\n", code);
    	switch (gif.rgb.colorDepth) {
    	case 1: // ���m�N���摜�̏ꍇ
    	    i = gif.rgb.offset + (gif.rgb.xPoint / 8);
    	    j = 7 - (gif.rgb.xPoint & 0x07);
    	    rgb[i] = (byte) ((rgb[i] & ~(1 << j)) | (code << j));
//Debug.println("x: " + gif.rgb.xPoint + ", y: " + (gif.rgb.offset / gif.rgb.bytesPerLine) + " / w: " + gif.rgb.width + ", h: " + gif.rgb.height + ": " + StringUtil.toHex2(rgb[i]));
    	    break;
    	case 4: // 16�F�摜�̏ꍇ
    	    i = gif.rgb.offset + (gif.rgb.xPoint >> 1);
    	    j = (gif.rgb.xPoint & 0x01) << 2;
    	    rgb[i] = (byte) ((rgb[i] & (0x0f << j)) | (code << (4 - j)));
    	    break;
    	default: // 256�F�̏ꍇ
        	rgb[gif.rgb.offset + gif.rgb.xPoint] = (byte) code;
            break;
    	}

        // �������݈ʒu���C���N�������g
        gif.rgb.xPoint++;

        // �������݈ʒu�����C���̏I�[�ɒB�����ꍇ
        if (gif.rgb.xPoint == gif.rgb.width) {
//Debug.println("y: " + (gif.rgb.offset / gif.rgb.bytesPerLine));
            if (gif.rgb.interlaced) { // �C���^���[�X GIF �̏ꍇ

                // �C���^���[�X���C������ʉ��[�ɒB�����ꍇ
                if ((gif.rgb.currentLine + gif.rgb.interlaceOffset) >= gif.rgb.height) {
                    if ((gif.rgb.currentLine & 0x07) == 0) {
                        gif.rgb.offset = (gif.rgb.height - 5) * gif.rgb.bytesPerLine;
                        gif.rgb.currentLine = 4;
                    } else if (gif.rgb.interlaceOffset == 8) {
                        gif.rgb.offset = (gif.rgb.height - 3) * gif.rgb.bytesPerLine;
                        gif.rgb.currentLine = 2;
                        gif.rgb.interlaceOffset = 4;
                    } else if (gif.rgb.interlaceOffset == 4) {
                        gif.rgb.offset = (gif.rgb.height - 2) * gif.rgb.bytesPerLine;
                        gif.rgb.currentLine = 1;
                        gif.rgb.interlaceOffset = 2;
                    }
                } else {
                    gif.rgb.offset -= gif.rgb.bytesPerLine * gif.rgb.interlaceOffset;
                    gif.rgb.currentLine += gif.rgb.interlaceOffset;
                }
            } else { // ���j�A GIF �̏ꍇ
                gif.rgb.offset -= gif.rgb.bytesPerLine;
            }
            gif.rgb.xPoint = 0;
        }
    }
}

/* */
