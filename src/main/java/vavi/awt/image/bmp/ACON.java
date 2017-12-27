/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.bmp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import vavi.io.LittleEndianDataInputStream;
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

    /** for debug */
    protected void printData() {
        System.err.println("---- data ----");
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

    // -------------------------------------------------------------------------

    public class LIST extends vavi.util.win32.LIST {
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
        @SuppressWarnings("hiding")
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

            @SuppressWarnings("resource")
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
            printData();
            icons = new WindowsIcon[frames];
            count = 0;

            header = this;
        }

        protected void printData() {
            System.err.println("size: " + size);
            System.err.println("frames: " + frames);
            System.err.println("steps: " + steps);
            System.err.println("x: " + x);
            System.err.println("y: " + y);
            System.err.println("bits: " + bits);
            System.err.println("plains: " + plains);
            System.err.println("jifrate: " + jifrate);
            System.err.println("flags: " + flags);
        }
    }

    /** */
    public class rate extends Chunk {
        /** */
        public void setData(InputStream is) throws IOException {
            @SuppressWarnings("resource")
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
            @SuppressWarnings("resource")
            LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(is);
            sequences = new int[header.steps];
            for (int i = 0; i < sequences.length; i++) {
                sequences[i] = ledis.readInt();
            }
        }
    }

    // -------------------------------------------------------------------------

    /** */
    public static void main(String[] args) throws Exception {
        InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
        ACON ani = (ACON) ACON.readFrom(is);
System.err.println("ACON: " + ani);
    }
}

/* */
