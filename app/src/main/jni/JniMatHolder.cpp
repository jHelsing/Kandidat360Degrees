//
// Created by Neso on 2017-04-19.
//

#include "JniMatHolder.h"
#include <stdlib.h>
JNIEXPORT jobject JNICALL Java_com_ciux031701_kandidat360degrees_imageprocessing_JniMatHolder_jniStoreMatData(JNIEnv * env, jobject obj, jobject mat){
    jclass Mat_class = env->FindClass("org/opencv/core/Mat");
    jmethodID Mat_total = env->GetMethodID(Mat_class, "total", "()J");
    jmethodID Mat_channels = env->GetMethodID(Mat_class, "channels", "()I");
    jmethodID Mat_get = env->GetMethodID(Mat_class, "get", "(II[B)I");
    jmethodID Mat_rows = env->GetMethodID(Mat_class, "rows", "()I");
    jmethodID Mat_cols = env->GetMethodID(Mat_class, "cols", "()I");
    jmethodID Mat_type = env->GetMethodID(Mat_class, "type", "()I");

    LOGD("--Entering method calls--");
    long total = env->CallLongMethod(mat, Mat_total);
    LOGD("total: %d", total);
    int channels = env->CallIntMethod(mat, Mat_channels);
    LOGD("channels: %d", channels);
    long length = total * channels;
    unsigned char * data = new unsigned char[length]();
    LOGD("data memory allocated");
    jbyteArray jdata = env->NewByteArray(length);
    LOGD("created new bytearray of length %d", length);
    int get_result = env->CallIntMethod(mat, Mat_get, 0, 0, jdata);
    LOGD("populated jbyteArray with data");
    jboolean isCopy;
    unsigned char * elements = (unsigned char*)env->GetByteArrayElements(jdata, &isCopy);
    LOGD("cast jbyteArray elements into unsigned char *");
    memcpy(data, elements, sizeof(unsigned char) * length);
    LOGD("copied the elements");
    if(isCopy)
        env->ReleaseByteArrayElements(jdata, (jbyte*)elements, JNI_ABORT);
    LOGD("released byte array elements");

    JniMat * jniMat = new JniMat();
    jniMat->width = env->CallIntMethod(mat, Mat_cols);
    jniMat->height = env->CallIntMethod(mat, Mat_rows);
    jniMat->channels = channels;
    jniMat->type = env->CallIntMethod(mat, Mat_type);
    jniMat->data = data;

    LOGD("--Storing Mat--");
    LOGD("width: %d", jniMat->width);
    LOGD("height: %d", jniMat->height);
    LOGD("type: %d", jniMat->type);

    return env->NewDirectByteBuffer(jniMat,0);

}

JNIEXPORT jobject JNICALL Java_com_ciux031701_kandidat360degrees_imageprocessing_JniMatHolder_jniGetMatFromStoredData(JNIEnv * env, jobject obj, jobject handle){
    return getJMatFromHandle(env, handle);
}

jobject getJMatFromHandle(JNIEnv * env, jobject handle){
   jclass Mat_class = env->FindClass("org/opencv/core/Mat");
   jmethodID Mat_construct = env->GetMethodID(Mat_class, "<init>", "(III)V");
   jmethodID Mat_put = env->GetMethodID(Mat_class, "put", "(II[B)I");

   LOGD("--Entering JMat construction--");
   JniMat * jniMat = (JniMat*)env->GetDirectBufferAddress(handle);
   int length = jniMat->width * jniMat->height * jniMat->channels;
   LOGD("fetched JniMat * from handle");
   if(jniMat->data == NULL){
       LOGE("Data was null...");
       return NULL;
   }
   jbyteArray jdata = env->NewByteArray(length);
   env->SetByteArrayRegion(jdata, 0, length, (jbyte*)jniMat->data);
   jobject newMat = env->NewObject(Mat_class, Mat_construct, jniMat->height, jniMat->width, jniMat->type);
   int put_result = env->CallIntMethod(newMat, Mat_put, 0, 0, jdata);
   return newMat;
}

cv::Mat* getMatFromHandle(JNIEnv * env, jobject handle){
       LOGD("--Entering CMat construction--");
       JniMat * jniMat = (JniMat*)env->GetDirectBufferAddress(handle);
       return new cv::Mat(jniMat->height, jniMat->width, jniMat->type, jniMat->data);
}

std::vector<cv::Mat> getMatVectorFromHandles(JNIEnv * env, jobject handleList){
    jclass ArrayList_class = env->FindClass("java/util/ArrayList");
    jmethodID ArrayList_get = env->GetMethodID(ArrayList_class, "get", "(I)Ljava/lang/Object;");
    jmethodID ArrayList_size = env->GetMethodID(ArrayList_class, "size", "()I");
    LOGD("--Entering CMat vector construction--");
    std::vector<cv::Mat> matVector;
    int size = env->CallIntMethod(handleList, ArrayList_size);
    for(int i = 0; i < size; i++){
        LOGD("-Mat %d-", i);
        jobject handle = env->CallObjectMethod(handleList, ArrayList_get, i);
        LOGD("handle acquired");
        cv::Mat* mat = getMatFromHandle(env, handle);
        LOGD("mat constructed");
        matVector.push_back(*mat);
        LOGD("JniMat %d deallocated", i);
    }
    return matVector;
}

void freeMatDataList(JNIEnv * env, jobject handleList){
        jclass ArrayList_class = env->FindClass("java/util/ArrayList");
        jmethodID ArrayList_get = env->GetMethodID(ArrayList_class, "get", "(I)Ljava/lang/Object;");
        jmethodID ArrayList_size = env->GetMethodID(ArrayList_class, "size", "()I");
        LOGD("--Entering clear phase for--");
        int size = env->CallIntMethod(handleList, ArrayList_size);
        for(int i = 0; i < size; i++){
            LOGD("-Mat %d-", i);
            jobject handle = env->CallObjectMethod(handleList, ArrayList_get, i);
            LOGD("handle acquired");
            freeMatData(env, handle);
            LOGD("deallocation success");
        }
}

JNIEXPORT void JNICALL Java_com_ciux031701_kandidat360degrees_imageprocessing_JniMatHolder_jniFreeMatData(JNIEnv * env, jobject obj, jobject handle){
    freeMatData(env, handle);
}

void freeMatData(JNIEnv * env, jobject handle){
        JniMat * jniMat = (JniMat*)env->GetDirectBufferAddress(handle);
        if(jniMat->data == NULL)
            return;
        delete[] jniMat->data;
        delete jniMat;
}