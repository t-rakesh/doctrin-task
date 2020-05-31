package com.doctrin.task.automation;

import java.io.*;
import java.util.ArrayList;

public class Util {

    public static File createOutputFile(String fileName) {
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            System.out.println("File " + fileName + " already exists.");
            outputFile.delete();
            System.out.println("File " + fileName + " deleted.");
        }
        return outputFile;
    }

    public static void writeToFile(String fileName, ArrayList<String> lines) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);;
        try {
            for (String line: lines) {
                fileWriter.write(line + "\n");
            }
        } finally {
            fileWriter.close();
        }
    }
}

class Employee {
    String name;
    String title;

    public Employee(String name, String title) {
        this.name = name;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Title: " + title;
    }
}