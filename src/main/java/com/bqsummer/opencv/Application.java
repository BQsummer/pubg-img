package com.bqsummer.opencv;

import com.bqsummer.weapon.ShootMoving;
import com.bqsummer.weapon.ShootPoint;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.*;

public class Application {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) throws Exception {
        Map<String, List<File>> weaponImgs = readFile();
        for (Map.Entry<String, List<File>> entry : weaponImgs.entrySet()) {
            String weaponName = entry.getKey();
            List<File> imgNames = entry.getValue();
            List<List<ShootMoving>> shootMovingList = new ArrayList<>();
            for (File fileName : imgNames) {
                Mat img = Imgcodecs.imread("ori\\" + fileName.getName());
                List<Point> points = Img.getShootPoint(img, fileName.getName());
                List<ShootMoving> movingList = calcuMoving(points);
                shootMovingList.add(movingList);
            }
            System.out.println(shootMovingList);
            System.out.print("gun." + weaponName + ".recoil=");
            List<Double> verticalAverage = calculateVerticalAverage(shootMovingList);
            //System.out.println(verticalAverage);
            for (Double doubleNum : verticalAverage) {
                System.out.print(String.format("%.2f", doubleNum) + ", ");
            }
            System.out.println(" ");
        }
    }

    public static List<Double> calculateVerticalAverage(List<List<ShootMoving>> shootMovingList) {
        int maxLength = shootMovingList.stream().max(Comparator.comparingInt(List::size)).map(List::size).get();
        List<Double> averageList = new ArrayList<>();
        List<Integer> incorrectArr = new ArrayList<>();
        for (int i = 0; i < maxLength; i++) {
            List<Double> shootMoving = new ArrayList<>();
            for (List<ShootMoving> list : shootMovingList) {
                if (i < list.size()) {
                    shootMoving.add(list.get(i).getY());
                }
            }
            incorrectArr.addAll(calculateIncorrect(shootMoving));
        }
        System.out.println("incorrect arr size = " + incorrectArr.size());
        for (int i = 0; i < maxLength; i++) {
            List<Double> shootMoving = new ArrayList<>();
            for (List<ShootMoving> list : shootMovingList) {
                if (i < list.size()) {
                    shootMoving.add(list.get(i).getY());
                }
            }
            Double average = calculateAverage(shootMoving, incorrectArr);
            averageList.add(average);
        }
        return averageList;
    }

    public static Double calculateAverage(List<Double> data, List<Integer> incorrectArr) {
        Collections.sort(data);
        int counter = 0;
        double total = 0;
        for(int i = 0; i < data.size(); i++) {
            double num = data.get(i);
            if (!incorrectArr.contains(i)) {
                total += num;
                counter++;
            }
        }
        return total/counter;
    }

    public static List<Integer> calculateIncorrect(List<Double> data) {
        List<Integer> incorrectArr = new ArrayList<>();
        Collections.sort(data);
        double median = data.get(data.size()/2);
        int counter = 0;
        for(int i = 0; i < data.size(); i++) {
            double num = data.get(i);
            if (Math.abs(num - median) > median/3) {
                incorrectArr.add(i);
                counter++;
                //System.out.println("incorrect value = " + num + ", it = " + it);
            }
        }
        if (counter > data.size() / 5) {
            //System.out.println("data discrete, available data size = " + counter);
            //System.exit(1);
        }
        return incorrectArr;
    }

    public static List<ShootMoving> calcuMoving(List<Point> points) {
        List<ShootPoint> shootPointList = new ArrayList<>();
        for (Point point : points) {
            shootPointList.add(new ShootPoint(point));
        }
        Collections.sort(shootPointList);
        List<ShootMoving> movingList = new ArrayList<>();
        for (int i = 1; i < shootPointList.size(); i++) {
            movingList.add(new ShootMoving(shootPointList.get(i).getX() - shootPointList.get(i - 1).getX(), shootPointList.get(i).getY() - shootPointList.get(i - 1).getY()));
        }
        Collections.reverse(movingList);
        return movingList;
    }

    public static Map<String, List<File>> readFile() {
        Map<String, List<File>> weaponMap = new HashMap<>();
        File director = new File("ori");
        File[] tempList = director.listFiles();
        if (tempList != null && tempList.length > 0) {
            for (File file : tempList) {
                if (file.isFile() && file.getAbsoluteFile().getName().contains(".") && file.getAbsoluteFile().getName().split("\\.").length > 1) {
                    String[] attrs = file.getAbsoluteFile().getName().split("\\.");
                    List<File> fileList = weaponMap.get(attrs[0]);
                    if (fileList == null) {
                        fileList = new ArrayList<>();
                        fileList.add(file);
                        weaponMap.put(attrs[0], fileList);
                    } else {
                        fileList.add(file);
                    }
                }
            }
        }
        return weaponMap;
    }
}
