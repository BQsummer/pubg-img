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
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 过滤连续点. 连续的定义: 相邻的点x轴和y轴的绝对值小于absXDelta和absYDelta, 且斜率大于slope
     * @param filtered
     * @param absXDelta
     * @param absYDelta
     * @param slope
     * @return
     */
    public static List<Point> filterContours(List<Point> filtered, double absXDelta, double absYDelta, double slope) {
        List<Point> sorted = filtered.stream().sorted(Comparator.comparing(e -> e.y)).collect(Collectors.toList());
        Map<Point, List<Point>> map = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            map.computeIfAbsent(sorted.get(i), k -> new ArrayList<>());
            for (int j = i + 1; j < sorted.size(); j++) {
                if (isContinuous(sorted.get(i), sorted.get(j), absXDelta, absYDelta, slope)) {
                    map.get(sorted.get(i)).add(sorted.get(j));
                }
            }
        }
        return findLongestSegment(filtered, map);

    }

    public static List<Point> findLongestSegment(List<Point> points, Map<Point, List<Point>> graph) {
        List<Point> result = new ArrayList<>();
        for (Point p : points) {
            List<Point> path = dfs(graph, p, new HashSet<>(), new ArrayList<>());
            if (path.size() > result.size()) {
                result = path;
            }
        }
        return result;
    }

    private static List<Point> dfs(Map<Point, List<Point>> graph, Point current, Set<Point> visited, List<Point> path) {
        visited.add(current);
        path.add(current);

        List<Point> longestPath = new ArrayList<>(path);
        for (Point neighbor : graph.get(current)) {
            if (!visited.contains(neighbor)) {
                List<Point> newPath = dfs(graph, neighbor, visited, new ArrayList<>(path));
                if (newPath.size() > longestPath.size()) {
                    longestPath = newPath;
                }
            }
        }

        return longestPath;
    }

    public static boolean isContinuous(Point point1, Point point2, double absXDelta, double absYDelta, double slope) {
        return Math.abs(point1.x - point2.x) < absXDelta && Math.abs(point1.y - point2.y) < absYDelta && Math.abs(point1.y - point2.y) / Math.abs(point1.x - point2.x) > slope;
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
}
