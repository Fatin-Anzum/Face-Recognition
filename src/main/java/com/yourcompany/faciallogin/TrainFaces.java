
////import org.bytedeco.opencv.opencv_imgcodecs.*;
////import org.bytedeco.opencv.opencv_imgproc.*;

package com.yourcompany.faciallogin;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class TrainFaces {

    public static void main(String[] args) {
        String datasetPath = "C:\\Users\\SAFWAN AHMED HABIB\\Desktop\\Final Project CSE 215\\facial-login-project-main\\dataset";
        String cascadePath = "C:\\Users\\SAFWAN AHMED HABIB\\Desktop\\Final Project CSE 215\\facial-login-project-main\\haarcascade_frontalface_default.xml";
        String modelPath = "C:\\Users\\SAFWAN AHMED HABIB\\Desktop\\Final Project CSE 215\\facial-login-project-main\\trained_faces.xml";
        String usersFile = "C:\\Users\\SAFWAN AHMED HABIB\\Desktop\\Final Project CSE 215\\facial-login-project-main\\users.txt";

        CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
        if (faceDetector.empty()) {
            System.err.println("❌ Error loading Haar Cascade file.");
            return;
        }

        ArrayList<Mat> faceImages = new ArrayList<>();
        ArrayList<Integer> faceLabels = new ArrayList<>();
        StringBuilder userFileContent = new StringBuilder();

        File root = new File(datasetPath);
        File[] personDirs = root.listFiles();
        if (personDirs == null) {
            System.err.println("❌ Dataset folder not found or empty.");
            return;
        }

        for (File personDir : personDirs) {
            if (!personDir.isDirectory()) continue;

            // Folder name format: name_id.
            String[] nameId = personDir.getName().split("_");
            if (nameId.length != 2) {
                System.err.println("⚠ Skipping folder: " + personDir.getName() + " (invalid name_id format)");
                continue;
            }
            String name = nameId[0];
            int id;
            try {
                id = Integer.parseInt(nameId[1]);
            } catch (NumberFormatException e) {
                System.err.println("⚠ Invalid ID for: " + personDir.getName());
                continue;
            }

            // Store mapping in users.txt format: name,id
            userFileContent.append(name).append(",").append(id).append("\n");

            // Load all images in this folder
            File[] images = personDir.listFiles();
            if (images == null) continue;

            for (File imageFile : images) {
                Mat img = imread(imageFile.getAbsolutePath());
                if (img.empty()) {
                    System.err.println("⚠ Could not read image: " + imageFile.getName());
                    continue;
                }

                Mat gray = new Mat();
                cvtColor(img, gray, COLOR_BGR2GRAY);

                RectVector faces = new RectVector();
                faceDetector.detectMultiScale(gray, faces);

                for (int i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i);
                    Mat faceROI = new Mat(gray, face);
                    resize(faceROI, faceROI, new Size(200, 200));

                    faceImages.add(faceROI);
                    faceLabels.add(id);
                }
            }
        }

        if (faceImages.isEmpty()) {
            System.err.println("❌ No faces found in dataset.");
            return;
        }

        // Convert ArrayList to MatVector
        MatVector imagesVector = new MatVector(faceImages.size());
        Mat labelsMat = new Mat(faceImages.size(), 1, CV_32SC1);
        IntBuffer labelsBuf = labelsMat.createBuffer();

        for (int i = 0; i < faceImages.size(); i++) {
            imagesVector.put(i, faceImages.get(i));
            labelsBuf.put(i, faceLabels.get(i));
        }

        // Train and save model
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.train(imagesVector, labelsMat);
        recognizer.save(modelPath);

        System.out.println("✅ Training complete. Model saved at: " + modelPath);

        // Save users.txt mapping
        try (FileWriter fw = new FileWriter(usersFile)) {
            fw.write(userFileContent.toString());
            System.out.println("✅ Users file saved at: " + usersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

