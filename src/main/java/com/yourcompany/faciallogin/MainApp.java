//
//package com.yourcompany.faciallogin;
//
//import org.bytedeco.javacv.*;
//import org.bytedeco.opencv.opencv_core.*;
//import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
//import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.bytedeco.opencv.global.opencv_core.*;
//import static org.bytedeco.opencv.global.opencv_imgproc.*;
//
//public class MainApp {
//
//    // Static method to be called from Interface.java
//    public static boolean startRecognition(String enteredName, long enteredId) {
//        String cascadePath = "haarcascade_frontalface_default.xml";
//        String modelPath   = "trained_faces.xml";
//        String usersFile   = "users.txt";
//
//        // Load Haar Cascade
//        CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
//        if (faceDetector.empty()) {
//            System.err.println("❌ Error loading Haar Cascade file.");
//            return false;
//        }
//
//        // Load trained recognizer
//        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
//        recognizer.read(modelPath);
//
//        // Load users mapping
//        Map<Long, String> userMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(",");
//                if (parts.length == 2) {
//                    String name = parts[0].trim();
//                    long id = Long.parseLong(parts[1].trim());
//                    userMap.put(id, name);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        boolean authenticated = false;
//
//        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
//            grabber.start();
//            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
//
//            long startTime = System.currentTimeMillis();
//            while (System.currentTimeMillis() - startTime < 10000) { // max 10 sec
//                Frame frame = grabber.grab();
//                if (frame == null) break;
//
//                Mat mat = converter.convert(frame);
//                Mat gray = new Mat();
//                cvtColor(mat, gray, COLOR_BGR2GRAY);
//
//                RectVector faces = new RectVector();
//                faceDetector.detectMultiScale(gray, faces);
//
//                for (int i = 0; i < faces.size(); i++) {
//                    Rect face = faces.get(i);
//
//                    Mat faceROI = new Mat(gray, face);
//                    resize(faceROI, faceROI, new Size(200, 200));
//
//                    int[] label = new int[1];
//                    double[] confidence = new double[1];
//                    recognizer.predict(faceROI, label, confidence);
//
//                    if (confidence[0] < 80 && (long) label[0] == enteredId) {
//                        String predictedName = userMap.get((long) label[0]);
//                        if (predictedName != null && predictedName.equalsIgnoreCase(enteredName)) {
//                            authenticated = true;
//                            break;
//                        }
//                    }
//                }
//
//                if (authenticated) break;
//            }
//
//            grabber.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return authenticated;
//    }
//
//    // Optional: keep console main for testing
//    public static void main(String[] args) {
//        System.out.println("Run Interface.java for GUI version.");
//    }
//}
//
//
//
//
//


//package com.yourcompany.faciallogin;
//
//import org.bytedeco.javacpp.indexer.UByteIndexer;
//
//import javafx.application.Platform;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.WritableImage;
//import org.bytedeco.javacv.*;
//import org.bytedeco.opencv.opencv_core.*;
//import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
//import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.bytedeco.opencv.global.opencv_imgproc.*;
//
//public class MainApp {
//
//    // Convert OpenCV Mat to JavaFX Image
//    private static Image matToImage(Mat mat) {
//        int width = mat.cols();
//        int height = mat.rows();
//        WritableImage image = new WritableImage(width, height);
//        PixelWriter pw = image.getPixelWriter();
//
//        if (mat.channels() == 1) { // grayscale
//            UByteIndexer indexer = mat.createIndexer();
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    int gray = indexer.get(y, x) & 0xFF;
//                    int argb = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
//                    pw.setArgb(x, y, argb);
//                }
//            }
//            indexer.release();
//        } else { // color
//            UByteIndexer indexer = mat.createIndexer();
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    int b = indexer.get(y, x, 0) & 0xFF;
//                    int g = indexer.get(y, x, 1) & 0xFF;
//                    int r = indexer.get(y, x, 2) & 0xFF;
//                    int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
//                    pw.setArgb(x, y, argb);
//                }
//            }
//            indexer.release();
//        }
//
//        return image;
//    }
//
//    // Recognition method with JavaFX ImageView
//    public static boolean startRecognition(String enteredName, long enteredId, ImageView fxView) {
//        String cascadePath = "haarcascade_frontalface_default.xml";
//        String modelPath = "trained_faces.xml";
//        String usersFile = "users.txt";
//
//        // Load Haar Cascade
//        CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
//        if (faceDetector.empty()) {
//            System.err.println("❌ Error loading Haar Cascade file.");
//            return false;
//        }
//
//        // Load trained recognizer
//        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
//        recognizer.read(modelPath);
//
//        // Load users mapping
//        Map<Long, String> userMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(",");
//                if (parts.length == 2) {
//                    String name = parts[0].trim();
//                    long id = Long.parseLong(parts[1].trim());
//                    userMap.put(id, name);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        final boolean[] authenticated = {false};
//
//        Thread webcamThread = new Thread(() -> {
//            try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
//                grabber.start();
//                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
//
//                long startTime = System.currentTimeMillis();
//                while (System.currentTimeMillis() - startTime < 15000) { // 15 sec max
//                    Frame frame = grabber.grab();
//                    if (frame == null) break;
//
//                    Mat mat = converter.convert(frame);
//                    Mat gray = new Mat();
//                    cvtColor(mat, gray, COLOR_BGR2GRAY);
//
//                    RectVector faces = new RectVector();
//                    faceDetector.detectMultiScale(gray, faces);
//
//                    for (int i = 0; i < faces.size(); i++) {
//                        Rect face = faces.get(i);
//                        Mat faceROI = new Mat(gray, face);
//                        org.bytedeco.opencv.global.opencv_imgproc.resize(faceROI, faceROI, new Size(200, 200));
//
//                        int[] label = new int[1];
//                        double[] confidence = new double[1];
//                        recognizer.predict(faceROI, label, confidence);
//
//                        if (confidence[0] < 80 && (long) label[0] == enteredId) {
//                            String predictedName = userMap.get((long) label[0]);
//                            if (predictedName != null && predictedName.equalsIgnoreCase(enteredName)) {
//                                authenticated[0] = true;
//                                break;
//                            }
//                        }
//                    }
//
//                    // Update ImageView in JavaFX thread
//                    Image fxImage = matToImage(mat);
//                    Platform.runLater(() -> fxView.setImage(fxImage));
//
//                    if (authenticated[0]) break;
//                }
//
//                grabber.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        webcamThread.setDaemon(true);
//        webcamThread.start();
//
//        try {
//            webcamThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return authenticated[0];
//    }
//
//    public static void main(String[] args) {
//        System.out.println("Run Interface.java for GUI version.");
//    }
//}


