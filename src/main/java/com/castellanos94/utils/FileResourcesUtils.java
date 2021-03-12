package com.castellanos94.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileResourcesUtils {

    public static void main(String[] args) throws IOException, URISyntaxException {

        FileResourcesUtils app = new FileResourcesUtils();

        // String fileName = "database.properties";
        int numberOfObjectives = 3;
        int p = 1;
        String fileName = "DTLZ_INSTANCES/" + numberOfObjectives + "/DTLZ" + p + "_Instance.txt";

        System.out.println("getResourceAsStream : " + fileName);
        InputStream is = app.getFileFromResourceAsStream(fileName);
       // printInputStream(is);
        System.out.println(convertToTmpFile(is).getAbsolutePath());

        System.out.println("\ngetResource : " + fileName);
        File file = app.getFileFromResource(fileName);
        printFile(file);

    }

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    public InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
     //   ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = FileResourcesUtils.class.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    /*
     * The resource URL is not working in the JAR If we try to access a file that is
     * inside a JAR, It throws NoSuchFileException (linux), InvalidPathException
     * (Windows)
     * 
     * Resource URL Sample: file:java-io.jar!/json/file1.json
     */
    private File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            // return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }

    // print input stream
    private static void printInputStream(InputStream is) {

        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // print a file
    private static void printFile(File file) {

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            lines.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static File convertToTmpFile(InputStream inputStream) throws IOException {

        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        File targetFile = File.createTempFile("data", ".temp");
        targetFile.deleteOnExit();
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();
        return targetFile;
    }
}