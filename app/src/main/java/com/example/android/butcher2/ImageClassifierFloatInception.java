/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.android.butcher2;

import android.app.Activity;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

import java.io.IOException;

import static org.opencv.imgproc.Imgproc.GaussianBlur;

/**
 * This classifier works with the Inception-v3 slim model.
 * It applies floating point inference rather than using a quantized model.
 */
public class ImageClassifierFloatInception extends ImageClassifier {

  private int outputW = 112;
  private int outputH = 112;
  private Mat mMat = null;

  /**
   * An array to hold inference results, to be feed into Tensorflow Lite as outputs.
   * This isn't part of the super class, because we need a primitive array here.
   */
  private float[][][][] heatMapArray = null;

  /**
   * Initializes an {@code ImageClassifier}.
   *
   * @param activity
   */
  ImageClassifierFloatInception(Activity activity) throws IOException {
    super(activity);
    heatMapArray = new float[1][outputW][outputH][14];
  }

  @Override
  protected String getModelPath() {
    // you can download this file from
    // https://storage.googleapis.com/download.tensorflow.org/models/tflite/inception_v3_slim_2016_android_2017_11_10.zip
    return "mv2-cpm-224.tflite";
  }

  @Override
  protected int getImageSizeX() {
    return 224;
  }

  @Override
  protected int getImageSizeY() {
    return 224;
  }

  @Override
  protected int getNumBytesPerChannel() {
    // a 32bit float value requires 4 bytes
    return 4;
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.putFloat(((float)(pixelValue & 0xFF)));
    imgData.putFloat(((float)((pixelValue >> 8) & 0xFF)));
    imgData.putFloat(((float)((pixelValue >> 16) & 0xFF)));
  }

  @Override
  protected float getProbability(int labelIndex) {

//    return labelProbArray[0][labelIndex];
    return 0;
  }

  @Override
  protected void setProbability(int labelIndex, Number value) {
//    labelProbArray[0][labelIndex] = value.floatValue();
  }

  @Override
  protected float getNormalizedProbability(int labelIndex) {
    // TODO the following value isn't in [0,1] yet, but may be greater. Why?
    return getProbability(labelIndex);
  }

  private float get(int x, int y, float[] arr) {
    if (x < 0 || y < 0 || x >= outputW || y >= outputH)
      return -1;
    else
      return arr[x * outputW + y];
  }

  @Override
  protected void runInference() {

    tflite.run(imgData, heatMapArray);

    if (mPrintPointArray == null)
      mPrintPointArray = new float[2][14];

    if (!CameraActivity.isOpenCVInit)
      return;

    // Gaussian Filter 5*5
    if (mMat == null)
      mMat = new Mat(outputW, outputH, CvType.CV_32F);

    float[] tempArray = new float[outputW * outputH];
    float[] outTempArray = new float[outputW * outputH];
    for (int i = 0; i <= 13; i++) {
      int index = 0;
      for (int x = 0; x < outputW; x++) {
        for (int y = 0; y < outputH; y++) {

//          if (heatMapArray[0][y][x][i] > 0){
//            Log.i("heatmapOutPut", "!!!!!!!!!!!=" + heatMapArray[0][y][x][i]);
//          }

          tempArray[index] = heatMapArray[0][y][x][i];
          index++;
        }
      }

      mMat.put(0, 0, tempArray);
      GaussianBlur(mMat, mMat, new Size(5.0, 5.0), 0.0, 0.0);
      mMat.get(0, 0, outTempArray);

      float maxX = 0;
      float maxY = 0;
      float max = 0;

      // Find keypoint coordinate through maximum values
      for (int x = 0; x < outputW; x++) {
        for (int y = 0; y < outputH; y++) {
          float center = get(x, y, outTempArray);
          if (center > max) {
            max = center;
            maxX = (float) x;
            maxY = (float) y;
          }
        }
      }

      if (max == 0) {
        mPrintPointArray = new float[2][14];
        return;
      }

      mPrintPointArray[0][i] = maxX;
      mPrintPointArray[1][i] = maxY;

    }
  }

}