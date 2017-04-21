//
// Created by Anna on 2017-03-22.
//
#include "com_ciux031701_kandidat360degrees_representation_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"
#include "opencv2/core/core.hpp"
#include "JniMatHolder.h";
#include <android/bitmap.h>
#include <stdio.h>
#include <vector>
#include <string.h>
#include <jni.h>
#include "HeapInfo.h"

using namespace std;
using namespace cv;

JNIEXPORT jobject JNICALL
Java_com_ciux031701_kandidat360degrees_representation_NativePanorama_processPanoramaFromHandles(
        JNIEnv *env, jobject obj, jobject handleList) {
    LOGD("--Stitching--");
    vector<Mat> matVector = getMatVectorFromHandles(env, handleList);
    LOGD("acquired mat vector");
    vector<Mat> newVector;
    for (int i = 0; i < matVector.size(); i++) {
        LOGD("-conversion to 3 channels-");
        Mat newMat;
        cvtColor(matVector[i], newMat, CV_BGRA2BGR);
        newVector.push_back(newMat);
        LOGD("Mat %d conversion OK!", i);
        LOGD("New type: %d", newMat.type());
    }
    LOGD("heap size: %d", getNativeHeapSize(env));
    LOGD("heap allocated: %d", getNativeHeapAllocatedSize(env));
    freeMatDataList(env, handleList);
    LOGD("Old data deallocated");
    LOGD("heap size: %d", getNativeHeapSize(env));
    LOGD("heap allocated: %d", getNativeHeapAllocatedSize(env));

    Stitcher stitcher = Stitcher::createDefault(true);
    //Set parameters:
    //Warper
    stitcher.setWarper(new CylindricalWarper());

    //Feature finder with ORB-algorithm
    stitcher.setFeaturesFinder(new cv::detail::OrbFeaturesFinder());

    //Exposure compensator (should test more if this or BlockGainCompensator (default) is the best)
    stitcher.setExposureCompensator(makePtr<detail::GainCompensator>());
    Mat result;
    //OBS: should only use homography model for all parameters (panorama mode)
    stitcher.stitch(newVector, result);
    LOGD("STITCHING RESULTS:");
    LOGD("width = %d", result.cols);
    LOGD("height = %d", result.rows);

    long length = result.cols * result.rows * result.channels();
    unsigned char * data = new unsigned char[length]();
    memcpy(data, result.data, length);
    JniMat * out = new JniMat();
    out->width=result.cols;
    out->height=result.rows;
    out->type=result.type();
    out->channels=result.channels();
    out->data = data;
    return env->NewDirectByteBuffer(out, 0);
}
