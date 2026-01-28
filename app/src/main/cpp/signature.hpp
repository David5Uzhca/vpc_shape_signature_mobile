#ifndef SIGNATURE_HPP
#define SIGNATURE_HPP

#include <opencv2/opencv.hpp>
#include <vector>
#include <string>
#include <map>

struct ClassificationResult {
    std::string className;
    float distance;
    float accuracy;
    std::vector<float> signature;
};

class ShapeSignature {
public:
    std::vector<float> getFourierDescriptor(const cv::Mat& inputImage);

    ClassificationResult classify(const std::vector<float>& currentSig,
                                 const std::map<std::string, std::vector<float>>& corpus);

private:
    void resampleContour(const std::vector<cv::Point>& contour, std::vector<cv::Point2f>& resampled, int n);
    std::vector<float> normalizeDescriptors(cv::Mat& coeffs);
    float euclideanDistance(const std::vector<float>& a, const std::vector<float>& b);
};

#endif // SIGNATURE_HPP
