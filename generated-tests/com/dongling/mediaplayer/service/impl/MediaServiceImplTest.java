

package com.dongling.mediaplayer.service.impl;

import org.mockito.InjectMocks;

import org.junit.runner.RunWith;

import com.dongling.mediaplayer.enums.FileTypesEnum;

import org.junit.Test;

import static org.junit.Assert.*;

import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for MediaServiceImpl.
 * Generated from template configuration.
 */

@RunWith(MockitoJUnitRunner.class)

public class MediaServiceImplTest {

    @InjectMocks

    private MediaServiceImpl target;

    /**
     * 空路径时抛出 BizException
     */
    @Test
    public void testPlay_WithBlankPath_ShouldThrowBizException() throws Exception {

        assertThrows(com.dongling.mediaplayer.exception.BizException.class, () -> {
            target.play("", FileTypesEnum.MP3);
        });

    }

    /**
     * 路径不存在时抛出 BizException
     */
    @Test
    public void testPlay_WithMissingPath_ShouldThrowBizException() throws Exception {

        assertThrows(com.dongling.mediaplayer.exception.BizException.class, () -> {
            target.play("C:/Users/slt/evosuite/example_for_test/testdata/not-exists.mp3", FileTypesEnum.MP3);
        });

    }

    /**
     * MP4 文件按 MP3 播放时抛出 BizException
     */
    @Test
    public void testPlay_WithMP4ButExpectMP3_ShouldThrowBizException() throws Exception {

        assertThrows(com.dongling.mediaplayer.exception.BizException.class, () -> {
            target.play("C:/Users/slt/evosuite/example_for_test/testdata/test.mp4", FileTypesEnum.MP3);
        });

    }

    /**
     * MP3 文件按 MP4 播放时抛出 BizException
     */
    @Test
    public void testPlay_WithMP3ButExpectMP4_ShouldThrowBizException() throws Exception {

        assertThrows(com.dongling.mediaplayer.exception.BizException.class, () -> {
            target.play("C:/Users/slt/evosuite/example_for_test/testdata/test.mp3", FileTypesEnum.MP4);
        });

    }

    /**
     * 停止播放不抛异常
     */
    @Test
    public void testStop() throws Exception {

        target.stop();

    }

    /**
     * 销毁播放器不抛异常
     */
    @Test
    public void testDestroy() throws Exception {

        target.destroy();

    }

}