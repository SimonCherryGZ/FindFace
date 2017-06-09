#include <stdio.h>
#include <jni.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <dirent.h>
#include <opencv2/opencv.hpp>
#include<android/log.h>
/* Header for class com_simoncherry_jnidemo_util_JNIUtils */
#include "jni_app.h"

#define LOG    "FindFace-jni" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__) // 定义LOGF类型

#ifdef __cplusplus
extern "C" {
#endif

using namespace cv;
using namespace std;


CascadeClassifier *g_CascadeClassifier;

IplImage * change4channelTo3InIplImage(IplImage * src);

IplImage * change4channelTo3InIplImage(IplImage * src) {
    if (src->nChannels != 4) {
        return NULL;
    }

    IplImage * destImg = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 3);
    for (int row = 0; row < src->height; row++) {
        for (int col = 0; col < src->width; col++) {
            CvScalar s = cvGet2D(src, row, col);
            cvSet2D(destImg, row, col, s);
        }
    }

    return destImg;
}

IplImage * changeFuckIplImage(IplImage * src) {
    IplImage * destImg = cvCreateImage(cvGetSize(src), IPL_DEPTH_8U, 3);
    for (int row = 0; row < src->height; row++) {
        for (int col = 0; col < src->width; col++) {
            CvScalar s = cvGet2D(src, row, col);
            cvSet2D(destImg, row, col, s);
        }
    }

    return destImg;
}

/*
 * Class:     com_simoncherry_findface_util_JNIUtils
 * Method:    doGrayScale 图像灰度化
 * Signature: ([III)[I
 */
JNIEXPORT jintArray JNICALL Java_com_simoncherry_findface_util_JNIUtils_doGrayScale
        (JNIEnv *env, jclass obj, jintArray buf, jint w, jint h) {
    LOGE("doGrayScale Start");

    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, JNI_FALSE);
    if (cbuf == NULL) {
        return 0;
    }

    cv::Mat imgData(h, w, CV_8UC4, (unsigned char *) cbuf);

    uchar* ptr = imgData.ptr(0);
    for(int i = 0; i < w*h; i ++){
        //计算公式：Y(亮度) = 0.299*R + 0.587*G + 0.114*B
        //对于一个int四字节，其彩色值存储方式为：BGRA
        int grayScale = (int)(ptr[4*i+2]*0.299 + ptr[4*i+1]*0.587 + ptr[4*i+0]*0.114);
        ptr[4*i+1] = grayScale;
        ptr[4*i+2] = grayScale;
        ptr[4*i+0] = grayScale;
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, cbuf);
    env->ReleaseIntArrayElements(buf, cbuf, 0);

    LOGE("doGrayScale End");
    return result;
}

/*
 * Class:     com_simoncherry_findface_util_JNIUtils
 * Method:    doEdgeDetection 图像边缘检测
 * Signature: ([III)[I
 */
JNIEXPORT jintArray JNICALL Java_com_simoncherry_findface_util_JNIUtils_doEdgeDetection
        (JNIEnv *env, jclass obj, jintArray buf, jint w, jint h) {
    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, JNI_FALSE);
    if (cbuf == NULL) {
        return 0;
    }

    cv::Mat myimg(h, w, CV_8UC4, (unsigned char*) cbuf);
    IplImage image = IplImage(myimg);
    IplImage* image3channel = change4channelTo3InIplImage(&image);

    IplImage* pCannyImage = cvCreateImage(cvGetSize(image3channel),IPL_DEPTH_8U,1);
    cvCanny(image3channel, pCannyImage, 50, 150, 3);

    int* outImage = new int[w*h];
    for(int i=0;i<w*h;i++) {
        outImage[i]=(int)pCannyImage->imageData[i];
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, outImage);
    env->ReleaseIntArrayElements(buf, cbuf, 0);
    return result;
}

/*
 * Class:     com_simoncherry_findface_util_JNIUtils
 * Method:    doBinaryzation 图像二值化
 * Signature: ([III)[I
 */
JNIEXPORT jintArray JNICALL Java_com_simoncherry_findface_util_JNIUtils_doBinaryzation
        (JNIEnv *env, jclass obj, jintArray buf, jint w, jint h) {
    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, JNI_FALSE);
    if (cbuf == NULL) {
        return 0;
    }

    cv::Mat imgData(h, w, CV_8UC4, (unsigned char *) cbuf);
    IplImage image = IplImage(imgData);
    IplImage *pSrcImage = change4channelTo3InIplImage(&image);
    IplImage *g_pGrayImage = cvCreateImage(cvGetSize(pSrcImage), IPL_DEPTH_8U, 1);
    cvCvtColor(pSrcImage, g_pGrayImage, CV_BGR2GRAY);

    IplImage *g_pBinaryImage = cvCreateImage(cvGetSize(g_pGrayImage), IPL_DEPTH_8U, 1);
    cvThreshold(g_pGrayImage, g_pBinaryImage, 127, 255, CV_THRESH_BINARY);

    int* outImage = new int[w * h];
    for(int i=0;i<w*h;i++) {
        outImage[i]=(int)g_pBinaryImage->imageData[i];
    }

    int size = w * h;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, outImage);
    env->ReleaseIntArrayElements(buf, cbuf, 0);
    return result;
}

