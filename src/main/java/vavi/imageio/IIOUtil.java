/*
 * Copyright (c) 2011 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;

import static java.lang.System.getLogger;


/**
 * IIOUtil.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2011/02/16 umjammer initial version <br>
 */
public class IIOUtil {

    private static final Logger logger = getLogger(IIOUtil.class.getName());

    private IIOUtil() {
    }

    /** */
    private static boolean ignoreErrors = true;

    /** */
    public static void setIgnoreErrors(boolean ignoreErrors) {
        IIOUtil.ignoreErrors = ignoreErrors;
    }

    /**
     * @param <T> service provider type
     * @param pt service provider class
     * @param p1 primary provider class name
     * @param p2 secondary provider class name
     */
    public static <T> void setOrder(Class<T> pt, String p1, String p2) {
        list(pt, p1, p2);
        int retry = 0;
        while (!verify(pt, p1, p2) && retry < 10) {
            setOrderInternal(pt, p1, p2);
            retry++;
        }
        list(pt, p1, p2);
    }

    /** */
    static <T> void list(Class<T> pt, String p1, String p2) {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        Iterator<T> i = iioRegistry.getServiceProviders(pt, true);
        System.out.println("---------");
        while (i.hasNext()) {
            T p = i.next();
            if (p1.equals(p.getClass().getName())) {
                System.out.println(p.getClass().getName() + " (I)");
            } else if (p2.equals(p.getClass().getName())) {
                System.out.println(p.getClass().getName() + " (II)");
//            } else {
//                System.out.println(p.getClass().getName());
            }
        }
    }

    private static <T> boolean verify(Class<T> pt, String p1, String p2) {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        Iterator<T> i = iioRegistry.getServiceProviders(pt, true);
        int pos1 = 0, pos2 = 0, pos = 0;
        while (i.hasNext()) {
            T p = i.next();
            if (p1.equals(p.getClass().getName())) {
                pos1 = pos;
            } else if (p2.equals(p.getClass().getName())) {
                pos2 = pos;
            }
            pos++;
        }
        return pos1 < pos2;
    }

    private static <T> void setOrderInternal(Class<T> pt, String p1, String p2) {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        T sp1 = null;
        T sp2 = null;
        Iterator<T> i = iioRegistry.getServiceProviders(pt, true);
        while (i.hasNext()) {
            T p = i.next();
            if (p1.equals(p.getClass().getName())) {
                sp1 = p;
            } else if (p2.equals(p.getClass().getName())) {
                sp2 = p;
            }
        }
        if (sp1 == null || sp2 == null) {
            if (!ignoreErrors) {
                throw new IllegalArgumentException(p1 + " or " + p2 + " not found");
            } else {
                logger.log(Level.DEBUG, p1 + " or " + p2 + " not found");
            }
        }
        iioRegistry.setOrdering(pt, sp1, sp2);
    }

    /**
     * @param <T> service provider type
     * @param pt service provider class
     * @param p0 provider class name
     */
    public static <T> void deregister(Class<T> pt, String p0) {
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        T sp = null;
        Iterator<T> i = iioRegistry.getServiceProviders(pt, true);
        while (i.hasNext()) {
            T p = i.next();
            if (p0.equals(p.getClass().getName())) {
                sp = p;
            }
        }
        if (sp == null) {
            if (!ignoreErrors) {
                throw new IllegalArgumentException(p0 + " not found");
            } else {
                logger.log(Level.DEBUG, p0 + " not found");
            }
        }
        iioRegistry.deregisterServiceProvider(sp, pt);
    }

    /**
     * @param type interface
     * @param className implementation
     */
    public static ImageWriter getImageWriter(String type, String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("no such ImageWriter: " + className);
        }
        Iterator<ImageWriter> iws = ImageIO.getImageWritersByFormatName(type);
        while (iws.hasNext()) {
            ImageWriter iw = iws.next();
            if (clazz.isInstance(iw)) {
logger.log(Level.TRACE, "ImageWriter: " + iw.getClass());
                return iw;
            }
        }

        throw new IllegalStateException("no suitable ImageWriter: " + type);
    }

    /**
     * @param type interface
     * @param className implementation
     */
    public static ImageReader getImageReader(String type, String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("no such ImageReader: " + className);
        }
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName(type);
        while (irs.hasNext()) {
            ImageReader ir = irs.next();
            if (clazz.isInstance(ir)) {
logger.log(Level.TRACE, "ImageReader: " + ir.getClass().getName());
                return ir;
            }
        }

        throw new IllegalStateException("no suitable ImageReader: " + type);
    }
}
