#include <jni.h>
#include <opencv2/opencv.hpp>
#include "signature.hpp"
#include <string>
#include <vector>
#include <map>
#include <sstream>
#include <iomanip>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_shapesignatureapp_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    std::string hello = "Hello from C++ with OpenCV";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_shapesignatureapp_MainActivity_classifyShape(JNIEnv* env, jobject thiz, jlong matAddr, jobjectArray classes, jobjectArray signatures) {
    cv::Mat& img = *(cv::Mat*)matAddr;

    ShapeSignature ss;
    std::vector<float> currentSig = ss.getFourierDescriptor(img);

    if (currentSig.empty()) {
        return env->NewStringUTF("Error: No signature|0|0|");
    }

    std::map<std::string, std::vector<float>> corpus;

    int count = env->GetArrayLength(classes);
    for (int i = 0; i < count; i++) {
        jstring classNameStr = (jstring)env->GetObjectArrayElement(classes, i);
        const char* classNameChars = env->GetStringUTFChars(classNameStr, nullptr);
        std::string className(classNameChars);

        jfloatArray sigArray = (jfloatArray)env->GetObjectArrayElement(signatures, i);
        float* sigElements = env->GetFloatArrayElements(sigArray, nullptr);
        int sigLen = env->GetArrayLength(sigArray);

        std::vector<float> sigVec(sigElements, sigElements + sigLen);
        corpus[className] = sigVec;

        env->ReleaseFloatArrayElements(sigArray, sigElements, 0);
        env->ReleaseStringUTFChars(classNameStr, classNameChars);
    }

    ClassificationResult result = ss.classify(currentSig, corpus);

    // Formato: "Nombre|Accuracy|Distancia|Descriptor"
    std::stringstream ss_out;
    ss_out << result.className << "|"
           << std::fixed << std::setprecision(2) << result.accuracy << "|"
           << std::fixed << std::setprecision(4) << result.distance << "|";

    for (size_t i = 0; i < result.signature.size(); ++i) {
        ss_out << result.signature[i] << (i == result.signature.size() - 1 ? "" : ",");
    }

    return env->NewStringUTF(ss_out.str().c_str());
}
