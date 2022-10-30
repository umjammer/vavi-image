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

import java.io.IOException;

import vavi.io.SeekableDataInput;


/**
 * ZimStream.
 *
 * little endian
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-26 nsano initial version <br>
 */
public class ZimStream {

    byte[] flags1 = new byte[1];
    byte[] flags2 = new byte[8];

    private SeekableDataInput<?> sdi;

    public ZimStream(SeekableDataInput<?> sdi) {
        this.sdi = sdi;
    }

    private int readUnpacked(byte[] flags, int unpackedOffset) throws IOException {
        return ((flags[unpackedOffset >> 3] & 0xff) >> (~unpackedOffset & 7) & 1) != 0 ? readByte() : 0;
    }

    boolean unpack(byte[] flags, byte[] unpacked, int unpackedLength) throws IOException {
        boolean enough = true;
        for (int unpackedOffset = 0; unpackedOffset < unpackedLength; unpackedOffset++) {
            int b = readUnpacked(flags, unpackedOffset);
            if (b < 0) {
                enough = false;
                b = 0;
            }
            unpacked[unpackedOffset] = (byte) b;
        }
        return enough;
    }

    boolean unpackFlags2() throws IOException {
        int b = readByte();
        if (b < 0)
            return false;
        flags1[0] = (byte) b;
        return unpack(flags1, flags2, 8);
    }

    public int readByte() throws IOException {
        return sdi.readByte() & 0xff;
    }

    public int readUnsignedShort() throws IOException {
        return sdi.readUnsignedShort();
    }

    public int skipBytes(int n) throws IOException {
        return sdi.skipBytes(n);
    }
}

