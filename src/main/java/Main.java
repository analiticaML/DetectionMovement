import nu.pattern.OpenCV;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.SystemSettingsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;


public class Main {

    public static void main(String[] args) throws Exception {
        String capturesFolder = System.getenv("CAPTURES_FOLDER");
        String mqHost = System.getenv("MQ_HOST");
        String movementSensibility = System.getenv("MOVEMENT_SENSIBILITY");
        String millisecondsBetweenCaptures = System.getenv("MILLISECONDS_BETWEEN_CAPTURES");
        String bucket_name = "prueba-bucket-machine";
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
        System.out.println(s3);
        System.out.println(millisecondsBetweenCaptures);
        System.out.println(capturesFolder);
        OpenCV.loadLocally();
        Recorder recorder = new Recorder(capturesFolder, mqHost, Integer.parseInt(movementSensibility), Integer.parseInt(millisecondsBetweenCaptures),s3, bucket_name);
        recorder.start();
    }
}
