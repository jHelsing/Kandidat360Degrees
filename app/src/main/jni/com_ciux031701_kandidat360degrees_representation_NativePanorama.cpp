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

//For stitching detailed:
#include <iostream>
#include <fstream>
#include <string>
#include "opencv2/opencv_modules.hpp"
#include <opencv2/core/utility.hpp>
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/stitching/detail/autocalib.hpp"
#include "opencv2/stitching/detail/blenders.hpp"
#include "opencv2/stitching/detail/timelapsers.hpp"
#include "opencv2/stitching/detail/camera.hpp"
#include "opencv2/stitching/detail/exposure_compensate.hpp"
#include "opencv2/stitching/detail/matchers.hpp"
#include "opencv2/stitching/detail/motion_estimators.hpp"
#include "opencv2/stitching/detail/seam_finders.hpp"
#include "opencv2/stitching/detail/warpers.hpp"
#include "opencv2/stitching/warpers.hpp"

#define ENABLE_LOG 1
#define LOG(msg) std::cout << msg
#define LOGLN(msg) std::cout << msg << std::endl

using namespace cv::detail;

using namespace std;
using namespace cv;

// Default command line args
vector<String> img_names;
bool preview = false;
bool try_cuda = false;
double work_megapix = 0.6;
double seam_megapix = 0.1;
double compose_megapix = -1;
float conf_thresh = 1.f;
string features_type = "orb";
string matcher_type = "homography";
string estimator_type = "homography";
string ba_cost_func = "ray";
string ba_refine_mask = "xxxxx";
bool do_wave_correct = true;
WaveCorrectKind wave_correct = detail::WAVE_CORRECT_HORIZ;
bool save_graph = false;
std::string save_graph_to;
string warp_type = "cylindrical";
int expos_comp_type = ExposureCompensator::GAIN_BLOCKS;
float match_conf = 0.3f;
string seam_find_type = "gc_color";
int blend_type = Blender::MULTI_BAND;
int timelapse_type = Timelapser::AS_IS;
float blend_strength = 5;
string result_name = "result.jpg";
bool timelapse = false;
int range_width = -1;

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
    LOGD("heap size: %d", (int) getNativeHeapSize(env));
    LOGD("heap allocated: %d", getNativeHeapAllocatedSize(env));
    freeMatDataList(env, handleList);
    LOGD("Old data deallocated");
    LOGD("heap size: %d", getNativeHeapSize(env));
    LOGD("heap allocated: %d", getNativeHeapAllocatedSize(env));

    LOGD(" --- Stitching detailed --- ");
    Mat result;

    if (newVector.size() < 2) {
        //Let result be empty
        LOGD("Number of images are too few: %d st", newVector.size());
    } else {

#if 0
        cv::setBreakOnError(true);
#endif

        double work_scale = 1, seam_scale = 1, compose_scale = 1;
        bool is_work_scale_set = false, is_seam_scale_set = false, is_compose_scale_set = false;

        int num_images = matVector.size();

        LOGD("Finding features...");

        Ptr<FeaturesFinder> finder = makePtr<OrbFeaturesFinder>();

        Mat full_img, img;
        vector<ImageFeatures> features(num_images);
        vector<Mat> images(num_images);
        vector<Size> full_img_sizes(num_images);
        double seam_work_aspect = 1;


        for (int i = 0; i < num_images; ++i) {
            full_img = newVector[i];
            full_img_sizes[i] = full_img.size();

            if (work_megapix < 0) {
                img = full_img;
                work_scale = 1;
                is_work_scale_set = true;
            } else {
                if (!is_work_scale_set) {
                    work_scale = min(1.0, sqrt(work_megapix * 1e6 / full_img.size().area()));
                    is_work_scale_set = true;
                }
                resize(full_img, img, Size(), work_scale, work_scale);
            }
            if (!is_seam_scale_set) {
                seam_scale = min(1.0, sqrt(seam_megapix * 1e6 / full_img.size().area()));
                seam_work_aspect = seam_scale / work_scale;
                is_seam_scale_set = true;
            }

            (*finder)(img, features[i]);
            features[i].img_idx = i;
            //LOGD("Features in image #" << i+1 << ": " << features[i].keypoints.size());

            resize(full_img, img, Size(), seam_scale, seam_scale);
            images[i] = img.clone();
        }


        finder->collectGarbage();
        full_img.release();
        img.release();

        LOGD("Pairwise matching");

        vector<MatchesInfo> pairwise_matches;
        Ptr<FeaturesMatcher> matcher = makePtr<BestOf2NearestMatcher>(try_cuda, match_conf);


        (*matcher)(features, pairwise_matches); //this is working even though it says it shouldn't...
        matcher->collectGarbage();

        LOGD("Pairwise matching done");




        // Leave only images we are sure are from the same panorama
        vector<int> indices = leaveBiggestComponent(features, pairwise_matches, conf_thresh);
        vector<Mat> img_subset;
        vector<Mat> image_vector_subset;
        vector<Size> full_img_sizes_subset;
        for (size_t i = 0; i < indices.size(); ++i) {
            image_vector_subset.push_back(newVector[indices[i]]);
            img_subset.push_back(images[indices[i]]);
            full_img_sizes_subset.push_back(full_img_sizes[indices[i]]);
        }

        images = img_subset;
        newVector = image_vector_subset;
        full_img_sizes = full_img_sizes_subset;

        // Check if we still have enough images
        num_images = static_cast<int>(images.size());
        if (num_images < 2) {
            LOGD("Too few images after subsetting, %d st", num_images);
            //return -1;
        } else {
            Ptr<Estimator> estimator = makePtr<HomographyBasedEstimator>();


            vector<CameraParams> cameras;
            if (!(*estimator)(features, pairwise_matches, cameras)) {
                LOGD("Homography estimation failed.");
                //cout << "Homography estimation failed.\n";
                //return -1;
            } else {
                LOGD("Homography estimation success.");
            }


            for (size_t i = 0; i < cameras.size(); ++i) {
                Mat R;
                cameras[i].R.convertTo(R, CV_32F);
                cameras[i].R = R;
                //LOGD("Initial camera intrinsics #" << indices[i]+1 << ":\nK:\n" << cameras[i].K() << "\nR:\n" << cameras[i].R);
            }

            Ptr<detail::BundleAdjusterBase> adjuster = makePtr<detail::BundleAdjusterRay>();

            adjuster->setConfThresh(conf_thresh);
            Mat_<uchar> refine_mask = Mat::zeros(3, 3, CV_8U);
            refine_mask(0, 0) = 1;
            refine_mask(0, 1) = 1;
            refine_mask(0, 2) = 1;
            refine_mask(1, 1) = 1;
            refine_mask(1, 2) = 1;

            adjuster->setRefinementMask(refine_mask);
            if (!(*adjuster)(features, pairwise_matches, cameras)) {
                //cout << "Camera parameters adjusting failed.\n";
                LOGD("Camera parameters adjusting failed");
                //return -1;
            } else {
                LOGD("Camera parameters adjusting success");
            }

            LOGD("Finding median focal length.");
            vector<double> focals;
            for (size_t i = 0; i < cameras.size(); ++i) {
                focals.push_back(cameras[i].focal);
            }

            sort(focals.begin(), focals.end());
            float warped_image_scale;
            if (focals.size() % 2 == 1)
                warped_image_scale = static_cast<float>(focals[focals.size() / 2]);
            else
                warped_image_scale =
                        static_cast<float>(focals[focals.size() / 2 - 1] + focals[focals.size() / 2]) * 0.5f;

            if (do_wave_correct) {
                vector<Mat> rmats;
                for (size_t i = 0; i < cameras.size(); ++i)
                    rmats.push_back(cameras[i].R.clone());
                waveCorrect(rmats, wave_correct);
                for (size_t i = 0; i < cameras.size(); ++i)
                    cameras[i].R = rmats[i];
            }

            //LOGD("Warping images (auxiliary)... ");


            vector<Point> corners(num_images);
            vector<UMat> masks_warped(num_images);
            vector<UMat> images_warped(num_images);
            vector<Size> sizes(num_images);
            vector<UMat> masks(num_images);

            // Prepare images masks
            for (int i = 0; i < num_images; ++i) {
                masks[i].create(images[i].size(), CV_8U);
                masks[i].setTo(Scalar::all(255));
            }


            // Warp images and their masks

            Ptr<WarperCreator> warper_creator = makePtr<cv::CylindricalWarper>();


            Ptr<RotationWarper> warper = warper_creator->create(
                    static_cast<float>(warped_image_scale * seam_work_aspect));

            for (int i = 0; i < num_images; ++i) {
                Mat_<float> K;
                cameras[i].K().convertTo(K, CV_32F);
                float swa = (float) seam_work_aspect;
                K(0, 0) *= swa;
                K(0, 2) *= swa;
                K(1, 1) *= swa;
                K(1, 2) *= swa;

                corners[i] = warper->warp(images[i], K, cameras[i].R, INTER_LINEAR, BORDER_REFLECT,
                                          images_warped[i]);
                sizes[i] = images_warped[i].size();

                warper->warp(masks[i], K, cameras[i].R, INTER_NEAREST, BORDER_CONSTANT,
                             masks_warped[i]);
            }

            vector<UMat> images_warped_f(num_images);
            for (int i = 0; i < num_images; ++i)
                images_warped[i].convertTo(images_warped_f[i], CV_32F);

            //LOGD("Warping images done.");


            //LOGD("Exposure compensator and seam finder.");
            Ptr<ExposureCompensator> compensator = ExposureCompensator::createDefault(
                    expos_comp_type);
            compensator->feed(corners, images_warped, masks_warped);

            Ptr<SeamFinder> seam_finder = makePtr<detail::GraphCutSeamFinder>(
                    GraphCutSeamFinderBase::COST_COLOR);

            seam_finder->find(images_warped_f, corners, masks_warped);


            // Release unused memory
            images.clear();
            images_warped.clear();
            images_warped_f.clear();
            masks.clear();

            LOGD("Compositing...");

            Mat img_warped, img_warped_s;
            Mat dilated_mask, seam_mask, mask, mask_warped;
            Ptr<Blender> blender;
            //Ptr<Timelapser> timelapser;
            double compose_work_aspect = 1;


            for (int img_idx = 0; img_idx < num_images; ++img_idx) {
                LOGD("Compositing image #%d", indices[img_idx] + 1);

                // Read image and resize it if necessary
                full_img = newVector[img_idx];
                if (!is_compose_scale_set) {
                    if (compose_megapix > 0)
                        compose_scale = min(1.0,
                                            sqrt(compose_megapix * 1e6 / full_img.size().area()));
                    is_compose_scale_set = true;

                    // Compute relative scales
                    //compose_seam_aspect = compose_scale / seam_scale;
                    compose_work_aspect = compose_scale / work_scale;

                    // Update warped image scale
                    warped_image_scale *= static_cast<float>(compose_work_aspect);
                    warper = warper_creator->create(warped_image_scale);


                    //LOGD("UPDATE CORNERS");
                    // Update corners and sizes
                    for (int i = 0; i < num_images; ++i) {
                        // Update intrinsics
                        cameras[i].focal *= compose_work_aspect;
                        cameras[i].ppx *= compose_work_aspect;
                        cameras[i].ppy *= compose_work_aspect;

                        // Update corner and size
                        Size sz = full_img_sizes[i];
                        if (std::abs(compose_scale - 1) > 1e-1) {
                            sz.width = cvRound(full_img_sizes[i].width * compose_scale);
                            sz.height = cvRound(full_img_sizes[i].height * compose_scale);
                        }

                        Mat K;
                        cameras[i].K().convertTo(K, CV_32F);
                        Rect roi = warper->warpRoi(sz, K, cameras[i].R);
                        corners[i] = roi.tl();
                        sizes[i] = roi.size();
                    }
                }
                if (abs(compose_scale - 1) > 1e-1)
                    resize(full_img, img, Size(), compose_scale, compose_scale);
                else
                    img = full_img;
                full_img.release();
                Size img_size = img.size();

                Mat K;
                cameras[img_idx].K().convertTo(K, CV_32F);

                //LOGD("WARPING");
                // Warp the current image
                warper->warp(img, K, cameras[img_idx].R, INTER_LINEAR, BORDER_REFLECT, img_warped);

                // Warp the current image mask
                mask.create(img_size, CV_8U);
                mask.setTo(Scalar::all(255));
                warper->warp(mask, K, cameras[img_idx].R, INTER_NEAREST, BORDER_CONSTANT,
                             mask_warped);

                //LOGD("COMPENSATE EXPOSURE");
                // Compensate exposure
                compensator->apply(img_idx, corners[img_idx], img_warped, mask_warped);

                img_warped.convertTo(img_warped_s, CV_16S);
                img_warped.release();
                img.release();
                mask.release();

                dilate(masks_warped[img_idx], dilated_mask, Mat());
                resize(dilated_mask, seam_mask, mask_warped.size());
                mask_warped = seam_mask & mask_warped;

                if (!blender) {
                    blender = Blender::createDefault(blend_type, try_cuda);
                    Size dst_sz = resultRoi(corners, sizes).size();
                    float blend_width =
                            sqrt(static_cast<float>(dst_sz.area())) * blend_strength / 100.f;
                    MultiBandBlender *mb = dynamic_cast<MultiBandBlender *>(blender.get());
                    mb->setNumBands(static_cast<int>(ceil(log(blend_width) / log(2.)) - 1.));
                    LOGD("Multi-band blender, number of bands: %d", mb->numBands());

                    blender->prepare(corners, sizes);
                }

                blender->feed(img_warped_s, mask_warped, corners[img_idx]);
            }


            Mat result_mask;
            blender->blend(result, result_mask);

            LOGD("Compositing done!");
            LOGD("Result mat has type %d", result.type());


            result.convertTo(result, CV_8UC3);
            LOGD("Result mat type after conversion: %d", result.type());

            //LOGD("Creating stitcher and 'stitch'");
            //Stitcher stitcher = Stitcher::createDefault(true);
            //Set parameters:
            //Warper
            //stitcher.setWarper(new cv::detail::CylindricalWarper());


            //Feature finder with ORB-algorithm
            //stitcher.setFeaturesFinder(new cv::detail::OrbFeaturesFinder());

            //Exposure compensator (should test more if this or BlockGainCompensator (default) is the best)
            //stitcher.setExposureCompensator(makePtr<detail::GainCompensator>());
            //Mat result;
            //OBS: should only use homography model for all parameters (panorama mode)
            //stitcher.stitch(newVector, result);

        }
    }

    LOGD("Stitching finished.");
    LOGD("STITCHING RESULTS:");
    LOGD("width = %d", result.cols);
    LOGD("height = %d", result.rows);
    LOGD("type = %d", result.type());

    long length = result.cols * result.rows * result.channels();
    unsigned char *data = new unsigned char[length]();
    memcpy(data, result.data, length);
    JniMat *out = new JniMat();
    out->width = result.cols;
    out->height = result.rows;
    out->type = result.type();
    out->channels = result.channels();
    out->data = data;
    return env->NewDirectByteBuffer(out, 0);
}