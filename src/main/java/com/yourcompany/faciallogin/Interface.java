

package com.yourcompany.faciallogin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class Interface extends Application {

    private LBPHFaceRecognizer recognizer;
    private CascadeClassifier faceDetector;
    private Map<Long, String> userMap;

    @Override
    public void start(Stage primaryStage) {
        // Load face recognizer
        recognizer = LBPHFaceRecognizer.create();
        recognizer.read("trained_faces.xml");

        // Load Haar Cascade
        faceDetector = new CascadeClassifier("haarcascade_frontalface_default.xml");

        // Load users.txt
        userMap = new HashMap<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    userMap.put(Long.parseLong(parts[1].trim()), parts[0].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // UI
        Label nameLabel = new Label("Enter Name:");
        TextField nameField = new TextField();
        Label idLabel = new Label("Enter ID:");
        TextField idField = new TextField();
        Button loginButton = new Button("Login");
        Label messageLabel = new Label();
        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(400);
        webcamView.setFitHeight(300);
        webcamView.setPreserveRatio(true);

        VBox root = new VBox(10, nameLabel, nameField, idLabel, idField, loginButton, messageLabel, webcamView);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Scene scene = new Scene(root, 450, 600);

        loginButton.setOnAction(e -> {
            String enteredName = nameField.getText().trim();
            long enteredId;
            try {
                enteredId = Long.parseLong(idField.getText().trim());
            } catch (NumberFormatException ex) {
                messageLabel.setText("❌ Invalid ID!");
                return;
            }

            messageLabel.setText("⏳ Scanning...");
            new Thread(() -> runRecognition(enteredName, enteredId, webcamView, messageLabel)).start();
        });

        primaryStage.setTitle("Facial Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void runRecognition(String enteredName, long enteredId, ImageView webcamView, Label messageLabel) {
        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
            // Map trained labels to Users
            Map<Integer, Users> labelMap = new HashMap<>();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("users.txt"))) {
                String line;
                int counter = 0;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        int id = Integer.parseInt(parts[1].trim());
                        labelMap.put(counter, new Users(name, id, counter));
                        counter++;
                    }
                }
            }

            grabber.start();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

            boolean authenticated = false;
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < 15000) { // max 15 sec
                Frame frame = grabber.grab();
                if (frame == null) continue;

                Mat mat = converter.convert(frame);
                Mat gray = new Mat();
                cvtColor(mat, gray, COLOR_BGR2GRAY);

                RectVector faces = new RectVector();
                faceDetector.detectMultiScale(gray, faces);

                for (int i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i);
                    Mat faceROI = new Mat(gray, face);
                    resize(faceROI, faceROI, new Size(200, 200));

//                    int[] label = new int[1];
//                    double[] confidence = new double[1];
//                    recognizer.predict(faceROI, label, confidence);
//
//                    if (confidence[0] < 80 && (long) label[0] == enteredId) {
//                        String predictedName = userMap.get((long) label[0]);
//                        if (predictedName != null && predictedName.equalsIgnoreCase(enteredName)) {
//                            authenticated = true;
//                        }
//                    }
                    int[] label = new int[1];
                    double[] confidence = new double[1];
                    recognizer.predict(faceROI, label, confidence);

// NEW authentication check
                    Users predicted = labelMap.get(label[0]);
                    if (predicted != null &&
                            predicted.getUserId() == enteredId &&
                            predicted.getName().equalsIgnoreCase(enteredName) &&
                            confidence[0] < 90) {  // adjust threshold if needed
                        authenticated = true;
                    }

                    rectangle(mat, face, new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
                }

                Mat finalMat = mat.clone();
                Platform.runLater(() -> webcamView.setImage(matToImage(finalMat)));

                if (authenticated) break;
            }

            grabber.stop();

            boolean finalAuth = authenticated;
            Platform.runLater(() -> {
                if (finalAuth) {
                    messageLabel.setText("✅ Access Granted!");
                } else {
                    messageLabel.setText("❌ too much makeup!");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private WritableImage matToImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        WritableImage image = new WritableImage(width, height);
        PixelWriter pw = image.getPixelWriter();

        byte[] data = new byte[width * height * channels];
        mat.data().get(data);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = 0, g = 0, b = 0;
                if (channels == 3) { // BGR
                    b = data[(y * width + x) * 3] & 0xFF;
                    g = data[(y * width + x) * 3 + 1] & 0xFF;
                    r = data[(y * width + x) * 3 + 2] & 0xFF;
                } else if (channels == 1) { // Grayscale
                    r = g = b = data[y * width + x] & 0xFF;
                }
                int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                pw.setArgb(x, y, argb);
            }
        }
        return image;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