jstring str2jstring(JNIEnv* env, const char* pat) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("Ljava/lang/String;");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("GB2312");
    //将byte数组转换为java String,并输出
    return (jstring)(env)->NewObject(strClass, ctorID, bytes, encoding);
}

std::string jstring2str(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr,mid,strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr,JNI_FALSE);
    if(alen > 0) {
        rtn = (char*)malloc(alen+1);
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }
    env->ReleaseByteArrayElements(barr,ba,0);
    std::string stemp(rtn);
    free(rtn);
    return stemp;
}

const char* string2printf(JNIEnv *env, std::string str) {
    jstring jstr = env->NewStringUTF(str.c_str());
    return env->GetStringUTFChars(jstr, NULL);
}

/*
 * Class:     com_simoncherry_findface_util_JNIUtils
 * Method:    setClassifier
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_simoncherry_findface_util_JNIUtils_setClassifier
  (JNIEnv *env, jobject instance, jstring path_) {
        const char *path = env->GetStringUTFChars(path_, NULL);
        g_CascadeClassifier = new CascadeClassifier(path);
        long ptr = reinterpret_cast<long>(g_CascadeClassifier);
        env->ReleaseStringUTFChars(path_, path);
        LOGE("cascadeClassifier initialized");
        return ptr;
  }

/*
 * Class:     com_simoncherry_findface_util_JNIUtils
 * Method:    deleteCassifier
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_simoncherry_findface_util_JNIUtils_deleteCassifier
  (JNIEnv *env, jobject instance, jlong cptr) {
        CascadeClassifier *cascadeClassifier = reinterpret_cast<cv::CascadeClassifier *>(cptr);
        delete cascadeClassifier;
        return;
  }

/*
 * Class:     com_simoncherry_findface_util_JNIUtils
 * Method:    detectFace
 * Signature: ([III)[Lcom/simoncherry/findface/model/CVFace;
 */
JNIEXPORT jobjectArray JNICALL Java_com_simoncherry_findface_util_JNIUtils_detectFace
  (JNIEnv *env, jclass obj, jintArray buf, jint w, jint h) {
    LOGE("detectFace Start");

    if (!g_CascadeClassifier) {
            jclass je = env->FindClass("java/lang/Exception");
            env->ThrowNew(je, "CascadeClassifier not initialized!");
    }

    jint *cbuf;
    cbuf = env->GetIntArrayElements(buf, JNI_FALSE);
    if (cbuf == NULL) {
      return 0;
    }

    cv::Mat srcImage(h, w, CV_8UC4, (unsigned char *) cbuf);
    cvtColor(srcImage, srcImage, CV_BGR2GRAY); // 转为灰度图像

    //直方图均值化
    equalizeHist(srcImage, srcImage);
    vector<Rect> vectors;
    g_CascadeClassifier->detectMultiScale(srcImage, vectors);
    LOGE("detected.");
    jclass objectClass = env->FindClass("com/simoncherry/findface/model/CVFace");
    // 返回的人脸数组
    jobjectArray result = env->NewObjectArray(vectors.size(), objectClass, NULL);
    jmethodID cId = env->GetMethodID(objectClass, "<init>", "()V");
    jfieldID xId = env->GetFieldID(objectClass, "x", "I");
    jfieldID yId = env->GetFieldID(objectClass, "y", "I");
    jfieldID widthId = env->GetFieldID(objectClass, "width", "I");
    jfieldID heightId = env->GetFieldID(objectClass, "height", "I");
    for (int i = 0; i < vectors.size(); i++) {
        jobject obj = env->NewObject(objectClass, cId);
        env->SetIntField(obj, xId, vectors[i].x);
        env->SetIntField(obj, yId, vectors[i].y);
        env->SetIntField(obj, widthId, vectors[i].width);
        env->SetIntField(obj, heightId, vectors[i].height);
        env->SetObjectArrayElement(result, i, obj);
    }
    env->ReleaseIntArrayElements(buf, cbuf, 0);
    return result;
}

#ifdef __cplusplus
}
#endif
