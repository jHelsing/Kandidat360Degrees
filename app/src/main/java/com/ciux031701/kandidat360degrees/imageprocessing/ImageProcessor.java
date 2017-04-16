package com.ciux031701.kandidat360degrees.imageprocessing;

import com.ciux031701.kandidat360degrees.ThreeSixtyWorld;
import com.ciux031701.kandidat360degrees.representation.ThreeSixtyPanorama;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.*;

import java.util.ArrayList;

/**
 * Created by Neso on 2017-04-15.
 */

public class ImageProcessor {
    public static Rect getBlackCroppedRect(Mat src) {
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
            return new Rect();
        }
        else
        {
            double x1 = (points[0].x > points[1].x) ? points[0].x : points[1].x;
            double y1 = (points[0].y > points[3].y) ? points[0].y : points[3].y;
            double x_e1 = (points[3].x > points[2].x) ? points[2].x : points[3].x;
            double y_e1 = (points[1].y > points[2].y) ? points[2].y : points[1].y;

            int x = (x1 < x_e1) ? (int)x1 : (int)x_e1;
            int x_e = (x1 < x_e1) ? (int)x_e1 : (int)x1;
            int y = (y1 < y_e1) ? (int)y1 : (int)y_e1;
            int y_e = (y1 < y_e1) ? (int)y_e1 : (int)y1;

            int width = (x_e - x);
            int height = (y_e - y);



            return new Rect(x, y, width, height);
        }
    }

    private static int maxSizeIndex(ArrayList<MatOfPoint> list){
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

    public static Mat replaceBlack(Mat mat){
        Mat copy = new Mat();
        mat.copyTo(copy);
        Mat mask = new Mat();
        Scalar lower = new Scalar(0.0, 0.0, 0.0, 0.0);
        Scalar upper = new Scalar(5.0, 5.0, 5.0, 0.0);
        Scalar c = new Scalar(6.0, 6.0, 6.0, 0.0);
        Core.inRange(copy, lower, upper, mask);
        copy.setTo(c, mask);
        return copy;
    }

    public static ArrayList<Mat> replaceBlackInList(ArrayList<Mat> list){
        ArrayList<Mat> out = new ArrayList();
        for(int i = 0; i < list.size(); i++){
            out.add(replaceBlack(list.get(i)));
        }
        return out;
    }


}
