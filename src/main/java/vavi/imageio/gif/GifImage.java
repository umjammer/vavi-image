/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * GifImage.
 * 
 * <pre>
 * 
 *  �@�\�_�o�[�W����  | GIF87  | GIF87a | GIF89a
 *  ------------------+--------+--------+-------
 *  �ʏ�摜          |   ��   |   ��   |  ��
 *  �C���^���[�X GIF  |   �~   |   ��   |  ��
 *  ���� GIF          |   �~   |   �~   |  ��
 *  GIF �A�j���[�V����|   �~   |   �~   |  ��
 *  
 * </pre>
 * <pre>
 * 
 *  GIF Header 
 *  Block
 *   :
 *  Trailer 0x3b 
 *  
 * </pre>
 * 
 * @author DJ.Uchi [H.Uchida]
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 2.00 xxxxxx original version <br>
 * @version 2.10 040914 nsano initial version <br>
 * @see "http://www.w3.org/Graphics/GIF/spec-gif89a.txt"
 */
public class GifImage {

    /** */
    private static NonLzwGifDecoder decoder = new NonLzwGifDecoder();

    /**
     * GIF �̃p���b�g�\���� RGB ���̕��т� 3 �o�C�g�\��
     */
    private static class GifRGB {
        /** �Ԑ��� */
        byte red;
        /** �ΐ��� */
        byte green;
        /** ���� */
        byte blue;
        /** */
        public static GifRGB readFrom(InputStream is) throws IOException {
            GifRGB rgb = new GifRGB();
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
            rgb.red = (byte) dis.read();
            rgb.green = (byte) dis.read();
            rgb.blue = (byte) dis.read();
            return rgb;
        }
    }

    /**
     * GIF �̉�ʋL�q���\���� GIF �S�̂̏����L�q�����w�b�_�[���B
     */
    private static class GifHeader {
        /** GIF Signature ("GIF") */
        byte[] signature = new byte[3];
        /** GIF Version ("87a" or "89a") */
        byte[] version = new byte[3];
        /**
         * ���� GIF �S�̂�\������̂ɕK�v�ȉ� pixel ��
         */
        @SuppressWarnings("unused")
        int logicalScreenWidth;
        /**
         * ���� GIF �S�̂�\������̂ɕK�v�ȏc pixel ��
         */
        @SuppressWarnings("unused")
        int logicalScreenHeight;
        /**
         * Packed Fields
         * <pre>
         * 7 654 3 210
         * ~ ~~~ ~ ~~~ 
         * | |   | +--- Size of Global Color Table
         * | |   +----- Sort Flag
         * | +--------- Color Resolution
         * +----------- Global Color Table Flag
         * </pre>
         */
        int packedFields;
        /** Background Color Index */
        @SuppressWarnings("unused")
        int backgroundColorIndex;
        /** Pixel Aspect Ratio */
        @SuppressWarnings("unused")
        int pixelAspectRatio;
        /** */
        static GifHeader readFrom(InputStream is) throws IOException {
            GifHeader gh = new GifHeader();
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
            dis.readFully(gh.signature);
            dis.readFully(gh.version);
            gh.logicalScreenWidth = dis.readUnsignedShort();
            gh.logicalScreenHeight = dis.readUnsignedShort();
            gh.packedFields = dis.readUnsignedByte();
            gh.backgroundColorIndex = dis.readUnsignedByte();
            gh.pixelAspectRatio = dis.readUnsignedByte();
            return gh;
        }
    }

    /**
     * GIF �̃C���[�W�L�q���\���� GIF �摜�ꖇ�ɂ��Ă̏����L�q�������́B
     */
    private static class ImageDescriptor {
        /** ��ʍ��ォ��̕\���ʒu (�� pixel ��) */
        @SuppressWarnings("unused")
        int left;
        /** ��ʍ��ォ��̕\���ʒu (�c pixel ��) */
        @SuppressWarnings("unused")
        int top;
        /** ���̉摜�̉� pixel �� */
        int width;
        /** ���̉摜�̏c pixel �� */
        int height;
        /**
         * Packed Fields
         * <pre>
         * 7 6 5 43 210
         * ~ ~ ~ ~~ ~~~ 
         * | | | |  +--- Size of Local Color Table
         * | | | +------ Reserved
         * | | +-------- Sort Flag
         * | +---------- Interlace Flag
         * +------------ Local Color Table Flag
         * </pre>
         */
        int packedFields;
        /** */
        static ImageDescriptor readFrom(InputStream is) throws IOException {
            ImageDescriptor id = new ImageDescriptor();
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
            // gih.split = (byte) dis.read();
            id.left = dis.readUnsignedShort();
            id.top = dis.readUnsignedShort();
            id.width = dis.readUnsignedShort();
            id.height = dis.readUnsignedShort();
            id.packedFields = dis.readUnsignedByte();
            return id;
        }
    }

    /** */
    private static class InternalImage {
        /** */
        ImageDescriptor imageDescriptor;
        /** */
        int sizeOfColorTable;
        /** */
        GifRGB[] localColorTable;
        /** */
        byte[] tableBasedImageData;
    }

    /** */
    private int index;

    /** */
    public int getIndex() {
        return index;
    }

    /** */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return width
     */
    public int getWidth() {
        return images.get(index).imageDescriptor.width;
//        return header.logicalScreenWidth; // TODO
    }

    /**
     * @return height
     */
    public int getHeight() {
        return images.get(index).imageDescriptor.height;
//        return header.logicalScreenHeight; // TODO
    }

    /** �p���b�g���쐬���܂��D */
    public ColorModel getColorModel() {

        InternalImage image = images.get(index);

        int bits = image.sizeOfColorTable + 1;
        int usedColor = (int) Math.pow(2, bits);

        byte reds[] = new byte[usedColor];
        byte greens[] = new byte[usedColor];
        byte blues[] = new byte[usedColor];

        for (int i = 0; i < usedColor; i++) {
            blues[i] = image.localColorTable[i].blue;
            greens[i] = image.localColorTable[i].green;
            reds[i] = image.localColorTable[i].red;
//Debug.println("palette(" + i + "): " + StringUtil.paramString(palette[i]));
        }

        return new IndexColorModel(bits, usedColor, reds, greens, blues);
    }

    /**
     * @return pixels
     */
    public byte[] getPixels() {
Debug.println("image[" + index + "]: " + images.get(index).tableBasedImageData.length);
        return images.get(index).tableBasedImageData;
    }

    /** */
    private GifHeader header;

    /** Global Color Table */
    private GifRGB[] globalColorTable;

    /** */
    private List<InternalImage> images = new ArrayList<InternalImage>();

    /** */
    static GifImage readFrom(InputStream is) throws IOException {
        GifImage gifImage = new GifImage();
        // �O���[�o����ʋL�q�����擾�B
        gifImage.header = GifHeader.readFrom(is);
//Debug.println(StringUtil.paramString(gifImage.header));
        // ���� GIF �̃J���[�r�b�g�� (8 �r�b�g�ȉ�)
        int sizeOfGlobalColorTable = gifImage.header.packedFields & 0x07;
Debug.println("sizeOfGlobalColorTable: " + sizeOfGlobalColorTable);

        // �O���[�o���J���[�}�b�v�̏���
        if ((gifImage.header.packedFields & 0x80) != 0) {
            gifImage.globalColorTable = new GifRGB[(int) Math.pow(2, sizeOfGlobalColorTable + 1)];
            if ((gifImage.header.packedFields & 0x80) != 0) {
                for (int i = 0; i < (2 << sizeOfGlobalColorTable); i++) {
                    gifImage.globalColorTable[i] = GifRGB.readFrom(is);
//Debug.println("gifRGB: " + StringUtil.paramString(gifImage.palette[i]));
                }
            }
        }

        while (true) {
            int blockType = is.read();
            if (blockType == 0x2c) { // Image Separator

Debug.println("2c: image block");
                InternalImage image = new InternalImage();

                // �ꖇ�ڂ̃C���[�W�L�q�����擾
                image.imageDescriptor = ImageDescriptor.readFrom(is);
//Debug.println("imageHeader: " + StringUtil.paramString(imageHeader));

                // �C���^���[�X�t���O
                boolean interlaced = (image.imageDescriptor.packedFields & 0x40) != 0;

                // ���[�J���J���[�}�b�v������ꍇ�̏���
                if ((image.imageDescriptor.packedFields & 0x80) != 0) {
                    image.sizeOfColorTable = image.imageDescriptor.packedFields & 0x07;
Debug.println("sizeOfLocalColorTable: " + image.sizeOfColorTable);
                    image.localColorTable = new GifRGB[(int) Math.pow(2, image.sizeOfColorTable + 1)];
                    for (int i = 0; i < (2 << image.sizeOfColorTable); i++) {
                        image.localColorTable[i] = GifRGB.readFrom(is);
                    }
                } else {
                    image.sizeOfColorTable = sizeOfGlobalColorTable;
                    image.localColorTable = gifImage.globalColorTable;
                }

                int bytesParLine = image.imageDescriptor.width;
                int dibColorDepth = -1;
                // �J���[�r�b�g����DIB�Ŏg�p�ł���l�ɋ����ϊ� (�I�[�o�[�T���v��)
                if (image.sizeOfColorTable == 0) {
                    bytesParLine = (((image.imageDescriptor.width + 7) >> 3) + 3) & ~3;
                    dibColorDepth = 1;
                } else if (image.sizeOfColorTable < 4) {
                    bytesParLine = (((image.imageDescriptor.width + 1) >> 1) + 3) & ~3;
                    dibColorDepth = 4;
                } else if (image.sizeOfColorTable < 8) {
                    bytesParLine = (image.imageDescriptor.width + 3) & ~3;
                    dibColorDepth = 8;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int lzwMinimumCodeSize = is.read();
Debug.println("2c: lzwMinimumCodeSize: " + lzwMinimumCodeSize);
                if (lzwMinimumCodeSize < 0) {
                    throw new EOFException();
                }
                baos.write(lzwMinimumCodeSize);
                while (true) {
                    int subBlockSize = is.read();
                    if (subBlockSize < 0) {
                        throw new EOFException();
                    } else if (subBlockSize == 0) {
Debug.println("2c: terminator");
                        break;
                    }
//Debug.println("2c: subBlockSize: " + subBlockSize);
                    baos.write(subBlockSize);
                    byte[] subBlockData = new byte[subBlockSize];
                    int l = 0;
                    while (l < subBlockSize) {
                        int r = is.read(subBlockData, l, subBlockSize - l);
                        if (r < 0) {
                            throw new EOFException();
                        }
                        l += r;
                    }
                    baos.write(subBlockData);
                }
                byte[] data = baos.toByteArray();
Debug.println("2c: data.length: " + data.length);

                // GIF �W�J
                image.tableBasedImageData = decoder.decode(data, image.imageDescriptor.width, image.imageDescriptor.height, bytesParLine, interlaced, dibColorDepth, data.length);

                gifImage.images.add(image);
Debug.println("2c: images: " + gifImage.images.size() + ", " + image.tableBasedImageData.length);
            } else if (blockType == 0x21) { // extention
                // GIF �g���u���b�N
                int extentionType = is.read();
Debug.println(String.format("21: extentionType: %02x", extentionType));
                switch (extentionType) {
                case 0xf9: { // 0xf9 Graphic Control Label
                    int blockSize = is.read(); // always 4
                    if (blockSize < 0) {
                        throw new EOFException();
                    }
Debug.println("21 f9: blockSize: " + blockSize);
                    byte[] data = new byte[4];
                    int l = 0;
                    while (l < 4) {
                        int r = is.read(data, l, 4 - l);
                        if (r < 0) {
                            throw new EOFException();
                        }
                        l += r;
                    }
Debug.println("21 f9: Graphic Control:\n" + StringUtil.getDump(data));
                    int blockTerminator = is.read(); // should be 0
Debug.println("21 f9: terminator: " + blockTerminator);
                }
                    break;
                case 0xfe: { // 0xfe Comment Label
                    while (true) {
                        int subBlockSize = is.read();
                        if (subBlockSize < 0) {
                            throw new EOFException();
                        } else if (subBlockSize == 0) {
Debug.println("21 fe: terminator");
                            break;
                        }
Debug.println("21 fe: subBlockSize: " + subBlockSize);
                        byte[] subBlockData = new byte[subBlockSize];
                        int l = 0;
                        while (l < subBlockSize) {
                            int r = is.read(subBlockData, l, subBlockSize - l);
                            if (r < 0) {
                                throw new EOFException();
                            }
                            l += r;
                        }
Debug.println("21 fe: subBlock:\n" + StringUtil.getDump(subBlockData));
                    }
                }
                    break;
                case 0x01: { // 0x01 Plain Text Label
                    int blockSize = is.read(); // always 12
Debug.println("21 01: size: " + blockSize);
                    byte[] data = new byte[12];
                    int l = 0;
                    while (l < 12) {
                        int r = is.read(data, l, 12 - l);
                        if (r < 0) {
                            throw new EOFException();
                        }
                        l += r;
                    }
Debug.println("21 01: Plain Text:\n" + StringUtil.getDump(data));
                    while (true) {
                        int subBlockSize = is.read();
Debug.println("21 01: subBlockSize: " + subBlockSize);
                        if (subBlockSize < 0) {
                            throw new EOFException();
                        } else if (subBlockSize == 0) {
Debug.println("21 01: terminator");
                            break;
                        }
                        byte[] subBlockData = new byte[subBlockSize];
                        l = 0;
                        while (l < subBlockSize) {
                            int r = is.read(subBlockData, l, subBlockSize - l);
                            if (r < 0) {
                                throw new EOFException();
                            }
                            l += r;
                        }
Debug.println("21 01: subBlock:\n" + StringUtil.getDump(subBlockData));
                    }
                }
                    break;
                case 0xff: { // 0xff Application Extension Label
                    int blockSize = is.read(); // always 11
Debug.println("21 ff: size: " + blockSize);
                    byte[] applicationIdentifier = new byte[8];
                    byte[] applicationAuthenticationCode = new byte[3];
                    int l = 0;
                    while (l < 8) {
                        int r = is.read(applicationIdentifier, l, 8 - l);
                        if (r < 0) {
                            throw new EOFException();
                        }
                        l += r;
                    }
Debug.println("21 ff: applicationIdentifier:\n" + StringUtil.getDump(applicationIdentifier));
                    l = 0;
                    while (l < 3) {
                        int r = is.read(applicationAuthenticationCode, l, 3 - l);
                        if (r < 0) {
                            throw new EOFException();
                        }
                        l += r;
                    }
Debug.println("21 ff: applicationAuthenticationCode:\n" + StringUtil.getDump(applicationAuthenticationCode));
                    while (true) {
                        int subBlockSize = is.read();
Debug.println("21 ff: subBlockSize: " + subBlockSize);
                        if (subBlockSize < 0) {
                            throw new EOFException();
                        } else if (subBlockSize == 0) {
Debug.println("21 ff: terminator");
                            break;
                        }
                        byte[] subBlockData = new byte[subBlockSize];
                        l = 0;
                        while (l < subBlockSize) {
                            int r = is.read(subBlockData, l, subBlockSize - l);
                            if (r < 0) {
                                throw new EOFException();
                            }
                            l += r;
                        }
Debug.println("21 ff: subBlock:\n" + StringUtil.getDump(subBlockData));
                    }
                }
                    break;
                default:
Debug.println(String.format("21 %02x unknown extention type", extentionType));
                    break;
                }
            } else if (blockType == 0x3b) {
Debug.println("3b: Trailer");
            } else if (blockType == -1) {
                break;
            } else {
Debug.println(String.format("%02x: unknown block type", blockType));
            }
        }

        return gifImage;
    }
}

/* */
