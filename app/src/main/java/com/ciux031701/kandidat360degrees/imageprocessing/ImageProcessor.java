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
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Neso on 2017-04-15.
 */

public class ImageProcessor {
    public static Rect getBlackCroppedRect(Mat src) {
        double epsilon = 0.02;
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
        Imgproc.approxPolyDP(contour2f, approx2f, epsilon*Imgproc.arcLength(contour2f, true), true);
        while(approx2f.toArray().length != 4){
            epsilon += 0.001;
            Imgproc.approxPolyDP(contour2f, approx2f, epsilon*Imgproc.arcLength(contour2f, true), true);
            if(epsilon > 0.1)
                return null;
        }

        approx2f.convertTo(approx, CvType.CV_32S);
        Point points[] = approx.toArray();

        if(points.length != 4) {
            return null;
        }
        else
        {
            CornerCollection cs = determineCorners(points[0], points[1], points[2], points[3]);
            return calculateRect(cs);
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

    private static Rect calculateRect(CornerCollection cs){
        Corner uLeft = cs.get(Orientation.UPPER, Orientation.LEFT);
        Corner uRight = cs.get(Orientation.UPPER, Orientation.RIGHT);
        Corner lLeft = cs.get(Orientation.LOWER, Orientation.LEFT);
        Corner lRight = cs.get(Orientation.LOWER, Orientation.RIGHT);

        Rect out = new Rect();
        out.x = uLeft.x > lLeft.x ? (int)uLeft.x : (int)lLeft.x;
        out.y = uLeft.y > uRight.y ? (int)uLeft.y : (int)uRight.y;
        out.width = uRight.x < lRight.x ? (int)(uRight.x - out.x) : (int)(lRight.x - out.x);
        out.height = lLeft.y < lRight.y ? (int)(lLeft.y - out.y) : (int)(lRight.y - out.y);
        return out;
    }

    private static CornerCollection determineCorners(Point p1, Point p2, Point p3, Point p4){
        Comparator<Point> cmpX = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                Point p1 = o1;
                Point p2 = o2;
                if(p1.x < p2.x)
                    return -1;
                else if(p1.x > p2.x)
                    return 1;
                else
                    return 0;
            }
        };
        Comparator<Point> cmpY = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                Point p1 = o1;
                Point p2 = o2;
                if(p1.y < p2.y)
                    return -1;
                else if(p1.y > p2.y)
                    return 1;
                else
                    return 0;
            }
        };
        CornerCollection corners = new CornerCollection();
        corners.add(new Corner(p1));
        corners.add(new Corner(p2));
        corners.add(new Corner(p3));
        corners.add(new Corner(p4));



        Collections.sort(corners, cmpY);
        corners.get(0).vertical = Orientation.UPPER;
        corners.get(1).vertical = Orientation.UPPER;
        corners.get(2).vertical = Orientation.LOWER;
        corners.get(3).vertical = Orientation.LOWER;

        Collections.sort(corners, cmpX);
        corners.get(0).horizontal = Orientation.LEFT;
        corners.get(1).horizontal = Orientation.LEFT;
        corners.get(2).horizontal = Orientation.RIGHT;
        corners.get(3).horizontal = Orientation.RIGHT;

        return corners;
    }


}
