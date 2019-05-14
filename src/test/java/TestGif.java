/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import vavi.imageio.gif.NonLzwGifImageReader;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;


/**
 * TestGif.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/14 umjammer initial version <br>
 */
public class TestGif {

    @Test
    public void test() throws Exception {
        main(new String[] { "src/test/resources" });
    }

    //----

    static class LoopCounter {
        int count = 0;
        final int max;
        LoopCounter(int max) {
            this.max = max;
        }
        void increment() {
            this.count = count < max - 1 ? count + 1 : 0;
//System.err.println(count);
        }
        int get() {
            return count;
        }
    }

    static class RegexFileVisitor extends SimpleFileVisitor<Path> {
        Pattern pattern;
        Function<Path, Boolean> function;
        int max;
        int count;
        RegexFileVisitor(String pattern, int max, Function<Path, Boolean> function) {
            this.pattern = Pattern.compile(pattern);
            this.function = function;
            this.max = max;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            if (attr.isRegularFile()) {
                if (pattern.matcher(file.getFileName().toString()).find()) {
                    count += function.apply(file) ? 1 : 0;
                }
            }
            return count == max ? TERMINATE : CONTINUE;
        }
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return CONTINUE;
        }
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }

    /** */
    public static void main(final String[] args) throws IOException {
System.err.println(args[0]);
        ImageReader tmpIr = null;
        Iterator<ImageReader> irs = ImageIO.getImageReadersByFormatName("gif");
        while (irs.hasNext()) {
            tmpIr = irs.next();
            if (tmpIr.getClass().getName().equals(NonLzwGifImageReader.class.getName())) {
System.err.println("found ImageReader: " + tmpIr.getClass().getName());
                break;
            }
        }
        ImageReader ir = tmpIr;

        List<BufferedImage> images = new ArrayList<>();
        List<Path> errors = new ArrayList<>();

        Files.walkFileTree(Paths.get(args[0]), new RegexFileVisitor("\\.(gif|GIF)$", 1000, p -> {
            try {
                ir.setInput(new FileInputStream(p.toAbsolutePath().toString()));
                BufferedImage image = ir.read(0);
                images.add(image);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            } catch (Exception e) {
                System.err.println(p);
                e.printStackTrace();
                errors.add(p);
                return false;
            }
        }));

        System.err.println("*** ERRORS *** : " + errors.size());
//        errors.forEach(System.err::println);

        LoopCounter counter = new LoopCounter(images.size());

        JFrame frame = new JFrame();
        frame.setTitle(counter.get() + "/" + images.size());
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(images.get(counter.get()), 0, 0, this);
            }
        };
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                counter.increment();
                frame.setTitle(counter.get() + "/" + images.size());
                panel.repaint();
            }
        });
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}

/* */
