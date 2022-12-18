/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * IdeTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-09 nsano initial version <br>
 */
@EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
class IdeTest {

    /** using cdl cause junit stops awt thread suddenly */
    CountDownLatch cdl;

    @BeforeEach
    void setup() {
        cdl = new CountDownLatch(1);
    }

    @Test
    void run_Scaling_awt_ffmpeg() throws Exception {
        Scaling_awt_ffmpeg.main(new String[] {"src/test/resources/erika.jpg"});
    }

    @Test
    void run_Scaling_awt_java2d() throws Exception {
        Scaling_awt_java2d.main(new String[] {"src/test/resources/erika.jpg"});
    }

    @Test
    void run_GifUnderDirectory() throws Exception {
        GifUnderDirectory.main(new String[] {"src/test/resources"});
    }

    @Test
    void run_JpegBlur() throws Exception {
        JpegBlur.main(new String[] {"src/test/resources/erika.jpg"});
    }

    @Test
    void run_JpegQuality() throws Exception {
        JpegQuality.main(new String[] {"src/test/resources/erika.jpg"});
    }

    @Test
    void run_FontOnImage() throws Exception {
        FontOnImage.main(new String[] {"src/test/resources/erika.jpg", "Hello Erika", "Zapfino"});
    }

    @Test
    void run_JpegQualityBlur() throws Exception {
        JpegQualityBlur.main(new String[] {"src/test/resources/erika.jpg"});
    }

    @AfterEach
    void teardown() throws Exception {
        cdl.await(); // depends on each test frame's exit on close
    }
}
