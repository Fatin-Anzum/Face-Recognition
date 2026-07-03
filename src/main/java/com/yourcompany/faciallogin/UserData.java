package com.yourcompany.faciallogin;

import java.io.IOException;
import java.io.*;
import java.util.*;


interface UserData {
    void addUser(Users user) throws IOException;
    Users findUser(String name, int userId) throws IOException;
}

class FileUserData implements UserData {
    private static final String filePath = "E:/CSE215/Project/facial-login-project-main/users.txt";

    @Override
    public void addUser(Users user) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(user.getName() + "," + user.getUserId() + "," + user.getLabel());
            bw.newLine();
        }
    }

    @Override
    public Users findUser(String name, int userId) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String storedName = parts[0];
                    int storedId = Integer.parseInt(parts[1]);
                    int storedLabel = Integer.parseInt(parts[2]);

                    if (storedName.equalsIgnoreCase(name) && storedId == userId) {
                        return new Users(storedName, storedId, storedLabel);
                    }
                }
            }
        }
        return null;
    }
}