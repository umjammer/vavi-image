/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.gif;

import vavi.util.Debug;


/**
 * 非 LZW 理論 GIF デコーダ。
 *
 * @author DJ.Uchi [H.Uchida]
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 1.20 xxxxxx huchida original version <br>
 * @version 2.00 040913 nsano port to java <br>
 */
public class NonLzwGifDecoder {
    /**
     * DIB データ書き込み用構造体
     * DIB に画像データを書き込む際に必要な情報をまとめた物。
     */
    class RgbContext {
        /** 展開データ書き込み位置(同一ライン上でのオフセット値) */
        int xPoint;
        /** 展開データ書き込みオフセット(ライン数 * アラインメント) */
        int offset;
        /** 横 pixel 数 */
        int width;
        /** 縦 pixel 数 */
        int height;
        /** アラインメント値(１ライン分のバイト数) */
        int bytesPerLine;
        /** カラービット値 */
        int colorDepth;
        /** 展開データ書き込みライン(インタレース時に使用) */
        int currentLine;
        /** インタレースオフセット(ライン数) */
        int interlaceOffset;
        /** インタレースフラグ true でインターレース */
        boolean interlaced;
    }

    /**
     * GIF データ解析用構造体
     * GIF の符号化コード取得に必要な情報をまとめた物。
     */
    class GifContext {
        /** RgbDecodeStatus 構造体参照用ポインタ */
        RgbContext rgb;
        /** コードサイズ(CS) */
        int codeSize;
        /** ビットサイズ(CBL) */
        int bitSize;
        /** クリアコード */
        int clearCode;
        /** エンドコード */
        int endCode;
        /** 現エントリ数 */
        int entry;
        /** 現ビット位置 */
        int bitPoint;
        /** 次ブロック開始位置 */
        int nextBlock;
        /** データサイズ */
        int dataSize;
    }

