package com.bqsummer.opencv;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Img {

    public static Image matToImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);
        return SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(mob.toArray())), null);
    }

    /**
     * 灰度
     * @param img
     * @return
     */
    public static Mat cvtColor(Mat img) {
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        return img;
    }

    public static Mat normalize(Mat img, int alpha, int beta) {
        Mat effected = img.clone();
        Core.normalize(img, effected, alpha, beta, Core.NORM_MINMAX);
        return effected;
    }

    public static Mat medianBlur(Mat img, int ksize) {
        Mat effected = img.clone();
        Imgproc.medianBlur(img, effected,ksize);
        return effected;
    }

    public static Mat erode(Mat img, int erosionSize) {
        Mat effected = img.clone();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2 * erosionSize + 1, 2 * erosionSize + 1));
        Imgproc.erode(img, effected, element);
        return effected;
    }

    public static Mat threshold(Mat img, int thresh, int maxval) {
        Mat effected = img.clone();
        Imgproc.threshold(img, effected, thresh, maxval, Imgproc.THRESH_BINARY);
        return effected;
    }

    public static Mat canny(Mat img, int threshold1, int threshold2, int apertureSize) {
        Mat effected = img.clone();
        Imgproc.Canny(img, effected, threshold1, threshold2, apertureSize);
        return effected;
    }

    public static List<Point> findContours(Mat img, double maxArea, double minArea, double variance) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        List<Moments> mu = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            mu.add(Imgproc.moments(contours.get(i), true));
        }
        // 轮廓中心
        List<Point> mc = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            mc.add(new Point(mu.get(i).m10 / mu.get(i).m00, mu.get(i).m01 / mu.get(i).m00));
        }

        // 过滤轮廓
        List<Point> filtered = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            if(Imgproc.contourArea(contours.get(i)) < maxArea
                    && Imgproc.contourArea(contours.get(i)) > minArea    // 面积
                    //&& img.get((int)mc.get(i).y, (int)mc.get(i).x)[0] == 0.0   // 黑色
                    //&&  mc.get(i).x > img.cols() * minWidthPercentage && mc.get(i).x < (img.cols() * maxWidthPercentage)   // 中间三分之一
                    && isCircle(contours.get(i), mc.get(i), variance)  // 圆形
            ) {
                filtered.add(mc.get(i));
            }
        }
        return filtered;


    }

    public static Mat drawContours(Size size, List<Point> filtered) {
        Mat filteredImg = Mat.zeros(size, CvType.CV_8UC3);
        for(Point point : filtered) {
            Scalar color = new Scalar(255, 255, 0);
            Imgproc.circle(filteredImg, point, 4, color, -1);
        }
        return filteredImg;
    }

    public static List<Point> filterContours(Size size, List<Point> filtered, double absXDelta, double absYDelta, double slope) {
        Map<Point, Integer> values = new HashMap<>();
        // 向上连续点的集合
        Map<Point, List<Point>> lines = new HashMap<>();
        for(int i = 0; i < filtered.size(); i++) {
            int value = 0;
            Point next = filtered.get(i);
            List<Point> line = new ArrayList<>();
            line.add(filtered.get(i));
            Boolean isFind = null;
            while(isFind == null || isFind) {
                isFind = false;
                for(int j = 0; j < filtered.size(); j++) {
                    // 向上寻找相近点
                    if(filtered.get(j).y < next.y && Math.abs(next.y- filtered.get(j).y) < absXDelta &&  Math.abs(next.x- filtered.get(j).x) < absYDelta && (Math.abs(next.y- filtered.get(j).y)/Math.abs(next.x- filtered.get(j).x) > slope)) {
                        value ++;
                        next = filtered.get(j);
                        isFind = true;
                        line.add(filtered.get(j));
                        break;
                    }
                }
            }
            values.put(filtered.get(i), value);
            lines.put(filtered.get(i), line);
        }

        // 最大连续点的集合
        int max = 0;
        Point maxPoint = null;
        for(Map.Entry<Point, Integer> entry : values.entrySet()) {
            if(entry.getValue() > max) {
                max = entry.getValue();
                maxPoint = entry.getKey();
            }
        }
        return lines.get(maxPoint);


    }

    public static List<Point> getShootPoint(Mat mat, String fileName) {
        mat = mat.submat(0, mat.height()-80, (int) (0.65f * mat.width()),(int) (0.8f * mat.width()));
        mat = Img.cvtColor(mat);
        mat = Img.normalize(mat, 0, 250);
        //Imgcodecs.imwrite("dist\\picb-normalize-" + fileName + ".PNG", img);
        Mat medianBlurImg = Img.erode(mat, 3);
        //Imgcodecs.imwrite("dist\\picb-medianBlur-" + fileName + ".PNG", medianBlurImg);
        Mat thresholdImg = Img.threshold(medianBlurImg, 47, 255);
        //Imgcodecs.imwrite("dist\\picb-threshold-" + fileName + ".PNG", thresholdImg);
        Mat cannyImg = Img.canny(thresholdImg, 255, 255, 7);
        //Imgcodecs.imwrite("dist\\picb-canny-" + fileName + ".PNG", cannyImg);
        List<Point> points = Img.findContours(cannyImg, 100, 1, 1);
        for(Point point : points) {
            Scalar color = new Scalar(255, 255, 0);
            Imgproc.circle(cannyImg, point, 4, color, -1);
        }
        List<Point> filterContours = Img.filterContours(mat.size(), points, 70, 70, 1.5);
        Mat line = drawContours(mat.size(), filterContours);
        Imgcodecs.imwrite("dist\\picb-final-" + fileName + ".PNG", line);
        return filterContours;
    }

    /**
     * 通过边缘到中心距离的平方差判断轮廓是否是圆
     * @param contour
     * @param center
     * @return
     */
    public static boolean isCircle(MatOfPoint contour, Point center, double variance) {
        double[] dis = new double[contour.toList().size()];
        for(int i = 0; i < contour.toList().size(); i++) {
            Point point = contour.toList().get(i);
            dis[i] = Math.sqrt(Math.pow(center.x - point.x, 2) + Math.pow(center.y - point.y, 2));
        }
        return variance(dis) < variance;
    }

    /**
     * 平方差
     * @param x
     * @return
     */
    public static double variance(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        return dVar/m;
    }

    public static double avg(List<Double> data) {
        if(data == null || data.size() ==0) {
            return 0;
        }
        double sum = 0D;
        for(Double a : data) {
            sum += a;
        }
        return sum/data.size();
    }
}
