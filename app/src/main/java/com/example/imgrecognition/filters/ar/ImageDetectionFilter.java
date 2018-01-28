package com.example.imgrecognition.filters.ar;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.imgrecognition.MainActivity;
import com.example.imgrecognition.filters.Filter;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ImageDetectionFilter implements Filter {
    public static int MaxMatch=0;
    //Mat是矩阵
    // The reference image (this detector's target).参考图像（这个探测器的目标）
    private final Mat mReferenceImage;
    // Features of the reference image.参考图像的特征
    private final MatOfKeyPoint mReferenceKeypoints = new MatOfKeyPoint();
    // Descriptors of the reference image's features.参考图像特征的描述
    private final Mat mReferenceDescriptors = new Mat();
    // The corner coordinates of the reference image, in pixels.参考图像的角坐标，以像素为单位。
    // CvType defines the color depth, number of channels, and
    // channel layout in the image. Here, each point is represented
    // by two 32-bit floats.
    private final Mat mReferenceCorners = new Mat(4, 1, CvType.CV_32FC2);

    // Features of the scene (the current frame).场景的特征（当前帧）
    private final MatOfKeyPoint mSceneKeypoints = new MatOfKeyPoint();
    // Descriptors of the scene's features.场景特征的描述
    private final Mat mSceneDescriptors = new Mat();
    // Tentative corner coordinates detected in the scene, in pixels.在场景中检测的暂定角坐标，以像素为单位。
    private final Mat mCandidateSceneCorners = new Mat(4, 1, CvType.CV_32FC2);
    // Good corner coordinates detected in the scene, in pixels.在场景中检测到良好的角坐标，以像素为单位
    private final Mat mSceneCorners = new Mat(0, 0, CvType.CV_32FC2);
    // The good detected corner coordinates, in pixels, as integers.良好的检测角坐标，像素，整数
    private final MatOfPoint mIntSceneCorners = new MatOfPoint();

    // A grayscale version of the scene.场景的灰度版本
    private final Mat mGraySrc = new Mat();
    // Tentative matches of scene features and reference features.场景特征和参考特征的暂定匹配
    private final MatOfDMatch mMatches = new MatOfDMatch();

    // A feature detector, which finds features in images.一个特征检测器，用于查找图像中的特征
    private final FeatureDetector mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
    // A descriptor extractor, which creates descriptors of features.描述符提取器，它创建要素的描述符
    private final DescriptorExtractor mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    // A descriptor matcher, which matches features based on their descriptors.描述符匹配器，它根据描述符匹配特征
    private final DescriptorMatcher mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

    // The color of the outline drawn around the detected image.在检测到的图像周围绘制轮廓的颜色
    private final Scalar mLineColor = new Scalar(0, 255, 0);

    public ImageDetectionFilter(final Context context, final Bitmap bitmap) throws IOException {

        // Load the reference image from the app's resources.从app程序资源加载参考图像
        // It is loaded in BGR (blue, green, red) format.它以BGR（蓝色，绿色，红色）格式加载
        mReferenceImage = new Mat();
        Utils.bitmapToMat(bitmap,mReferenceImage);//mat；

        // Create grayscale and RGBA versions of the reference image.创建参考图像的灰度和RGBA版本
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2RGBA);

        // Store the reference image's corner coordinates, in pixels.存储参考图像角点坐标，以像素为单位
        mReferenceCorners.put(0, 0, new double[]{0.0, 0.0});
        mReferenceCorners.put(1, 0, new double[]{referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0, new double[]{referenceImageGray.cols(), referenceImageGray.rows()});
        mReferenceCorners.put(3, 0, new double[]{0.0, referenceImageGray.rows()});

        // Detect the reference features and compute their descriptors.检测参考特征并计算它们的描述符
        mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints, mReferenceDescriptors);
    }

    public ImageDetectionFilter(final String path) throws IOException {
        // Load the reference image from the app's resources.从app程序资源加载参考图像
        // It is loaded in BGR (blue, green, red) format.它以BGR（蓝色，绿色，红色）格式加载
        mReferenceImage = Utils.loadPath(path, Imgcodecs.CV_LOAD_IMAGE_COLOR);//把此处的mReferenceImage设置成由bitmap转换成的Image
        // Create grayscale and RGBA versions of the reference image.创建参考图像的灰度和RGBA版本
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2RGBA);

        // Store the reference image's corner coordinates, in pixels.存储参考图像角点坐标，以像素为单位
        mReferenceCorners.put(0, 0, new double[]{0.0, 0.0});
        mReferenceCorners.put(1, 0, new double[]{referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0, new double[]{referenceImageGray.cols(), referenceImageGray.rows()});
        mReferenceCorners.put(3, 0, new double[]{0.0, referenceImageGray.rows()});

        // Detect the reference features and compute their descriptors.检测参考特征并计算它们的描述符
        mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints, mReferenceDescriptors);
    }
    public ImageDetectionFilter(final Mat mat) throws IOException {
        mReferenceImage = mat;
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2RGBA);

        // Store the reference image's corner coordinates, in pixels.存储参考图像角点坐标，以像素为单位
        mReferenceCorners.put(0, 0, new double[]{0.0, 0.0});
        mReferenceCorners.put(1, 0, new double[]{referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0, new double[]{referenceImageGray.cols(), referenceImageGray.rows()});
        mReferenceCorners.put(3, 0, new double[]{0.0, referenceImageGray.rows()});

        // Detect the reference features and compute their descriptors.检测参考特征并计算它们的描述符
        mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints, mReferenceDescriptors);
    }
    @Override
    public boolean match(final Mat src, final Mat dst) {

        // Convert the scene to grayscale.将场景转换为灰度
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);

        // Detect the scene features, compute their descriptors,检测场景特征，计算它们的描述符
        // and match the scene descriptors to reference descriptors.将场景描述符与参考描述符相匹配
        mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
        mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints, mSceneDescriptors);
        mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors, mMatches);

        // Attempt to find the target image's corners in the scene.试图找到场景中的目标图像角落
        return findSceneCorners();
    }

    private boolean findSceneCorners() {
        MaxMatch=0;
        final List<DMatch> matchesList = mMatches.toList();

        if (matchesList.size() < 4) {
            // There are too few matches to find the homography.匹配太少，无法找到单应性矩阵
            return false;
        }

        final List<KeyPoint> referenceKeypointsList = mReferenceKeypoints.toList();
        final List<KeyPoint> sceneKeypointsList = mSceneKeypoints.toList();

        // Calculate the max and min distances between keypoints.计算关键点之间的最大和最小距离
        double maxDist = 0.0;
        double minDist = Double.MAX_VALUE;
        for (final DMatch match : matchesList) {
            final double dist = match.distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }

        // The thresholds for minDist are chosen subjectively
        // based on testing. The unit is not related to pixel
        // distances; it is related to the number of failed tests
        // for similarity between the matched descriptors.
        // minDist的阈值是根据测试主观选择的。 单位与像素距离无关; 它与匹配描述符之间的相似性测试失败的次数有关。
        if (minDist > 50.0) {
            // The target is completely lost.目标完全失败
            // Discard any previously found corners.丢弃以前找到的任何角落
            mSceneCorners.create(0, 0, mSceneCorners.type());
            return false;
        } else if (minDist > 25.0) {
            // The target is lost but maybe it is still close.目标已经丢失，但也许还是接近
            // Keep any previously found corners.保留以前找到的任何角落
            return false;
        }

        // Identify "good" keypoints based on match distance.根据匹配距离确定“好”关键点
        final ArrayList<Point> goodReferencePointsList = new ArrayList<Point>();
        final ArrayList<Point> goodScenePointsList = new ArrayList<Point>();
        final double maxGoodMatchDist = 1.35 * minDist;
        for (final DMatch match : matchesList) {
            if (match.distance < maxGoodMatchDist) {
                goodReferencePointsList.add(referenceKeypointsList.get(match.trainIdx).pt);
                goodScenePointsList.add(sceneKeypointsList.get(match.queryIdx).pt);
            }
        }

        if (goodReferencePointsList.size() < 4 || goodScenePointsList.size() < 4) {
            // There are too few good points to find the homography.好点太少，无法找到单应性矩阵

            return false;
        }
        MaxMatch = goodReferencePointsList.size();
        return true;
    }
}
