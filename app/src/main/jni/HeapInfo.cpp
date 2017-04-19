//
// Created by Neso on 2017-04-19.
//

#include "HeapInfo.h"
long getNativeHeapAllocatedSize(JNIEnv *env)
{
    jclass clazz = env->FindClass("android/os/Debug");
    if (clazz)
    {
        jmethodID mid = env->GetStaticMethodID(clazz, "getNativeHeapAllocatedSize", "()J");
        if (mid)
        {
            return env->CallStaticLongMethod(clazz, mid);
        }
    }
    return -1L;
}

long getNativeHeapSize(JNIEnv *env)
{
    jclass clazz = env->FindClass("android/os/Debug");
    if (clazz)
    {
        jmethodID mid = env->GetStaticMethodID(clazz, "getNativeHeapSize", "()J");
        if (mid)
        {
            return env->CallStaticLongMethod(clazz, mid);
        }
    }
    return -1L;
}