    /**
     * GIF 画像(最初の一枚のみ)を展開する。
     * 入力は GIF データ、出力は RGB。
     * ここでは主にデコードの前処理を行います。
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
        int[] lzw = new int[8192]; // 圧縮データ格納用配列(辞書テーブルではない！)
        int code = 0; // 符号化コード初期化

        // DIB データ書き込み用構造体初期化
        RgbContext rgb = new RgbContext();
        rgb.xPoint = 0; // 展開データ書き込み位置初期化
        rgb.offset = (height - 1) * bytesPerLine; // 書き込み位置オフセット
        rgb.width = width; // 横 pixel 数取得
        rgb.height = height; // 縦 pixel 数取得
        rgb.bytesPerLine = bytesPerLine; // アラインメント値取得
        rgb.colorDepth = colorDepth; // カラービット値取得
        rgb.currentLine = 0; // 展開データ書き込みライン初期化
        rgb.interlaceOffset = 8; // インタレースオフセット初期化
        rgb.interlaced = interlaced; // インタレースフラグ取得
//Debug.println(StringUtil.paramString(rgb));

        // GIF データ解析用構造体初期化
        GifContext gif = new GifContext();
        gif.rgb = rgb; // RgbDecodeStatus 構造体参照用ポインタ取得
        gif.codeSize = data[0] & 0xff; // コードサイズ取得
        gif.bitSize = gif.codeSize + 1; // CBL 初期化 (コードサイズ + 1)
        gif.clearCode = 1 << gif.codeSize; // クリアコード設定 (2 ^ コードサイズ)
        gif.endCode = gif.clearCode + 1; // エンドコード設定 (2 ^ コードサイズ + 1)
        gif.entry = gif.endCode + 1; // エントリ数初期化 (2 ^ コードサイズ + 2)
        gif.bitPoint = 8; // ビットポインタ初期化 (8 bit = 1 byte)
        gif.nextBlock = 1; // 次ブロック位置初期化 (先頭ブロック)
        gif.dataSize = size; // GIF データサイズ取得
//Debug.println(StringUtil.paramString(gif));

        int times = 0; // 展開回数初期化
        int offset = 0; // 展開開始位置初期化

        // エンドコードが現れるまでひたすらループ
        while ((code = getCode(data, gif)) != gif.endCode) {
            // クリアコードが現れた場合
//System.err.println("code: " + code);
            if (code == gif.clearCode) {
                // エントリ数 & CBL 初期化
                gif.entry = gif.endCode + 1;
                gif.bitSize = gif.codeSize + 1;

                // 展開関数をコール。
                decodeLzw(vram, lzw, gif, times, offset);
                times = 0;
                offset = 0;
            } else {
                // int サイズ配列に取得した符号化コードを溜め込む。
                // これは展開作業をより円滑に行う為であり、辞書テーブルとは異なります。
                // 配列に溜め込まずに取得した符号化コードを直に展開することも出来ますが、
                // 速度的に問題がある為、この方法を採用しています。
                lzw[times++] = code & 0xffff;

                // いつまでもクリアコードが現れないと配列が溢れるため、
                // 符号化コードが 8192 個貯まった時点で展開作業を行う。
                // 非圧縮 GIF 対策か？
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

        // 配列に残っている符号化コードを展開する。
        decodeLzw(vram, lzw, gif, times, offset);

        return vram;
    }

    /**
     * 可変ビット長入力関数。
     * 符号化コードを一つ取り出して、ビット位置をインクリメントする。
     */
    private int getCode(byte[] data, GifContext gif) {
        int code = 0; // 符号化コード初期化
        int bytePoint = gif.bitPoint >> 3; // 読み込み位置(バイト単位)取得
//System.err.println("pt: " + bytePoint);

        // サイズオーバーフローの場合、強制的にエンドコードを返す(破損ファイル対策)
        if ((bytePoint + 2) > gif.dataSize) {
Debug.println("maybe broken");
            return gif.endCode;
        }

        // 符号化コード取得
        int i = 0; // 読み込みバイト数初期化
        while ((((gif.bitPoint + gif.bitSize) - 1) >> 3) >= bytePoint) {
            // 読み込み中にブロックが終了した場合
            if (bytePoint == gif.nextBlock) {
                gif.nextBlock += ((data[bytePoint++] & 0xff) + 1); // 次ブロック位置更新
                gif.bitPoint += 8; // ビットポインタを１バイト分加算
            }
            code += ((data[bytePoint++] & 0xff) << i); // コード取得
            i += 8;
        }

        // 得られたコードの余分なビットを切りとばす。(マスキング処理)
        code = (code >> (gif.bitPoint & 0x07)) & ((1 << gif.bitSize) - 1);

        // ビットポインタ更新
        gif.bitPoint += gif.bitSize;

        // CBL をインクリメントする必要があるかどうか確認する。
        if (gif.entry > ((1 << gif.bitSize) - 1)) {
            gif.bitSize++;
        }

        return code;
    }

    /**
     * 非 LZW 理論展開関数 (メインループ)
     * int サイズの配列に格納された符号化コードをデコードします。
     */
    private void decodeLzw(byte[] vram, int[] lzw, GifContext gif, int times, int offset) {
        // 単にループを回して展開関数を呼んでるだけ。
//Debug.println(" times: " + times + ", offset: " + offset);
        for (int i = offset; i < times; i++) {
            getLzwBytes(vram, lzw, gif, i);
        }
    }

