import com.google.gson.Gson;
import org.opencv.core.Mat;
import software.amazon.awssdk.services.s3.S3Client;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class Recorder {

    private final int movementSensibility;
    private final int millisecondsBetweenCaptures;
    private final CameraService cameraService;
    private final ImageService imageService;
    private final RabbitmqService rabbitmqService;
    private final String path;
    private final String mqHost;

    private final PutS3 putS3;

    private final S3Client s3;

    private final String bucketName;

    private int cuenta=0;

    Recorder(String path, String mqHost, int movementSensibility, int millisecondsBetweenCaptures, S3Client s3, String bucketName) {
        this.cameraService = new CameraService();
        this.imageService = new ImageService();
        this.rabbitmqService = new RabbitmqService();
        this.path = path;
        this.mqHost = mqHost;
        this.movementSensibility =  movementSensibility; //5000
        this.millisecondsBetweenCaptures = millisecondsBetweenCaptures;
        this.s3 = s3;
        this.bucketName = bucketName;
        this.putS3 = new PutS3();
    }

    void start() throws Exception {
        imageService.setFolder(path);
        cameraService.openCamera(640, 480);

        Mat previousGrayFrame = cameraService.getGrayScaleFrame(cameraService.getFrame());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
        Gson gson = new Gson();

        while (true) {
            Mat newFrame = cameraService.getFrame();
            cuenta = cuenta + 1;

            if (cuenta > 5) {
                Mat newFrameGrayScale = cameraService.getGrayScaleFrame(newFrame);
                System.out.println("antes de verificar si hay movimiento");


                if (cameraService.detectMovement(previousGrayFrame, newFrameGrayScale, 5000)) {
                    System.out.println("Motion detected!!!");

                    String date = simpleDateFormat.format(new Date());
                    String path = this.path + "/" + date + ".jpg";
                    Map<String, String> data = new HashMap<>();
                    data.put("path", path);
                    System.out.println("Data: " + data.toString());
                    String name = date +".jpg";
                    //this.imageService.saveImage(newFrame, path);

                    //this.rabbitmqService.publish(gson.toJson(data), "captured-image-queue", mqHost);
                    this.putS3.putS3Object((software.amazon.awssdk.services.s3.S3Client) s3,bucketName,name,newFrame,name);
                }

                previousGrayFrame = newFrameGrayScale;

                TimeUnit.MILLISECONDS.sleep(1000);

                cuenta=0;
            }
        }
    }
}
