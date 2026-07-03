//package com.yourcompany.faciallogin;
//
//import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
//import static org.bytedeco.opencv.global.opencv_imgproc.*;
//import org.bytedeco.opencv.global.opencv_core;
//import org.bytedeco.opencv.opencv_core.Mat;
//import java.io.File;
//import java.nio.IntBuffer;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import org.bytedeco.javacv.*;
//import org.bytedeco.opencv.opencv_core.*;
//import org.bytedeco.opencv.opencv_face.FaceRecognizer;
//import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
//import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
//
//public class LoginController {
//
//    @FXML
//    private ImageView cameraView;
//
//    @FXML
//    private TextField nameField;
//
//    @FXML
//    private Label statusLabel;
//
//    @FXML
//    private Button enrollButton;
//
//    @FXML
//    private Button loginButton;
//
//    private ScheduledExecutorService timer;
//    private OpenCVFrameGrabber grabber;
//    private CascadeClassifier faceCascade;
//    private FaceRecognizer faceRecognizer;
//    private final JavaFXFrameConverter frameConverter =
//        new JavaFXFrameConverter();
//
//    private boolean isLoginMode = false;
//    private int captureCount = 0;
//    private static final int CAPTURE_LIMIT = 30;
//    private static final String FACE_SAMPLES_DIR = "src/main/resources/faces/";
//    private static final String CASCADE_FILE =
//        "src/main/resources/cascades/haarcascade_frontalface_alt.xml";
//    private static final String TRAINED_MODEL_FILE =
//        "src/main/resources/trained_model.yml";
//
//    @FXML
//    public void initialize() {
//        grabber = new OpenCVFrameGrabber(0);
//        faceCascade = new CascadeClassifier(CASCADE_FILE);
//        faceRecognizer = LBPHFaceRecognizer.create();
//
//        new File(FACE_SAMPLES_DIR).mkdirs();
//
//        startCamera();
//        trainModel();
//    }
//
//    private void startCamera() {
//        try {
//            grabber.start();
//            timer = Executors.newSingleThreadScheduledExecutor();
//
//            timer.scheduleAtFixedRate(
//                () -> {
//                    try {
//                        Frame frame = grabber.grab();
//                        if (frame != null) {
//                            Mat image =
//                                new OpenCVFrameConverter.ToMat().convert(frame);
//                            detectAndProcessFaces(image);
//                            final Image fxImage = frameConverter.convert(
//                                new OpenCVFrameConverter.ToMat().convert(image)
//                            );
//                            Platform.runLater(() ->
//                                cameraView.setImage(fxImage)
//                            );
//                        }
//                    } catch (FrameGrabber.Exception e) {
//                        e.printStackTrace();
//                    }
//                },
//                0,
//                66,
//                TimeUnit.MILLISECONDS
//            );
//        } catch (FrameGrabber.Exception e) {
//            e.printStackTrace();
//            Platform.runLater(() ->
//                statusLabel.setText("Error: Could not start camera.")
//            );
//        }
//    }
//
//    private void detectAndProcessFaces(Mat image) {
//        Mat grayImage = new Mat();
//        cvtColor(image, grayImage, COLOR_BGR2GRAY);
//
//        RectVector faces = new RectVector();
//        faceCascade.detectMultiScale(grayImage, faces);
//
//        for (int i = 0; i < faces.size(); i++) {
//            Rect faceRect = faces.get(i);
//            rectangle(image, faceRect, new Scalar(0, 255, 0, 1));
//            Mat face = new Mat(grayImage, faceRect);
//
//            if (captureCount > 0 && captureCount <= CAPTURE_LIMIT) {
//                saveFaceSample(face);
//                Platform.runLater(() ->
//                    statusLabel.setText(
//                        "Capturing... " + captureCount + "/" + CAPTURE_LIMIT
//                    )
//                );
//                captureCount++;
//                if (captureCount > CAPTURE_LIMIT) {
//                    Platform.runLater(() ->
//                        statusLabel.setText(
//                            "Enrollment complete! Training model..."
//                        )
//                    );
//                    trainModel();
//                    captureCount = 0;
//                }
//            }
//
//            if (isLoginMode) {
//                int[] label = new int[1];
//                double[] confidence = new double[1];
//                faceRecognizer.predict(face, label, confidence);
//
//                String name;
//                if (confidence[0] < 60) {
//                    name = "User " + label[0];
//                    Platform.runLater(() -> {
//                        statusLabel.setText(
//                            "Login Successful! Welcome, " + name
//                        );
//                        isLoginMode = false;
//                    });
//                } else {
//                    name = "Unknown";
//                }
//                putText(
//                    image,
//                    name,
//                    new Point(faceRect.x(), faceRect.y() - 10),
//                    FONT_HERSHEY_PLAIN,
//                    1.5,
//                    new Scalar(0, 0, 255, 1)
//                );
//            }
//        }
//    }
//
//    private void saveFaceSample(Mat face) {
//        String userName = nameField.getText().trim().replaceAll("\\s+", "_");
//        if (userName.isEmpty()) {
//            Platform.runLater(() ->
//                statusLabel.setText("Please enter a name first!")
//            );
//            captureCount = 0;
//            return;
//        }
//
//        File userDir = new File(FACE_SAMPLES_DIR);
//        int userId = 0;
//        File[] userDirs = userDir.listFiles(File::isDirectory);
//        if (userDirs != null) {
//            userId = userDirs.length;
//            for (int i = 0; i < userDirs.length; i++) {
//                if (userDirs[i].getName().equals(userName)) {
//                    userId = i;
//                    break;
//                }
//            }
//        }
//
//        File targetUserDir = new File(
//            FACE_SAMPLES_DIR + userId + "_" + userName
//        );
//        targetUserDir.mkdirs();
//
//        Mat resizedFace = new Mat();
//        resize(face, resizedFace, new Size(160, 160));
//
//        String filename =
//            targetUserDir.getPath() + "/" + System.currentTimeMillis() + ".jpg";
//        imwrite(filename, resizedFace);
//    }
//
//    private void trainModel() {
//        File root = new File(FACE_SAMPLES_DIR);
//        File[] userDirs = root.listFiles(File::isDirectory);
//
//        if (userDirs == null || userDirs.length == 0) {
//            Platform.runLater(() ->
//                statusLabel.setText(
//                    "Status: No data to train. Please enroll users."
//                )
//            );
//            return;
//        }
//
//        MatVector images = new MatVector(userDirs.length * 50);
////        Mat labels = new Mat(userDirs.length * 50, 1, CV_32SC1);
//        Mat labels = new Mat(userDirs.length * 50, 1, opencv_core.CV_32SC1);
//        IntBuffer labelsBuf = labels.createBuffer();
//
//        int counter = 0;
//        for (File userDir : userDirs) {
//            try {
//                int labelId = Integer.parseInt(userDir.getName().split("_")[0]);
//                File[] imageFiles = userDir.listFiles((dir, name) ->
//                    name.endsWith(".jpg")
//                );
//                if (imageFiles != null) {
//                    for (File imageFile : imageFiles) {
//                        Mat img = imread(
//                            imageFile.getAbsolutePath(),
//                            IMREAD_GRAYSCALE
//                        );
//                        images.put(counter, img);
//                        labelsBuf.put(counter, labelId);
//                        counter++;
//                    }
//                }
//            } catch (NumberFormatException e) {
//                System.out.println(
//                    "Skipping directory with invalid name format: " +
//                    userDir.getName()
//                );
//            }
//        }
//
//        if (counter > 0) {
//            faceRecognizer.train(images, labels);
//            faceRecognizer.save(TRAINED_MODEL_FILE);
//            Platform.runLater(() ->
//                statusLabel.setText("Status: Model trained. Ready to login.")
//            );
//        }
//    }
//
//    @FXML
//    public void handleEnrollButton() {
//        String name = nameField.getText();
//        if (name.trim().isEmpty()) {
//            statusLabel.setText("Error: Name cannot be empty.");
//            return;
//        }
//        isLoginMode = false;
//        captureCount = 1;
//        statusLabel.setText("Enrolling... Look at the camera.");
//    }
//
//    @FXML
//    public void handleLoginButton() {
//        File modelFile = new File(TRAINED_MODEL_FILE);
//        if (!modelFile.exists()) {
//            statusLabel.setText(
//                "Error: No trained model found. Please enroll first."
//            );
//            return;
//        }
//        faceRecognizer.read(TRAINED_MODEL_FILE);
//        isLoginMode = true;
//        statusLabel.setText("Attempting to log in...");
//    }
//
//    public void stopCamera() {
//        if (timer != null && !timer.isShutdown()) {
//            try {
//                timer.shutdown();
//                timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
//                if (grabber != null) {
//                    grabber.stop();
//                    grabber.release();
//                }
//            } catch (InterruptedException | FrameGrabber.Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
