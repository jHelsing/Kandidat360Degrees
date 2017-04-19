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


#include "com_ciux031701_kandidat360degrees_representation_NativePanorama.h"
JNIEXPORT void JNICALL Java_com_ciux031701_kandidat360degrees_representation_NativePanorama_processPanorama
    (JNIEnv * env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress) {
        jsize nbrOfImages = env->GetArrayLength(imageAddressArray);
        //Convert imageAddressArray to an array of jlong
        jlong *imgAddressArr = env->GetLongArrayElements(imageAddressArray,0);
        //A vector to store all the images
        vector<Mat> imageVector;
        for(int i=0; i < nbrOfImages; i++){
            Mat & currentImage = *(Mat*)imgAddressArr[i];
            Mat newImage;
            //Convert to 3-channel Mat (for Stitcher module)
            cvtColor(currentImage,newImage,CV_BGRA2BGR);
            //Reduce resolution for fast computation --> we may want to remove this part
            //float scale = 1000.0f / currentImage.rows;
            //resize(newImage,newImage,Size(scale*currentImage.rows,scale*currentImage.cols));
            imageVector.push_back(newImage); //add last in the vector
        }
        Mat & result = *(Mat*) outputAddress;
        Stitcher stitcher = Stitcher::createDefault();

        //Set parameters:
        //Warper
        stitcher.setWarper(new CylindricalWarper());
        
        //Feature finder with ORB-algorithm
        stitcher.setFeaturesFinder(new cv::detail::OrbFeaturesFinder());

        //Exposure compensator (should test more if this or BlockGainCompensator (default) is the best)
        stitcher.setExposureCompensator(makePtr<detail::GainCompensator>());

        //OBS: should only use homography model for all parameters (panorama mode)

        stitcher.stitch(imageVector,result);
        //Release imgAdressArr
        env->ReleaseLongArrayElements(imageAddressArray,imgAddressArr,0);
    }

  JNIEXPORT void JNICALL Java_com_ciux031701_kandidat360degrees_representation_NativePanorama_processPanoramaFromHandles(JNIEnv * env, jobject obj, jobject handleList, jlong outputAddress){
    LOGD("--Stitching--");
    vector<Mat> matVector = getMatVectorFromHandles(env, handleList);
    LOGD("acquired mat vector");
    vector<Mat> newVector;
    for(int i = 0; i < matVector.size();i++){
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
    Mat & result = *(Mat*) outputAddress;
    Stitcher stitcher = Stitcher::createDefault(true);
    stitcher.stitch(newVector,result);
    LOGD("STITCHING RESULTS:");
    LOGD("width = %d", result.cols);
    LOGD("height = %d", result.rows);
  }
