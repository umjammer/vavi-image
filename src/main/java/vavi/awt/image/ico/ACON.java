/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.ico;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;
import vavi.util.win32.Chunk;
import vavi.util.win32.RIFF;


/**
 * Animated Cursor File Format.
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 020507 nsano initial version <br>
 *          0.10 021124 nsano complete <br>
 *          0.20 030605 nsano new version compliant <br>
 *          0.21 030711 nsano fix outer class instance related <br>
 */
public class ACON extends RIFF {

    /**
     */
    public ACON() {
    }

    /** Gets extention. */
    public static String getExtention() {
        return "ani";
    }

    /** */
    private anih header;

    /** */
//  private String name;

    /** */
//  private String copyright;

    /** temporary counter for icons */
    private int count;

    /** */
    private WindowsIcon[] icons;

    /** */
    private int[] steps;

    /** */
    private int[] sequences;

    // ----

    public class LIST extends Chunk {
        /** */
        public class INAM extends Chunk {
        }

        /** */
        public class IART extends Chunk {
        }

        /** */
        public class icon extends Chunk {
            public void setData(InputStream is) throws IOException {
                icons[count++] = WindowsIcon.readFrom(is)[0];
            }

            /** for debug */
            protected void printData() {
                System.err.println(icons[count - 1]);
            }
        }
    }

    /** */
    public class anih extends Chunk {

        /** */
        int size;
        /** */
        int frames;
        /** */
        int steps;
        /** */
        int x;
        /** */
        int y;
        /** */
        int bits;
        /** */
        int plains;
        /** */
        int jifrate;
        /** */
        int flags;

        /** */
        public void setData(InputStream is) throws IOException {

            LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(is);

            size = ledis.readInt();
            frames = ledis.readInt();
            steps = ledis.readInt();
            x = ledis.readInt();
            y = ledis.readInt();
            bits = ledis.readInt();
            plains = ledis.readInt();
            jifrate = ledis.readInt();
            flags = ledis.readInt();
Debug.println(Level.FINE, this);
            icons = new WindowsIcon[frames];
            count = 0;

            header = this;
        }

        @Override public String toString() {
            return "size: " + size
                    + "frames: " + frames
                    + "steps: " + steps
                    + "x: " + x
                    + "y: " + y
                    + "bits: " + bits
                    + "plains: " + plains
                    + "jifrate: " + jifrate
                    + "flags: " + flags;
        }
    }

    /** */
    public class rate extends Chunk {
        /** */
        public void setData(InputStream is) throws IOException {
            LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(is);
            steps = new int[header.steps];
            for (int i = 0; i < steps.length; i++) {
                steps[i] = ledis.readInt();
            }
        }
    }

    /** */
    public class seq extends Chunk {
        /** */
        public void setData(InputStream is) throws IOException {
            LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(is);
            sequences = new int[header.steps];
            for (int i = 0; i < sequences.length; i++) {
                sequences[i] = ledis.readInt();
            }
        }
    }
}
