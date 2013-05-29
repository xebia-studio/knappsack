package com.sparc.knappsack.components.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Component("imageValidator")
public class ImageValidator {
    private static final Logger log = LoggerFactory.getLogger(ImageValidator.class);
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_JPG = "image/jpg";
    private static final String IMAGE_JPEG = "image/jpeg";

    public BufferedImage createBufferedImage(MultipartFile multipartFile) {
        if (multipartFile == null) {
            log.info("Attempted to create buffered image from null MultipartFile");
            return null;
        }

        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(multipartFile.getInputStream());
        } catch (IOException e) {
            log.error("IOException caught validating application icon.", e);
            return null;
        }

        if (bufferedImage == null) {
            log.error("Unable to create BufferedImage from multipartFile");
            return null;
        }

        return bufferedImage;
    }

    public boolean isValidMinDimensions(BufferedImage image, long minWidth, long minHeight) {
        if (image != null) {
            return image.getWidth() >= minWidth && image.getHeight() >= minHeight;

        }
        return false;
    }

    public boolean isValidMaxDimensions(BufferedImage image, long maxWidth, long maxHeight) {
        if (image != null) {
            return image.getWidth() <= maxWidth && image.getHeight() <= maxHeight;
        }

        return false;
    }

    public boolean isSquare(BufferedImage image) {
        if (image != null) {
            return image.getWidth() == image.getHeight();
        }
        return true;
    }

    public boolean isValidImageSize(MultipartFile multipartFile, long bytes) {
        return multipartFile == null || multipartFile.getSize() <= bytes;
    }

    public boolean isValidImageType(MultipartFile multipartFile) {
        if (multipartFile != null) {
            String contentType = multipartFile.getContentType();
            return contentType != null && (IMAGE_PNG.equals(contentType) || IMAGE_JPG.equals(contentType) || IMAGE_JPEG.equals(contentType));
        }
        return true;
    }
}
