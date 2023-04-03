package com.example.hc05bt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase mOpenCvCameraView;
    Mat mRgba,hsvMat;
    Scalar yellow,red,lower_yellow,upper_yellow,lower_red,upper_red;
    List<Rect> yellowList, blueList;


    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{

                    mOpenCvCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.CameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,baseLoaderCallback);

        }
        else{
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        red = new Scalar(255,0,0,255);
        yellow = new Scalar(255,255,0,255);
        lower_yellow  = new Scalar(20,150,100);
        upper_yellow = new Scalar(32,255,255);
        lower_red = new Scalar(90,150,100);
        upper_red = new Scalar(110,255,255);
	/*For finding color scalar boundaries, use HSV color map*/


        mRgba = new Mat(width,height,CvType.CV_8UC1);
        hsvMat = new Mat();
        yellowList = new ArrayList<>();
        blueList = new ArrayList<>();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat yellowTmp = mRgba.clone();
        Mat redTmp = mRgba.clone();
        mRgba.copyTo(hsvMat);
        Imgproc.cvtColor(hsvMat,hsvMat,Imgproc.COLOR_RGB2HSV);

        Core.inRange(hsvMat,lower_yellow,upper_yellow,yellowTmp);
        List<MatOfPoint> contours_yellow = new ArrayList<MatOfPoint>();
        Imgproc.findContours(yellowTmp,contours_yellow,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        Core.inRange(hsvMat,lower_red,upper_red,redTmp);
        List<MatOfPoint> contours_red = new ArrayList<MatOfPoint>();
        Imgproc.findContours(redTmp,contours_red,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        for(MatOfPoint countor : contours_yellow ){

            MatOfPoint2f areaPoints = new MatOfPoint2f(countor.toArray());
            RotatedRect rotatedRect  = Imgproc.minAreaRect(areaPoints);
            double rectangleArea = rotatedRect.size.area();

            if (rectangleArea > 1300 && rectangleArea < 5000000) {
                Point rotated_rect_points[] = new Point[4];
                rotatedRect.points(rotated_rect_points);
                Rect rect3 = Imgproc.boundingRect(new MatOfPoint(rotated_rect_points));
                if(rect3.width>70 && rect3.height>70) {
                    if(((rect3.width/rect3.height) > 0.98) && ((rect3.width/rect3.height) < 1.2)){
                        yellowList.add(rect3);
                    }
                }
            }
        }

        for(MatOfPoint countor : contours_red ){

            MatOfPoint2f areaPoints = new MatOfPoint2f(countor.toArray());
            RotatedRect rotatedRect  = Imgproc.minAreaRect(areaPoints);
            double rectangleArea = rotatedRect.size.area();

            if (rectangleArea > 1300 && rectangleArea < 5000000) {
                Point rotated_rect_points[] = new Point[4];
                rotatedRect.points(rotated_rect_points);
                Rect rect3 = Imgproc.boundingRect(new MatOfPoint(rotated_rect_points));
                if(((rect3.width/rect3.height) > 0.98) && ((rect3.width/rect3.height) < 1.2)){
                    blueList.add(rect3);
                }
            }
        }
        for (int i = 0; i < yellowList.size(); i++)
            Imgproc.rectangle(mRgba,yellowList.get(i).tl(),yellowList.get(i).br(),yellow,4);
        for(int i = 0; i < blueList.size(); i++)
            Imgproc.rectangle(mRgba,blueList.get(i).tl(),blueList.get(i).br(),red,4);
        yellowList.clear();
        blueList.clear();
        return mRgba;
    }


}