//package com.yourcompany.faciallogin;
//
//import javafx.application.Platform;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.WritableImage;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.PixelFormat;
//
//import org.bytedeco.javacv.*;
//import org.bytedeco.opencv.opencv_core.*;
//import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
//import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.bytedeco.opencv.global.opencv_imgproc.*;
//
//public class MainApp {
//
//    private static CascadeClassifier faceDetector;
//    private static LBPHFaceRecognizer recognizer;
//    private static Map<Long, String> userMap;
//
//    // Initialize recognizer and load trained data
//    public static void initialize(String cascadePath, String modelPath, String usersFile) {
//        faceDetector = new CascadeClassifier(cascadePath);
//        if (faceDetector.empty()) {
//            throw new RuntimeException("Error loading Haar Cascade file: " + cascadePath);
//        }
//
//        recognizer = LBPHFaceRecognizer.create();
//        recognizer.read(modelPath);
//
//        userMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(",");
//                if (parts.length >= 2) {
//                    userMap.put(Long.parseLong(parts[1].trim()), parts[0].trim());
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to load users.txt", e);
//        }
//    }
//
//    // Start recognition, returns true if match found
//    public static boolean startRecognition(String enteredName, long enteredId, ImageView imageView) {
//        final boolean[] authenticated = {false};
//
//        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
//            grabber.start();
//            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
//
//            long startTime = System.currentTimeMillis();
//            while (System.currentTimeMillis() - startTime < 10000) { // max 10 sec
//                Frame frame = grabber.grab();
//                if (frame == null) break;
//
//                Mat mat = converter.convert(frame);
//                Mat gray = new Mat();
//                cvtColor(mat, gray, COLOR_BGR2GRAY);
//
//                RectVector faces = new RectVector();
//                faceDetector.detectMultiScale(gray, faces);
//
//                for (int i = 0; i < faces.size(); i++) {
//                    Rect face = faces.get(i);
//                    Mat faceROI = new Mat(gray, face);
//                    org.bytedeco.opencv.global.opencv_imgproc.resize(faceROI, faceROI, new Size(200, 200));
//
//                    int[] label = new int[1];
//                    double[] confidence = new double[1];
//                    recognizer.predict(faceROI, label, confidence);
//
//                    if (confidence[0] < 80 && (long) label[0] == enteredId) {
//                        String predictedName = userMap.get((long) label[0]);
//                        if (predictedName != null && predictedName.equalsIgnoreCase(enteredName)) {
//                            authenticated[0] = true;
//                            break;
//                        }
//                    }
//                }
//
//                // Update JavaFX ImageView with live frame
//                Image fxImage = matToImage(mat);
//                Platform.runLater(() -> imageView.setImage(fxImage));
//
//                if (authenticated[0]) break;
//            }
//
//            grabber.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return authenticated[0];
//    }
//
//    // Convert OpenCV Mat to JavaFX Image
//    private static Image matToImage(Mat mat) {
//        int width = mat.cols();
//        int height = mat.rows();
//        WritableImage image = new WritableImage(width, height);
//        PixelWriter pw = image.getPixelWriter();
//        byte[] data = new byte[width * height * (int)mat.elemSize()];
//        mat.data().get(data);
//        pw.setPixels(0, 0, width, height, PixelFormat.getByteRgbInstance(), data, 0, width * 3);
//        return image;
//    }
//}