    /**
     * 非 LZW 理論展開関数 (コア)
     * 非 LZW 理論の核。
     * 指定された符号化コードに対する展開データを返します。
     */
    private void getLzwBytes(byte[] vram, int[] lzw, GifContext gif, int offset) {
        // 配列から符号化コードを一つ取り出します。
        int code = lzw[offset];

        if (code < gif.clearCode) {
            // 符号化コードが "色数" より小さい場合
            // 符号化コードをそのまま RGB に書き込む。
            writeRgb(vram, gif, code);

        } else if (code > gif.endCode + offset--) {
            // 符号化コードが未知のものである場合

            // 一つ前の展開データ
            getLzwBytes(vram, lzw, gif, offset);

            // 一つ前の展開データの先頭一個
            getLzwByte(vram, lzw, gif, offset);

        } else {
            // 符号化コードが "色数 + 1" より大きい場合
            // ちなみに、この関数に入ってくるコードにエンドコードやクリアコードは
            // 絶対に現れませんので、その場合の処理は考慮されていません。

            // (符号化コード - 色数 + 1) の展開データ
            getLzwBytes(vram, lzw, gif, code - gif.endCode - 1);

            // (符号化コード - 色数 + 2) の展開データの先頭一個
            getLzwByte(vram, lzw, gif, code - gif.endCode);
        }
    }

    /**
     * 非 LZW 理論展開関数 (サブ)
     * 非 LZW 理論の核。
     * 指定された符号化コードに対する展開データの先頭１つを返します。
     */
    private void getLzwByte(byte[] vram, int[] lzw, GifContext gif, int offset) {
        int code;

        // 配列から符号化コードを一つ取り出し、"色数" より小さかどうか確認。
        while ((code = lzw[offset]) >= gif.clearCode) {
            // 符号化コードが未知のものである場合
            if (code > gif.endCode + offset) {
                // 一つ前の展開データの先頭一個
                offset--;

                // 符号化コードが "色数 + 1" より大きい場合
            } else {
                // (符号化コード - 色数 + 1) の展開データの先頭一個
                offset = code - gif.endCode - 1;
            }
        }

        // 得られた展開データをDIBに書き込む。
        writeRgb(vram, gif, code);
    }

    /**
     * RGB 画像データ書き込み関数。
     * 展開された画像データを RGB として書き込みます。
     */
    private void writeRgb(byte[] rgb, GifContext gif, int code) {
        // RGB に画像データを書き込みます。
        // モノクロや 16 色の場合はビット単位での書き込みになる為、マスキング処理を行います。
        int i;

        // RGB に画像データを書き込みます。
        // モノクロや 16 色の場合はビット単位での書き込みになる為、マスキング処理を行います。
        int j;
//Debug.println("gif.rgb.color: " + gif.rgb.colors);
//Debug.println("rgb.offset: " + gif.rgb.offset + ", rgb.rgb.point: " + gif.rgb.xPoint + ", code: " + code);
//System.out.printf("%d\n", code);
        switch (gif.rgb.colorDepth) {
        case 1: // モノクロ画像の場合
            i = gif.rgb.offset + (gif.rgb.xPoint / 8);
            j = 7 - (gif.rgb.xPoint & 0x07);
            rgb[i] = (byte) ((rgb[i] & ~(1 << j)) | (code << j));
//Debug.println("x: " + gif.rgb.xPoint + ", y: " + (gif.rgb.offset / gif.rgb.bytesPerLine) + " / w: " + gif.rgb.width + ", h: " + gif.rgb.height + ": " + StringUtil.toHex2(rgb[i]));
            break;
        case 4: // 16色画像の場合
            i = gif.rgb.offset + (gif.rgb.xPoint >> 1);
            j = (gif.rgb.xPoint & 0x01) << 2;
            rgb[i] = (byte) ((rgb[i] & (0x0f << j)) | (code << (4 - j)));
            break;
        default: // 256色の場合
            rgb[gif.rgb.offset + gif.rgb.xPoint] = (byte) code;
            break;
        }

        // 書き込み位置をインクリメント
        gif.rgb.xPoint++;

        // 書き込み位置がラインの終端に達した場合
        if (gif.rgb.xPoint == gif.rgb.width) {
//Debug.println("y: " + (gif.rgb.offset / gif.rgb.bytesPerLine));
            if (gif.rgb.interlaced) { // インタレース GIF の場合

                // インタレースラインが画面下端に達した場合
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
            } else { // リニア GIF の場合
                gif.rgb.offset -= gif.rgb.bytesPerLine;
            }
            gif.rgb.xPoint = 0;
        }
    }
}

/* */
