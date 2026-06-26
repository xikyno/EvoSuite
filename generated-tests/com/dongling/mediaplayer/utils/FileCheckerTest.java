

package com.dongling.mediaplayer.utils;

import org.junit.Assert.*;

import org.junit.Test;

import java.io.File;

/**
 * Test class for FileChecker.
 * Generated from template configuration.
 */
public class FileCheckerTest {

    /**
     * 测试正常 MP3 文件
     */

    @Test

    public void testIsMP3File() throws Exception {
        // Arrange — Mock dependencies

        // Arrange — Create SUT and call methods

        boolean result = isMP3File("new java.io.File(\"C:/Users/slt/evosuite/example_for_test/testdata/test.mp3\")");

        // Assert

        assertEquals(true, result);

    }

    /**
     * 测试正常 MP4 文件
     */

    @Test

    public void testIsMP4File() throws Exception {
        // Arrange — Mock dependencies

        // Arrange — Create SUT and call methods

        boolean result = isMP4File("new java.io.File(\"C:/Users/slt/evosuite/example_for_test/testdata/test.mp4\")");

        // Assert

        assertEquals(true, result);

    }

}