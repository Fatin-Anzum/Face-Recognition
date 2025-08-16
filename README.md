# Facial Recognition Login System

A proof-of-concept **console-based facial recognition login system** built with **Java** and **JavaCV (OpenCV bindings)**.  
Users can enroll their face, train a recognition model, and log in using their **Name + ID + Face**.

---

## 📌 Application Workflow

```mermaid
graph TD
    A[Start Application] --> B{Camera Initialized};
    B --> C{Face Detected?};
    C -- No --> B;
    C -- Yes --> D[Draw Rectangle Around Face];
    D --> E{User Action};
    E -- Run TrainFaces.java --> F[Capture Face Samples + Assign Name/ID];
    F --> G[Save images + users.txt mapping];
    G --> H[Train LBPH Model];
    H --> I[Save trained_faces.xml];
    E -- Run MainApp.java --> J[Load trained_faces.xml + users.txt];
    J --> K[Predict Face];
    K --> L{Confidence < 80?};
    L -- Yes --> M[✅ Login Successful];
    L -- No --> N[❌ Access Denied];
## ✨ Features

- 🎥 **Live Camera Feed** – Real-time webcam capture  
- 👤 **Face Detection** – Haar Cascade-based detection  
- 📝 **User Enrollment** – Capture face samples under **Name + ID**  
- 🧠 **Model Training** – Train LBPH recognizer (`trained_faces.xml`)  
- 🔐 **Facial Login** – Authenticate user by verifying **Name + ID + Face**  

---

## 🛠️ Tech Stack

- ☕ **Java 11+** – Programming language  
- 🔍 **JavaCV (OpenCV)** – Face detection & recognition  
- ⚙️ **Maven** – Build & dependency management  
- 📄 **Haar Cascade** – Pre-trained XML for face detection  

## 🛠️ Project Structure

.
├── pom.xml
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── yourcompany
│       │           └── faciallogin
│       │               ├── MainApp.java       # Login with face recognition (Name + ID)
│       │               ├── TrainFaces.java    # Enroll & train faces, update users.txt
│       │               ├── Users.java         # POJO: name + id (+ optional label)
│       │               └── UserData.java      # File-based user storage & lookup
│       └── resources
│           ├── haarcascade_frontalface_default.xml
│           ├── users.txt                      # User mapping (Name,ID[,Label])
│           └── trained_faces.xml              # Trained LBPH model
└── .gitignore

## ⚙️ Prerequisites

Before running this project, make sure you have the following installed:

- **Java JDK 11+** – Required to compile and run the application  
- **Apache Maven** – For build and dependency management  
- **A working webcam** – Needed for capturing and recognizing faces  
