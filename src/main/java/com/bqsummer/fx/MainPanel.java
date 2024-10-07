package com.bqsummer.fx;

import com.bqsummer.opencv.Img;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainPanel {

    @FXML
    private ImageView normalizeImg;
    @FXML
    private ImageView medianBlurImg;
    @FXML
    private ImageView thresholdImg;
    @FXML
    private ImageView cannyImg;
    @FXML
    private ImageView filteredImg;
    @FXML
    private ImageView finalImg;

    @FXML
    private Button next;
    @FXML
    private Button pre;
    @FXML
    private Button print;
    @FXML
    private Slider xPosition;
    @FXML
    private Slider alphaSlider;
    @FXML
    private Slider betaSlider;
    @FXML
    private Slider ksizeSlider;
    @FXML
    private Slider threshSlider;
    @FXML
    private Slider varianceSlider;
    @FXML
    private Slider apertureSizeSlider;
    @FXML
    private Slider maxAreaSlider;
    @FXML
    private Slider minAreaSlider;

    @FXML
    private Text imageName;
    @FXML
    private Text xPositionLabel;
    @FXML
    private Text alphaLabel;
    @FXML
    private Text betaLabel;
    @FXML
    private Text ksizeLabel;
    @FXML
    private Text threshLabel;
    @FXML
    private Text apertureSizeLabel;
    @FXML
    private Text maxAreaLabel;
    @FXML
    private Text minAreaLabel;
    @FXML
    private Text varianceLabel;

    private Mat _img;

    private AtomicInteger imageIndex = new AtomicInteger(0);

    private List<Point> finalLine = null;

    public void initImg() {
        _img = Imgcodecs.imread("ori\\" + getImageName());
    }

    public Mat cutImg(Mat img, Double minWidthPercentage, float maxWidthPercentage) {
        if (maxWidthPercentage > 1) {
            maxWidthPercentage = 1;
        }
        return img.submat(0, img.height()-100, (int) (minWidthPercentage * img.width()) + 1, (int) (maxWidthPercentage * img.width()));
    }

    private String getImageName() {
        List<String> files = getAllImageName();
        if (imageIndex.get() < files.size()) {
            return files.get(imageIndex.get());
        }
        return files.get(0);
    }

    private List<String> getAllImageName() {
        List<String> files = new ArrayList<>();
        File folder = new File("ori");
        File[] listOfFiles = folder.listFiles();
        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files.add(listOfFiles[i].getName());
                }
            }
        }
        return files;
    }

    private void addIndex() {
        List<String> files = getAllImageName();
        if (imageIndex.get() < files.size() - 1) {
            imageIndex.addAndGet(1);
            imageName.setText(getImageName());
            _img = Imgcodecs.imread("ori\\" + getImageName());
            refreshAllImage();
        }
    }

    private void minusIndex() {
        if (imageIndex.get() == 0) {
            return;
        }
        imageIndex.addAndGet(-1);
        imageName.setText(getImageName());
        _img = Imgcodecs.imread("ori\\" + getImageName());
        refreshAllImage();
    }

    private void printLineY() {
        List<Point> sorted = finalLine.stream().sorted(Comparator.comparing(e -> e.y)).collect(Collectors.toList());
        for (int i = sorted.size() - 1; i > 0; i--) {
            System.out.printf("%.1f%n", Math.abs(sorted.get(i - 1).y - sorted.get(i).y));
        }
    }


    public void initValue() {
        imageName.setText(getImageName());
        xPositionLabel.setText(String.valueOf(xPosition.getValue()));
        alphaLabel.setText(String.valueOf(alphaSlider.getValue()));
        betaLabel.setText(String.valueOf(betaSlider.getValue()));
        ksizeLabel.setText(String.valueOf(ksizeSlider.getValue()));
        threshLabel.setText(String.valueOf(threshSlider.getValue()));
        apertureSizeLabel.setText(String.valueOf(apertureSizeSlider.getValue()));
        maxAreaLabel.setText(String.valueOf(maxAreaSlider.getValue()));
        minAreaLabel.setText(String.valueOf(minAreaSlider.getValue()));
        varianceLabel.setText(String.valueOf(varianceSlider.getValue()));
    }

    public void initBind() {
        next.setOnAction(event -> {
            addIndex();
        });
        pre.setOnAction(event -> {
            minusIndex();
        });
        print.setOnAction(event -> {
            printLineY();
        });
        xPosition.valueProperty().addListener((observable, oldValue, newValue) -> {
            xPositionLabel.setText(String.valueOf(xPosition.getValue()));
            refreshAllImage();
        });
        alphaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            alphaLabel.setText(String.valueOf(alphaSlider.getValue()));
            refreshAllImage();
        });
        betaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            betaLabel.setText(String.valueOf(betaSlider.getValue()));
            refreshAllImage();
        });
        ksizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ksizeLabel.setText(String.valueOf(ksizeSlider.getValue()));
            refreshAllImage();
        });
        threshSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            threshLabel.setText(String.valueOf(threshSlider.getValue()));
            refreshAllImage();
        });
        apertureSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            apertureSizeLabel.setText(String.valueOf(apertureSizeSlider.getValue()));
            refreshAllImage();
        });
        maxAreaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            maxAreaLabel.setText(String.valueOf(maxAreaSlider.getValue()));
            refreshAllImage();
        });
        minAreaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            minAreaLabel.setText(String.valueOf(minAreaSlider.getValue()));
            refreshAllImage();
        });
        varianceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            varianceLabel.setText(String.valueOf(varianceSlider.getValue()));
            refreshAllImage();
        });
    }

    public void refreshAllImage() {
        refreshAllImage(_img.clone());
    }

    public void refreshAllImage(Mat img) {
        try {
            img = Img.cvtColor(cutImg(img, xPosition.getValue(), (float) (xPosition.getValue() + 0.2)));
            img = Img.normalize(img, Double.valueOf(alphaSlider.getValue()).intValue(), Double.valueOf(betaSlider.getValue()).intValue());
            setNormalizeImg(Img.matToImage(img));
            Mat medianBlurImg = Img.erode(img, (int) ksizeSlider.getValue());
            setMedianBlurImg(Img.matToImage(medianBlurImg));
            Mat thresholdImg = Img.threshold(medianBlurImg, (int) threshSlider.getValue(), 255);
            setThresholdImg(Img.matToImage(thresholdImg));
            Mat cannyImg = Img.canny(thresholdImg, 255, 255, (int) apertureSizeSlider.getValue());
            setCannyImg(Img.matToImage(cannyImg));
            List<Point> points = Img.findContours(cannyImg, maxAreaSlider.getValue(), minAreaSlider.getValue(), varianceSlider.getValue());
            Mat filteredImg = Mat.zeros(img.size(), CvType.CV_8UC3);
            for(Point point : points) {
                Scalar color = new Scalar(255, 255, 0);
                Imgproc.circle(filteredImg, point, 4, color, -1);
            }
            setFilteredImg(Img.matToImage(filteredImg));
            List<Point> line = Img.filterContours(points, 70, 90, 1.2);
            finalLine = line;
            if(line == null) {
                return;
            }
            Mat drawing = Mat.zeros(img.size(), CvType.CV_8UC3);
            for(Point point : line) {
                Scalar color = new Scalar(255, 255, 0);
                Imgproc.circle(drawing, point, 4, color, -1);
            }
            setFinalImg(Img.matToImage(drawing));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNormalizeImg(Image value) {
        normalizeImg.setImage(value);
    }

    public void setMedianBlurImg(Image value) {
        medianBlurImg.setImage(value);
    }

    public void setThresholdImg(Image value) {
        thresholdImg.setImage(value);
    }

    public void setCannyImg(Image value) {
        cannyImg.setImage(value);
    }

    public void setFilteredImg(Image value) {
        filteredImg.setImage(value);
    }

    public void setFinalImg(Image value) {
        finalImg.setImage(value);
    }

}
