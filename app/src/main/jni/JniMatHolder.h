//
// Created by Neso on 2017-04-19.
//

#ifndef KANDIDAT360DEGREES_JNIMATHOLDER_H
#define KANDIDAT360DEGREES_JNIMATHOLDER_H

#include <android/log.h>
#include <jni.h>
#include <stdint.h>
#include "opencv2/core/core.hpp"
#include <vector>

#define  LOG_TAG    "DEBUG"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


extern "C"
  {
  JNIEXPORT jobject JNICALL Java_com_ciux031701_kandidat360degrees_imageprocessing_JniMatHolder_jniStoreMatData(JNIEnv * env, jobject obj, jobject mat);
  JNIEXPORT jobject JNICALL Java_com_ciux031701_kandidat360degrees_imageprocessing_JniMatHolder_jniGetMatFromStoredData(JNIEnv * env, jobject obj, jobject handle);
  JNIEXPORT void JNICALL Java_com_ciux031701_kandidat360degrees_imageprocessing_JniMatHolder_jniFreeMatData(JNIEnv * env, jobject obj, jobject handle);
  }

jobject getJMatFromHandle(JNIEnv * env, jobject handle);
cv::Mat* getMatFromHandle(JNIEnv * env, jobject handle);
std::vector<cv::Mat> getMatVectorFromHandles(JNIEnv * env, jobject handleList);
void freeMatData(JNIEnv * env, jobject handle);
void freeMatDataList(JNIEnv * env, jobject handleList);

class JniMat{
  public:
    unsigned char * data;
    int width;
    int height;
    int channels;
    int type;
    JniMat()
      {
        data = NULL;
        width = 0;
        height = 0;
      }
};

#endif //KANDIDAT360DEGREES_JNIMATHOLDER_H
