import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;



import java.util.ArrayList;
import java.util.List;

import static org.opencv.videoio.Videoio.*;

class CameraService {

    private final VideoCapture camera = new VideoCapture();
    private int count = 0;

    Mat getGrayScaleFrame(Mat frame) {
        Mat greyScaleFrame = new Mat();
        Imgproc.cvtColor(frame, greyScaleFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(greyScaleFrame, greyScaleFrame, new Size(21, 21), 0);

        return greyScaleFrame;
    }

    void openCamera(int width, int height) throws InterruptedException {
        //System.out.println("library path");
        //System.out.println(System.getProperty("java.library.path"));
        String path = null;
        try {
            //I have copied dlls from opencv folder to my project folder
            System.load(System.getenv("JAVA_OPENCV_DLL"));
            System.load(System.getenv("OPENCV_FFMPEG"));
            //System.loadLibrary("opencv_ffmpe320_64");
            //System.loadLibrary("opencv_java320");



        } catch (UnsatisfiedLinkError e) {
            System.out.println("Error loading libs");
        }

        //camera.open(0,CAP_DSHOW); //open camera
        camera.open("rtsp://admin:admin@192.168.1.80:1935",CAP_FFMPEG);

        if (camera.isOpened()) {
            System.out.println("Video is captured");}
        else {
            System.out.println("Video is not working");
        }



        double width2 = camera.get(CAP_PROP_FRAME_WIDTH );
        double height2 = camera.get(CAP_PROP_FRAME_HEIGHT );
        //double fps = camera.get(CAP_PROP_FPS);
        //double countfps = camera.get(CAP_PROP_FRAME_COUNT);

        System.out.println(width2);
        System.out.println(height2);
        //System.out.println(fps);
        //System.out.println(countfps);

        //camera.open(0);
        camera.set(3, width);
        camera.set(4, height);

        //camera.set(CAP_PROP_BUFFERSIZE, 1);

        Thread.sleep(1000);
    }

    Mat getFrame() throws InterruptedException  {
        Mat frame = new Mat();
        camera.read(frame);

        //System.out.println("frame");
        //System.out.println(frame);
        return frame;
    }

    private Mat getFramesDifference(Mat frameGrayScale, Mat newFrameGrayScale) {
        Mat framesDifference = new Mat();
        Core.absdiff(frameGrayScale, newFrameGrayScale, framesDifference);
        return framesDifference;
    }

    private List<MatOfPoint> getContours(Mat framesDifference) {
        Mat threshold = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.threshold(framesDifference, threshold, 25, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(threshold, threshold, new Mat(), new Point(-1, -1), 2);
        Imgproc.findContours(threshold, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours;
    }

    boolean detectMovement(Mat frameGrayScale, Mat newFrameGrayScale, int limit) {
        Mat framesDifference = this.getFramesDifference(frameGrayScale, newFrameGrayScale);
        List<MatOfPoint> framesDifferenceContours = this.getContours(framesDifference);

        System.out.println("Entra al detectMovement");
        System.out.println(framesDifferenceContours);

        for (MatOfPoint framesDifferenceContour : framesDifferenceContours) {
            System.out.println("Frame difference Contours");
            System.out.println(Imgproc.contourArea(framesDifferenceContour));
            if (Imgproc.contourArea(framesDifferenceContour) > limit) {
                System.out.println(count++);


                return true;
            }
        }
        return false;
    }

}
