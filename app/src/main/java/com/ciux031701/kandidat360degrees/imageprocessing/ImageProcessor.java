package com.ciux031701.kandidat360degrees.imageprocessing;

import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanorama;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.*;

import java.util.ArrayList;

/**
 * Created by Neso on 2017-04-15.
 */

public class ImageProcessor {
    public static Mat cropBlack(Mat src) {
        Mat gray = new Mat();
        Mat tresh = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.threshold(gray, tresh, 0, 255, Imgproc.THRESH_BINARY);
        Imgproc.findContours(tresh, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        MatOfPoint contour = contours.get(maxSizeIndex(contours));
        MatOfPoint approx = new MatOfPoint();
        MatOfPoint2f contour2f = new MatOfPoint2f();
        MatOfPoint2f approx2f = new MatOfPoint2f();
        contour.convertTo(contour2f, CvType.CV_32F);
        Imgproc.approxPolyDP(contour2f, approx2f, 0.02*Imgproc.arcLength(contour2f, true), true);
        approx2f.convertTo(approx, CvType.CV_32S);
        Point points[] = approx.toArray();

        if(points.length != 4) {
            ThreeSixtyWorld.showToast(ThreeSixtyWorld.getAppContext(), "Something went wrong during cropping.");
            return new Mat();
        }
        else
        {
            double x = (points[0].x > points[1].x) ? points[0].x : points[1].x;
            double y = (points[0].y > points[3].y) ? points[0].y : points[3].y;
            double x_e = (points[3].x > points[2].x) ? points[2].x : points[3].x;
            double y_e = (points[1].y > points[2].y) ? points[2].y : points[1].y;

            int width = (int)(x_e - x);
            int height = (int)(y_e - y);

            return src.submat((int)y, height, (int)x, width);
        }
    }

    public static int maxSizeIndex(ArrayList<MatOfPoint> list){
        int maxIndex = 0;
        for(int i = 0; i < list.size(); i++){
            MatOfPoint mop = list.get(i);
            MatOfPoint maxMop = list.get(maxIndex);
            double size = mop.size().width * mop.size().height;
            double maxSize = maxMop.size().width * maxMop.size().height;
            if((int)size > (int)maxSize)
                maxIndex = i;
        }
        return maxIndex;
    }
}
