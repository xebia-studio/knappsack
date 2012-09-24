package com.sparc.knappsack.components.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ImageIO.class)
public class ImageValidatorTest {

    private ImageValidator validator;

    @Mock private MockMultipartFile mockMultipartFile;

    @Before
    public void setup() {
        validator = new ImageValidator();
        PowerMockito.mockStatic(ImageIO.class);
    }

    @Test
    public void testValidIconDimension() {
        BufferedImage bufferedImage = Mockito.mock(BufferedImage.class);

        try {
            Mockito.when(ImageIO.read((InputStream) Matchers.any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        Mockito.when(bufferedImage.getWidth()).thenReturn(72);
        Mockito.when(bufferedImage.getHeight()).thenReturn(72);

        assertTrue(validator.isValidIconDimension(mockMultipartFile));
    }

    @Test
    public void testInValidIconDimension() {
        BufferedImage bufferedImage = Mockito.mock(BufferedImage.class);

        try {
            Mockito.when(ImageIO.read((InputStream) Matchers.any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        Mockito.when(bufferedImage.getWidth()).thenReturn(20);
        Mockito.when(bufferedImage.getHeight()).thenReturn(20);

        assertFalse(validator.isValidIconDimension(mockMultipartFile));

        setup();

        assertFalse(validator.isValidIconDimension(mockMultipartFile));

        setup();

        try {
            Mockito.when(ImageIO.read((InputStream) Matchers.any())).thenThrow(new IOException());
        } catch (IOException e) {
            fail();
        }

        assertFalse(validator.isValidIconDimension(mockMultipartFile));

        setup();

        try {
            Mockito.when(ImageIO.read((InputStream) Matchers.any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        Mockito.when(bufferedImage.getWidth()).thenReturn(72);
        Mockito.when(bufferedImage.getHeight()).thenReturn(20);

        assertFalse(validator.isValidIconDimension(mockMultipartFile));
    }

    @Test
    public void testNonSquareIcon() {
        BufferedImage bufferedImage = Mockito.mock(BufferedImage.class);

        try {
            Mockito.when(ImageIO.read((InputStream) Matchers.any())).thenReturn(bufferedImage);
        } catch (IOException e) {
            fail();
        }

        Mockito.when(bufferedImage.getWidth()).thenReturn(72);
        Mockito.when(bufferedImage.getHeight()).thenReturn(89);

        assertFalse(validator.isValidIconDimension(mockMultipartFile));
    }

    @Test
    public void testNullMultipartFiles() {
        assertTrue(validator.isValidIconDimension(null));
        assertTrue(validator.isValidImageSize(null));
        assertTrue(validator.isValidImageType(null));
    }

    @Test
    public void testInvalidImageSize() {
        Mockito.when(mockMultipartFile.getSize()).thenReturn(999999L);
        assertFalse(validator.isValidImageSize(mockMultipartFile));
    }

    @Test
    public void testValidImageSize() {
        Mockito.when(mockMultipartFile.getSize()).thenReturn(100L);
        assertTrue(validator.isValidImageSize(mockMultipartFile));
    }

    @Test
    public void testInvalidImageType() {
        assertFalse(validator.isValidImageType(mockMultipartFile));

        Mockito.when(mockMultipartFile.getContentType()).thenReturn("invalid");
        assertFalse(validator.isValidImageType(mockMultipartFile));
    }

    @Test
    public void testValidImageType() {
        Mockito.when(mockMultipartFile.getContentType()).thenReturn("image/png");
        assertTrue(validator.isValidImageType(mockMultipartFile));

        Mockito.when(mockMultipartFile.getContentType()).thenReturn("image/jpg");
        assertTrue(validator.isValidImageType(mockMultipartFile));

        Mockito.when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        assertTrue(validator.isValidImageType(mockMultipartFile));
    }
}
