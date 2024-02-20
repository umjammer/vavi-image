/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

#include <stdio.h>
#include <jni.h>
#include "libavutil/avutil.h"
#include "libavformat/avformat.h"
#include "vavi_awt_image_resample_FfmpegResampleOp.h"

/*
 * FfmpegResampleOp Wrapper
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">nsano</a>
 * @version 0.00 061030 nsano initial version <br>
 */

#if FFMPEG_VERSION == 0x000409

#include "avformat.h"
#include "avcodec.h"
#include "avutil.h"

//-----------------------------------------------------------------------------

/**
 * Throws an exception.
 * @param exception "java/lang/Exception"
 */
static void throwExceptionWithStringMessage(JNIEnv *env, char *exception, char *_message) {

    jclass class = (*env)->FindClass(env, exception);
    (*env)->ThrowNew(env, class, _message);
}

//-----------------------------------------------------------------------------

#ifdef __cplusplus
extern "C" {
#endif

/*
 * for ffmpeg 0x0409 (mobile radio 1 version)
 *
 * Class:     vavi_awt_image_resample_ResampleOp
 * Method:    filterInternal
 * Signature: (Ljava/lang/Object;IILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_vavi_awt_image_resample_FfmpegResampleOp_filterInternal
(JNIEnv *env, jobject obj, jintArray inBuffer, jint inType, jint inPixelSize, jint inWidth, jint inHeight, jobject outBuffer, jint outType, jint outPixelSize, jint outWidth, jint outHeight) {

    int *inBuf = (int *) (*env)->GetByteArrayElements(env, (jbyteArray) inBuffer, NULL);                // TODO assume byte[]

    AVPicture inPicture;
    avpicture_fill(&inPicture, (uint8_t *) inBuf, PIX_FMT_RGB24, inWidth, inHeight);                    // TODO assume PIX_FMT_RGB24
//fprintf(stderr, "here1: %d, %d, %d\n", (int) inWidth, (int) inHeight, sizeof(inBuf));
//fflush(stderr);
    AVPicture tmpInPicture;
    int tmpInSize = avpicture_get_size(PIX_FMT_YUV420P, inWidth, inHeight);
    uint8_t *tmpInBuf = (uint8_t *) av_malloc(tmpInSize);
    avpicture_fill(&tmpInPicture, tmpInBuf, PIX_FMT_YUV420P, inWidth, inHeight);
    int ret = img_convert(&tmpInPicture, PIX_FMT_YUV420P, &inPicture, PIX_FMT_RGB24, inWidth, inHeight);
//fprintf(stderr, "here1.5: %d\n", ret);
//fflush(stderr);
    if (ret < 0) {
        throwExceptionWithStringMessage(env, "java/lang/IllegalStateException", "Converting into YUV420P");
    }
//fprintf(stderr, "here2\n");
//fflush(stderr);

    AVPicture tmpOutPicture;
    int tmpOutSize = avpicture_get_size(PIX_FMT_YUV420P, outWidth, outHeight);
    uint8_t *tmpOutBuf = (uint8_t *) av_malloc(tmpOutSize);
    avpicture_fill(&tmpOutPicture, tmpOutBuf, PIX_FMT_YUV420P, outWidth, outHeight);    
//fprintf(stderr, "here3\n");
//fflush(stderr);

    ImgReSampleContext *irsc = img_resample_init(outWidth, outHeight, inWidth, inHeight);
    img_resample(irsc, &tmpOutPicture, &tmpInPicture); // !!! ONLY ONLY ACCEPT YUV420P !!!
    img_resample_close(irsc);
//fprintf(stderr, "here4\n");
//fflush(stderr);

    int *outBuf = (int *) (*env)->GetDirectBufferAddress(env, outBuffer);

    AVPicture outPicture;
    avpicture_fill(&outPicture, (uint8_t *) outBuf, PIX_FMT_RGB24, outWidth, outHeight);                // TODO assumed PIX_FMT_RGB24
    ret = img_convert(&outPicture, PIX_FMT_RGB24, &tmpOutPicture, PIX_FMT_YUV420P, outWidth, outHeight);
    if (ret < 0) {
        throwExceptionWithStringMessage(env, "java/lang/IllegalStateException", "Converting into RGBA");
    }
//fprintf(stderr, "here5\n");
//fflush(stderr);

    (*env)->ReleaseByteArrayElements(env, (jbyteArray) inBuffer, (jbyte *) inBuf, JNI_ABORT);
    av_free(tmpInBuf);
    av_free(tmpOutBuf);
}

#ifdef __cplusplus
}
#endif

#else

#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * for ffmpeg 5
 *
 * Class:     vavi_awt_image_resample_FfmpegResampleOp
 * Method:    filterInternal2
 * Signature: (Ljava/lang/Object;IILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_vavi_awt_image_resample_FfmpegResampleOp_filterInternal
(JNIEnv *env, jobject obj, jintArray inBuffer, jint inType, jint inPixelFormat, jint inPixelSize, jint inWidth, jint inHeight, jobject outBuffer, jint outType, jint outPixelFormat, jint outPixelSize, jint outWidth, jint outHeight, jint hint) {

//fprintf(stderr, "here 0: %d, %d, %d, %d\n", (int) inType, (int) outType, (int) inPixelSize, (int) outPixelSize);
//fflush(stderr);
    // 1. initialize
    uint8_t *inBuf __attribute__ ((aligned(16)));
    if (inType == 0) { // TYPE_BYTE
        inBuf = (uint8_t *) (*env)->GetByteArrayElements(env, (jbyteArray) inBuffer, NULL); // TYPE_BYTE
    } else {
        inBuf = (uint8_t *) (*env)->GetIntArrayElements(env, (jintArray) inBuffer, NULL); // TYPE_INT
    }
    enum AVPixelFormat inFormat;
    if (inPixelSize == 24) {
        inFormat = AV_PIX_FMT_RGB24;
    } else {
        switch (inPixelFormat) {
        case 10: // TYPE_BYTE_GRAY
            inFormat = AV_PIX_FMT_GRAYF32;
            break;
        case 2: // TYPE_4BYTE_ABGR
            inFormat = AV_PIX_FMT_BGR32_1;
            break;
        case 6: // TYPE_INT_ARGB
            inFormat = AV_PIX_FMT_RGB32_1;
            break;
        default:
            inFormat = AV_PIX_FMT_BGR32_1;
            break;
        }
    }
//fprintf(stderr, "in format: %d\n", (int) inFormat);
//fflush(stderr);

    uint8_t *outBuf __attribute__ ((aligned(16)));
    if (outType == 0) { // TYPE_BYTE
        outBuf = (uint8_t *) (*env)->GetByteArrayElements(env, (jbyteArray) outBuffer, NULL); // TYPE_BYTE
    } else {
        outBuf = (uint8_t *) (*env)->GetIntArrayElements(env, (jintArray) outBuffer, NULL); // TYPE_INT
    }

    enum AVPixelFormat outFormat;
    if (outPixelSize == 24) {
        outFormat = AV_PIX_FMT_BGR24; // TODO why ???
    } else {
        switch (inPixelFormat) {
        case 10: // TYPE_BYTE_GRAY
            outFormat = AV_PIX_FMT_GRAYF32;
            break;
        case 2: // TYPE_4BYTE_ABGR
            outFormat = AV_PIX_FMT_BGR32_1;
            break;
        case 6: // TYPE_INT_ARGB
            outFormat = AV_PIX_FMT_RGB32_1;
            break;
        default:
            outFormat = AV_PIX_FMT_BGR32_1;
            break;
        }
    }
//fprintf(stderr, "out format: %d\n", (int) outFormat);
//fflush(stderr);

    // 2. rescale
    struct SwsContext *swsContext = sws_getContext(inWidth, inHeight, inFormat, outWidth, outHeight, outFormat, hint | SWS_ACCURATE_RND, NULL, NULL, NULL);
//#ifdef __x86_64__
//fprintf(stderr, "here 0b: %lx\n", (long unsigned int) swsContext);
//#else
//fprintf(stderr, "here 0b: %x\n", (int) swsContext);
//#endif
//fflush(stderr);

//  uint8_t *outTemp __attribute__ ((aligned(16))) = (uint8_t *) malloc(avpicture_get_size(outFormat, outWidth, outHeight) + outWidth);
    AVFrame* inPic = av_frame_alloc();
    inPic->format = inFormat;
    inPic->width = inWidth;
    inPic->height = inHeight;
    av_frame_get_buffer(inPic, 1);
    av_image_fill_arrays(inPic->data, inPic->linesize, inBuf, inFormat, inWidth, inHeight, 1);
    AVFrame* outPic = av_frame_alloc();
    outPic->format = outFormat;
    outPic->width = outWidth;
    outPic->height = outHeight;
    av_frame_get_buffer(outPic, 1);

//fprintf(stderr, "pic: %d\n", sizeof(inPic));
//fprintf(stderr, "in: %d, out: %d, 32: %d\n", inFormat, outFormat, AV_PIX_FMT_RGB32_1);
//fflush(stderr);
//fprintf(stderr, "here 1b: %d, %d, %d, %d\n", (int) ((*env)->GetArrayLength(env, (jbyteArray) inBuffer) * (inPixelSize / 8)),
//                                             avpicture_get_size(inFormat, inWidth, inHeight),
//                                             (int) ((*env)->GetArrayLength(env, (jbyteArray) outBuffer) * (outPixelSize / 8)),
//                                             avpicture_get_size(outFormat, outWidth, outHeight));
//fflush(stderr);
//int i;
//for (i = 0; i < AV_NUM_DATA_POINTERS; i++) {
// fprintf(stderr, "%d: %d, %d\n", i, inPic.linesize[i], outPic.linesize[i]);
//}
//fflush(stderr);
    sws_scale(swsContext, inPic->data, inPic->linesize, 0, inHeight, outPic->data, outPic->linesize);

    size_t size = av_image_get_buffer_size(outFormat, outPic->width, outPic->height, 1);
    av_image_copy_to_buffer(outBuf, size, outPic->data, outPic->linesize, outFormat, outWidth, outHeight, 1);

    sws_freeContext(swsContext);

//  memcpy(outBuf, outTemp, avpicture_get_size(outFormat, outWidth, outHeight));
//  free(outTemp);

    // 3. clean up
    if (inType == 0) {
        (*env)->ReleaseByteArrayElements(env, (jbyteArray) inBuffer, (jbyte *) inBuf, JNI_ABORT); // TYPE_BYTE
    } else {
        (*env)->ReleaseIntArrayElements(env, (jintArray) inBuffer, (jint *) inBuf, JNI_ABORT); // TYPE_INT
    }
    if (outType == 0) {
        (*env)->ReleaseByteArrayElements(env, (jbyteArray) outBuffer, (jbyte *) outBuf, 0); // TYPE_BYTE
    } else {
        (*env)->ReleaseIntArrayElements(env, (jintArray) outBuffer, (jint *) outBuf, 0); // TYPE_INT
    }
}

#ifdef __cplusplus
}
#endif

#endif

/* */
