package com.sparc.knappsack.components.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ImageIO.class, LoggerFactory.class})
public class ImageValidatorTest {

    private ImageValidator validator;

    @Mock private MockMultipartFile mockMultipartFile;

    @Before
    public void setup() {
        mockStatic(LoggerFactory.class);
        Logger logger = mock(Logger.class);
        when(LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);

        validator = new ImageValidator();
        mockStatic(ImageIO.class);
    }



    @Test
    public void testValidIconDimension() {
        BufferedImage bufferedImage = mock(BufferedImage.class);

        try {
            when(ImageIO.read((InputStream) any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        when(bufferedImage.getWidth()).thenReturn(72);
        when(bufferedImage.getHeight()).thenReturn(72);

        assertTrue(validator.isValidMinDimensions(bufferedImage, 72, 72));
    }

    @Test
    public void testInValidIconDimension() {
        BufferedImage bufferedImage = mock(BufferedImage.class);

        try {
            when(ImageIO.read((InputStream) any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        when(bufferedImage.getWidth()).thenReturn(20);
        when(bufferedImage.getHeight()).thenReturn(20);

        assertFalse(validator.isValidMinDimensions(bufferedImage, 72, 72));

        setup();

        assertFalse(validator.isValidMinDimensions(bufferedImage, 72, 72));

        setup();

        try {
            when(ImageIO.read((InputStream) any())).thenThrow(new IOException());
        } catch (IOException e) {
            fail();
        }

        assertFalse(validator.isValidMinDimensions(bufferedImage, 72, 72));

        setup();

        try {
            when(ImageIO.read((InputStream) any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        when(bufferedImage.getWidth()).thenReturn(72);
        when(bufferedImage.getHeight()).thenReturn(20);

        assertFalse(validator.isValidMinDimensions(bufferedImage, 72, 72));
    }

    @Test
    public void testNonSquareIcon() {
        BufferedImage bufferedImage = mock(BufferedImage.class);

        try {
            when(ImageIO.read((InputStream) any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        when(bufferedImage.getWidth()).thenReturn(72);
        when(bufferedImage.getHeight()).thenReturn(89);

        assertFalse(validator.isSquare(bufferedImage));
    }

    @Test
    public void testNullMultipartFiles() {
        assertFalse(validator.isValidMinDimensions(null, 71, 71));
        assertTrue(validator.isValidImageSize(null, 819200));
        assertTrue(validator.isValidImageType(null));
    }

    @Test
    public void testInvalidImageSize() {
        when(mockMultipartFile.getSize()).thenReturn(999999L);
        assertFalse(validator.isValidImageSize(mockMultipartFile, 819200));
    }

    @Test
    public void testValidImageSize() {
        when(mockMultipartFile.getSize()).thenReturn(100L);
        assertTrue(validator.isValidImageSize(mockMultipartFile, 819200));
    }

    @Test
    public void testInvalidImageType() {
        assertFalse(validator.isValidImageType(mockMultipartFile));

        when(mockMultipartFile.getContentType()).thenReturn("invalid");
        assertFalse(validator.isValidImageType(mockMultipartFile));
    }

    @Test
    public void testValidImageType() {
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        assertTrue(validator.isValidImageType(mockMultipartFile));

        when(mockMultipartFile.getContentType()).thenReturn("image/jpg");
        assertTrue(validator.isValidImageType(mockMultipartFile));

        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        assertTrue(validator.isValidImageType(mockMultipartFile));
    }
}
