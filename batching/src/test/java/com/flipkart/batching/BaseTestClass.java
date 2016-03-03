package com.flipkart.batching;

import java.io.File;

/**
 * Created by anirudh.r on 26/02/16.
 * Base Test Class, contains methods used by other tests
 */
public class BaseTestClass {
    private static final String TEST_FILE_SUFFIX = ".tmp";
    private static final String TEST_FILE_DIR = "test_files";

    /**
     * Create a random string
     *
     * @return string
     */
    public String createRandomString() {
        double random = Math.random();
        return "Random" + random;
    }

    /**
     * Create a file with random name
     *
     * @return file
     */
    public File createRandomFile() {
        String randomFile = createRandomString() + TEST_FILE_SUFFIX;
        return createTestFile(randomFile);
    }

    public File createTestFile(String fileName) {
        File folder = new File(TEST_FILE_DIR);
        folder.mkdirs();
        File file = new File(TEST_FILE_DIR, fileName);
        if (file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    /**
     * Delete test files
     */
    public void deleteRandomFiles() {
        File folder = new File(TEST_FILE_DIR);
        File fList[] = folder.listFiles();
        if(fList != null) {
            for (File testFile : fList) {
                if (testFile.getName().endsWith(TEST_FILE_SUFFIX)) {
                    testFile.delete();
                }
            }
        }
        folder.delete();
    }
}
