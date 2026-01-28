#include "signature.hpp"
#include <algorithm>
#include <cmath>

float ShapeSignature::euclideanDistance(const std::vector<float>& a, const std::vector<float>& b) {
    float sum = 0.0;
    for (size_t i = 0; i < a.size() && i < b.size(); ++i) {
        sum += std::pow(a[i] - b[i], 2);
    }
    return std::sqrt(sum);
}

ClassificationResult ShapeSignature::classify(const std::vector<float>& currentSig,
                                             const std::map<std::string, std::vector<float>>& corpus) {
    ClassificationResult result;
    result.signature = currentSig;

    if (currentSig.empty()) {
        result.className = "No detectado";
        result.distance = -1;
        result.accuracy = 0;
        return result;
    }

    std::string bestMatch = "Desconocido";
    float minDistance = 1e10;

    for (auto const& [className, avgSig] : corpus) {
        float dist = euclideanDistance(currentSig, avgSig);
        if (dist < minDistance) {
            minDistance = dist;
            bestMatch = className;
        }
    }

    result.className = bestMatch;
    result.distance = minDistance;

    float confidence = 1.0f / (1.0f + minDistance);
    result.accuracy = confidence * 100.0f;

    return result;
}

std::vector<float> ShapeSignature::getFourierDescriptor(const cv::Mat& inputImage) {
    cv::Mat gray;
    if (inputImage.channels() > 1) {
        cv::cvtColor(inputImage, gray, cv::COLOR_BGR2GRAY);
    } else {
        gray = inputImage;
    }

    cv::Mat binary;
    cv::adaptiveThreshold(gray, binary, 255, cv::ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY_INV, 11, 2);

    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(binary, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_NONE);

    if (contours.empty()) return {};

    auto mainContour = *std::max_element(contours.begin(), contours.end(),
                                         [](const std::vector<cv::Point>& a, const std::vector<cv::Point>& b) {
                                             return cv::contourArea(a) < cv::contourArea(b);
                                         });

    std::vector<cv::Point2f> resampled;
    resampleContour(mainContour, resampled, 128);

    cv::Moments m = cv::moments(resampled);
    if (m.m00 == 0.0) return {};

    cv::Point2f center(m.m10/m.m00, m.m01/m.m00);

    cv::Mat complexSignal(1, 128, CV_32FC2);
    for (int i = 0; i < 128; i++) {
        complexSignal.at<cv::Vec2f>(0, i)[0] = resampled[i].x - center.x;
        complexSignal.at<cv::Vec2f>(0, i)[1] = resampled[i].y - center.y;
    }

    cv::Mat fourierTransform;
    cv::dft(complexSignal, fourierTransform, cv::DFT_COMPLEX_OUTPUT);

    return normalizeDescriptors(fourierTransform);
}

void ShapeSignature::resampleContour(const std::vector<cv::Point>& contour, std::vector<cv::Point2f>& resampled, int n) {
    resampled.clear();
    resampled.reserve(n);
    if (contour.empty()) return;

    for(int i = 0; i < n; i++) {
        int idx = (i * (int)contour.size()) / n;
        if (idx >= (int)contour.size()) idx = (int)contour.size() - 1;
        resampled.push_back(cv::Point2f(contour[idx].x, (float)contour[idx].y));
    }
}

std::vector<float> ShapeSignature::normalizeDescriptors(cv::Mat& coeffs) {
    std::vector<float> magnitudes;
    magnitudes.reserve(15);
    if (coeffs.cols < 16) return {};

    for (int i = 1; i < 16; i++) {
        cv::Vec2f c = coeffs.at<cv::Vec2f>(0, i);
        magnitudes.push_back(std::sqrt(c[0]*c[0] + c[1]*c[1]));
    }

    if (!magnitudes.empty()) {
        float f1 = magnitudes[0];
        if (f1 < 1e-10) {
            if (magnitudes.size() > 1 && magnitudes[1] > 1e-10) f1 = magnitudes[1];
            else return {};
        }
        for (float &m : magnitudes) m /= f1;
    }
    return magnitudes;
}