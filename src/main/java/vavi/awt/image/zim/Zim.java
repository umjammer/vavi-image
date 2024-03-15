// recoil.ci - RECOIL core
//
// Copyright (C) 2009-2022  Piotr Fusik
//
// This file is part of RECOIL (Retro Computer Image Library),
// see http://recoil.sourceforge.net
//
// RECOIL is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published
// by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.
//
// RECOIL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with RECOIL; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

package vavi.awt.image.zim;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import vavi.io.SeekableDataInput;
import vavi.util.ByteUtil;
import vavi.util.Debug;


/**
 * Zim.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-26 nsano initial version <br>
 */
public class Zim {

    private Zim() {}

    /** Maximum number of pixels in a decoded image. */
	static final int MaxPixelsLength = 1701 * 1678;

    /** ZX Spectrum formats. */
    static int getZxColor(int c) {
        return 0xff000000 | (c >> 1 & 1) * 0xff0000 | (c >> 2 & 1) * 0x00ff00 | (c & 1) * 0x0000ff;
    }

    /**
     * @param content little endian
     */
    public static BufferedImage decode(SeekableDataInput<?> content) throws IOException {
        byte[] b0 = new byte[8];
        content.readFully(b0);
        if (!Arrays.equals(b0, "FORMAT-A".getBytes()))
            throw new IllegalArgumentException("not zim image");
        content.position(0x1fa);
        int v_5_6 = content.readUnsignedShort();
        int v_3_4 = content.readUnsignedShort();
        int v_1_2 = content.readUnsignedShort();
        int contentOffset = 0x200 + (v_5_6 << 1);
        content.position(contentOffset);
Debug.printf(Level.FINE, "pos: %1$d, %1$08x", content.position());
        byte[] b1 = new byte[22];
        content.readFully(b1);
        if (b1[0] != 0 || b1[1] != 0
                || b1[2] != 0 || b1[3] != 0
                || b1[0x14] != 1 || b1[0x15] != 0) // TODO: uncompressed
            throw new IllegalArgumentException("wrong zim");
        int width = ByteUtil.readLeShort(b1, 4) + 1;
        int height = ByteUtil.readLeShort(b1, 6) + 1;
Debug.println(Level.FINE, "size: " + width + "x" + height);
        if (width * height > MaxPixelsLength)
            throw new IllegalArgumentException("too large " + width + "x" + height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        contentOffset += 24;
        int v_2 = content.readUnsignedByte(); // 23
        int v_1 = content.readUnsignedByte(); // 24
Debug.printf(Level.FINE, "pos: %1$d, %1$08x", content.position());
        // RGB palette decoded from the image file.
        int[] contentPalette = new int[256];
        if (v_2 != 0 || v_1 != 0) {
Debug.println(Level.FINE, "user palette");
            for (int c = 0; c < 16; c++) {
                int v0 = content.readUnsignedByte();
                int v1 = content.readUnsignedByte();
                int v2 = content.readUnsignedByte();
                int v_ = content.readUnsignedByte();
                contentPalette[c] = 0xff00_0000 | v1 << 16 | v2 << 8 | v0;
                contentOffset += 4;
            }
        } else {
Debug.println(Level.FINE, "default palette");
            for (int c = 0; c < 16; c++)
                contentPalette[c] = getZxColor(c);
            contentPalette[8] = 0xffff_ffff;
        }
Debug.printf(Level.FINE, "pos: %1$d, %1$08x", content.position());
        int pixelsLength = width * height;
        for (int pixelsOffset = 0; pixelsOffset < pixelsLength; pixelsOffset++)
            pixels[pixelsOffset] = contentPalette[0];
        byte[] flags3 = new byte[64];
        byte[] data = new byte[512];
Debug.printf(Level.FINE, "pos: %1$d, %1$08x", content.position());
        ZimStream stream = new ZimStream(content);
        int skip = stream.readUnsignedShort();
Debug.println(Level.FINE, "skip: " + (skip << 1));
        stream.skipBytes(skip << 1);
Debug.printf(Level.FINE, "pos: %1$d, %1$08x", content.position());
        while (true) {
            int dot = stream.readUnsignedShort();
//Debug.println(Level.FINER, "dot: " + dot + ", " + content.position());
            switch (dot) {
            case -1:
                throw new IllegalArgumentException("wrong zim");
            case 0:
                return image;
            default:
                break;
            }
            int x = stream.readUnsignedShort();
//Debug.println(Level.FINER, "x: " + x);
            if (x < 0 || x >= width)
                throw new IllegalArgumentException("wrong zim: " + x);
            int y = stream.readUnsignedShort();
//Debug.println(Level.FINER, "y: " + y);
            if (y < 0 || y >= height)
                throw new IllegalArgumentException("wrong zim: " + y);
            int len = stream.readUnsignedShort();
//Debug.println(Level.FINER, "len: " + len);
            if (len < 0)
                throw new IllegalArgumentException("wrong zim: " + len);
            int size = stream.readUnsignedShort();
//Debug.println(Level.FINER, "size: " + size);
            if (size > 512 || (size & 3) != 0 || size << 1 < dot)
                throw new IllegalArgumentException("wrong zim: " + size);
            int pixelsOffset = y * width + x;
            if (pixelsOffset + dot > pixelsLength)
                throw new IllegalArgumentException("wrong zim: " + dot);
            stream.unpackFlags2();
            stream.unpack(stream.flags2, flags3, 64);
            stream.unpack(flags3, data, size);
            for (int i = 1; i < size; i++)
                data[i] ^= data[i - 1] & 0xff;
            for (int i = 2; i < size; i++)
                data[i] ^= data[i - 2] & 0xff;
            size >>= 2;
            for (int i = 0; i < dot; i++) {
                int bit = ~i & 7;
                int c = ((data[i >> 3] & 0xff) >> bit & 1) << 3
                        | ((data[size + (i >> 3)] & 0xff) >> bit & 1) << 2
                        | ((data[2 * size + (i >> 3)] & 0xff) >> bit & 1) << 1
                        | ((data[3 * size + (i >> 3)] & 0xff) >> bit & 1);
                pixels[pixelsOffset + i] = contentPalette[c];
            }
        }
    }
}
