//
// Created by Anna on 2017-03-22.
//
#include "com_ciux031701_kandidat360degrees_representation_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"

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
