[![Release](https://jitpack.io/v/umjammer/vavi-image.svg)](https://jitpack.io/#umjammer/vavi-image)
[![Java CI](https://github.com/umjammer/vavi-image/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-image/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-image/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-image/actions/workflows/codeql-analysis.yml)
[![GitHub Packages (only ffmpeg)](https://github.com/umjammer/vavi-image/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/umjammer/vavi-image/actions/workflows/maven-publish.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

# vavi-image

🎨 Imaging the world!

## Formats

  * artmaster 88 (old japanese pc pc-88,98 image format)
  * windows bitmap
  * gif (non lzw)
  * windows icon
  * ppm

## Resizing

|type|quality|speed|comment|
|---|---|---|---|
|`AwtResampleOp`|4|×|`java.awt.Image#getScaledInstance(int,int,Image.SCALE_AREA_AVERAGING)`|
|`FfmpegResamle`|3|○|??? (`AREA_AVERAGING`)|
|`Lanczos3ResampleOp`|5|×|👑|
|`G2dResample`|2|△|`Graphics2d#drawImage` with rendering hints (`VALUE_INTERPOLATION_NEAREST_NEIGHBOR`)| 
|`AffineTransformOp`|2| |`TYPE_NEAREST_NEIGHBOR`|

## Quantization

| type                                                                             |quality|comment|
|----------------------------------------------------------------------------------|---|---|
| [`ImageMagick`](src/main/java/vavi/awt/image/quantization/ImageMagikQuantizeOp.java) |||
| `NeuralNet`                                                                      |👑|https://github.com/umjammer/vavi-image-sandbox/wiki/OctTree_vs_NeuralNet|
| `OctTree`                                                                        |||

## Installation

 * maven repo.
```xml
    <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/umjammer/*</url>
    </repository>

    <dependency>
      <groupId>vavi</groupId>
      <artifactId>vavi-image</artifactId>
      <version>1.0.9</version>
    </dependency>
```
 * if you want to use ffmpeg resizing
   * [pom.xml](https://github.com/umjammer/vavi-image/wiki/Install-ffmpeg-native-library)
   * exec jvm w/ `java.library.path` system property e.g `-Djava.library.path=/target/test-classes`

## TODO

 * test `OctTreeQuantizer` (only 256 colors?)  
 * complete `ImageMagikQuantizer`
 * Lanczos3 filter using AWT API
 * `BufferedImageOp` ???
 * https://github.com/iariro/N88BasicImage
 * ffmpeg resize 4byte 32bit operation is wrong

## Tech Know

 * Mac Open JDK's JNI library extension is `.dylib`
 * `libsescale` has MMX bug, this causes segmentation fault when resizing image.

## License

### Image I/O PPM Reader

Public Domain

### ImageMagik

```
/*
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files ("ImageMagick"),
 *  to deal in ImageMagick without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of ImageMagick, and to permit persons to whom the
 *  ImageMagick is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of ImageMagick.
 *
 *  The software is provided "as is", without warranty of any kind, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement.  In no event shall
 *  E. I. du Pont de Nemours and Company be liable for any claim, damages or
 *  other liability, whether in an action of contract, tort or otherwise,
 *  arising from, out of or in connection with ImageMagick or the use or other
 *  dealings in ImageMagick.
 *
 *  Except as contained in this notice, the name of the E. I. du Pont de
 *  Nemours and Company shall not be used in advertising or otherwise to
 *  promote the sale, use or other dealings in ImageMagick without prior
 *  written authorization from the E. I. du Pont de Nemours and Company.
 */
```

### Java Image Editor

| The downloadable source code on this page is released under the Apache License. Basically, this means that you are free to do whatever you like with this code, but it's not my fault if your satellite/nuclear power station/missile system fails as a result. Have fun!
|
| Licensed under the Apache License, Version 2.0 (the "License"); you may not use this code except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

### SkyView

 * none?

### Java Imaging Utilities  

 * GNU General Public License (GPL) version 2 

### libswscale (ffmpeg)  

 * GNU General Public License (GPL) version 2 
 * FFmpeg License and Legal Considerations 
