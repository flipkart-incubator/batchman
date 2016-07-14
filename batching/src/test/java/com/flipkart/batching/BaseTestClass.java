/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
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
