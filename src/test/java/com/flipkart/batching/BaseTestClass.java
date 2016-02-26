package com.flipkart.batching;

import java.io.File;

/**
 * Created by anirudh.r on 26/02/16.
 */
public class BaseTestClass {
    private static final String TEST_FILE_SUFFIX = ".tmp";
    private static final String TEST_FILE_DIR = "test_files";

    public String createRandomString() {
        double random = Math.random();
        return "Random" + random;
    }

    public File createRandomFile() {
        String randomFile = createRandomString() + TEST_FILE_SUFFIX;
        File folder = new File(TEST_FILE_DIR);
        folder.mkdirs();
        File file = new File(TEST_FILE_DIR, randomFile);
        if (file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public void deleteRandomFiles() {
        File folder = new File(TEST_FILE_DIR);
        File fList[] = folder.listFiles();
        for (int i = 0; i < fList.length; i++) {
            File testFile = fList[i];
            if (testFile.getName().endsWith(TEST_FILE_SUFFIX)) {
                testFile.delete();
            }
        }
        folder.delete();
    }
}
