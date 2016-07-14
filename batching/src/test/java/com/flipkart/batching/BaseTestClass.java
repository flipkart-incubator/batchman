/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.batching;

import java.io.File;

/**
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
        if (fList != null) {
            for (File testFile : fList) {
                if (testFile.getName().endsWith(TEST_FILE_SUFFIX)) {
                    testFile.delete();
                }
            }
        }
        folder.delete();
    }
}
