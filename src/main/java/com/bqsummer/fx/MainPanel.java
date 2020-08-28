package com.bqsummer.fx;

import com.bqsummer.opencv.Img;
import javafx.fxml.FXML;
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

import java.util.List;

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
    private Slider alphaSlider;
    @FXML
    private Slider betaSlider;
    @FXML
    private Slider ksizeSlider;
    @FXML
    private Slider threshSlider;
    @FXML
    private Slider varianceSlider;
//    @FXML
//    private Slider threshold1Slider;
//    @FXML
//    private Slider threshold2Slider;
    @FXML
    private Slider apertureSizeSlider;
    @FXML
    private Slider maxAreaSlider;
    @FXML
    private Slider minAreaSlider;

    @FXML
    private Text alphaLabel;
    @FXML
    private Text betaLabel;
    @FXML
    private Text ksizeLabel;
    @FXML
    private Text threshLabel;
//    @FXML
//    private Text threshold1Label;
//    @FXML
//    private Text threshold2Label;
    @FXML
    private Text apertureSizeLabel;
    @FXML
    private Text maxAreaLabel;
    @FXML
    private Text minAreaLabel;
    @FXML
    private Text varianceLabel;

    private Mat img;

    public void initImg() {
        img = Imgcodecs.imread("ori\\m4.3.png");
        img = Img.cvtColor(cutImg(img, 0.65f, 0.8f));
    }

    public Mat cutImg(Mat img, float minWidthPercentage, float maxWidthPercentage) {
        Mat mat = img.submat(0, img.height()-100, (int) (minWidthPercentage * img.width()),(int) (maxWidthPercentage * img.width()));
        return mat;
    }


    public void initValue() {
        alphaLabel.setText(String.valueOf(alphaSlider.getValue()));
        betaLabel.setText(String.valueOf(betaSlider.getValue()));
        ksizeLabel.setText(String.valueOf(ksizeSlider.getValue()));
        threshLabel.setText(String.valueOf(threshSlider.getValue()));
//        threshold1Label.setText(String.valueOf(threshold1Slider.getValue()));
//        threshold2Label.setText(String.valueOf(threshold2Slider.getValue()));
        apertureSizeLabel.setText(String.valueOf(apertureSizeSlider.getValue()));
        maxAreaLabel.setText(String.valueOf(maxAreaSlider.getValue()));
        minAreaLabel.setText(String.valueOf(minAreaSlider.getValue()));
        varianceLabel.setText(String.valueOf(varianceSlider.getValue()));
    }

    public void initBind() {
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
//        threshold1Slider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            threshold1Label.setText(String.valueOf(threshold1Slider.getValue()));
//            refreshAllImage();
//        });
//        threshold2Slider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            threshold2Label.setText(String.valueOf(threshold2Slider.getValue()));
//            refreshAllImage();
//        });
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
        try {
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
            List<Point> line = Img.filterContours(img.size(), points, 70, 70, 1.5);
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
