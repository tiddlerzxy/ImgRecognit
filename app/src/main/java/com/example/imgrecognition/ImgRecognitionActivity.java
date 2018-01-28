package com.example.imgrecognition;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;////////////////////////////
import android.os.Message;
import android.os.Bundle;
import android.view.SurfaceView;

import com.example.imgrecognition.filters.Filter;
import com.example.imgrecognition.filters.ar.ImageDetectionFilter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.Console;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class ImgRecognitionActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView jcv;
    // The filters.
    private Filter[] mImageDetectionFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_recognition);
        //new Thread(networkTask).start();/////////////////////////////////////////////
        mThreadPool = Executors.newCachedThreadPool();
        jcv = (JavaCameraView) findViewById(R.id.jcv);
        jcv.setVisibility(SurfaceView.VISIBLE);
        jcv.setCvCameraViewListener(this);
    }
    Bitmap[] bitmaps = MainActivity.bitmaps;

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (jcv != null)
            jcv.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jcv != null)
            jcv.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    jcv.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //摄像头开始获取影像
    //设置下我们需要识别的图片
    @Override
    public void onCameraViewStarted(int width, int height) {
        int i = 0;
        mImageDetectionFilters = new Filter[bitmaps.length];
        for (Bitmap bitmap : bitmaps) {
            Filter starryNight = null;
            try {
                starryNight = new ImageDetectionFilter(this, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageDetectionFilters[i] = starryNight;
            i++;
        }
    }
    //摄像头停止获取影像
    @Override
    public void onCameraViewStopped() {

    }

    private long lastTime;
    /**
     * describe 管理发送消息的线程池
     */
    private ExecutorService mThreadPool;

    //摄像头获取每一帧画面后回调，我们进行图像识别就是主要在这个方法中实现。
    //在onCameraFrame中进行图像对比
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();//Mat是存储了图像像素信息的矩阵   rgba是图像像素
        long time = System.currentTimeMillis() / 3000;
        if (time > lastTime) {
            if (mImageDetectionFilters != null) {//mImageDetectionFilters是reference个数大小的数组
                for (int i = 0; i < mImageDetectionFilters.length; i++) {//依次遍历reference
                    final int y = i;//y记录当前为第几个reference
                    mThreadPool.execute(new Runnable() {
                        @Override

                        public void run() {
                            if (mImageDetectionFilters[y].match(rgba, rgba)) {
                                h.sendEmptyMessage(y);
                            }
                        }
                    });
                }
            }
        }
        lastTime = time;
        return rgba;//如果有合适的匹配就返回
    }

//    boolean havematched = false;
//    @Override
//    public Mat onCameraFrame (CameraBridgeViewBase.CvCameraViewFrame inputFrame){
//        final Mat rgba = inputFrame.rgba();//Mat是存储了图像像素信息的矩阵   rgba是图像像素
//        final int[] matchresult = new int[mImageDetectionFilters.length];
//        long time = System.currentTimeMillis() / 3000;
//        if (time > lastTime) {
//            if (mImageDetectionFilters != null) {
//                Thread threadB = null;
//                for (int i = 0; i < mImageDetectionFilters.length; i++) {//依次遍历reference
//                    final int y = i;//y记录当前为第几个reference
//                    threadB = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mImageDetectionFilters[y].match(rgba, rgba)) {
//                                havematched = true;
//                                matchresult[y] = ImageDetectionFilter.MaxMatch;
//                            }
//                        }
//
//                    });
//                    threadB.start();
//                }//for
//
//                try{
//                    threadB.join();
//                }catch (InterruptedException e){ }
//
//                if(havematched) {
//                    int max = 0;int id=0;
//                    for(int i=0;i<matchresult.length;i++){
//                        if(matchresult[i]>max) {
//                            max = matchresult[i];
//                            id = i;
//                        }
//                    }
//                    havematched = false;
//                    h.sendEmptyMessage(id);
//                }
//            }
//        }
//        lastTime = time;
//        return rgba;//如果有合适的匹配就返回
//    }

    boolean isFinish=false;
    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {//handleMessage消息处理函数

            if (!isFinish) {
                int i = msg.what;
                Intent intent = new Intent();
                intent.putExtra("data", i);//对应MainActivity里的int data2 = data.getIntExtra("data", -1);再将图片显示出来
                setResult(RESULT_OK, intent);
                isFinish = true;//此处，只发了第一个匹配的图片，剩下的图片就舍弃了？
                finish();
            }
            super.handleMessage(msg);
        }
    };
}
