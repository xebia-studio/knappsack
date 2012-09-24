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

    public boolean isValidIconDimension(MultipartFile multipartFile) {
        if (multipartFile != null) {
            BufferedImage iconImage;
            try {
                iconImage = ImageIO.read(multipartFile.getInputStream());
                if (iconImage == null) {
                    return false;
                }
            } catch (IOException e) {
                log.error("IOException caught validating application icon.", e);
                return false;
            }

            return (iconImage.getWidth() >= 72 && iconImage.getHeight() >= 72) && iconImage.getWidth() == iconImage.getHeight();
        }
        return true;
    }

    public boolean isValidImageSize(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.getSize() <= 819200;
    }

    public boolean isValidImageType(MultipartFile multipartFile) {
        if (multipartFile != null) {
            String contentType = multipartFile.getContentType();
            return contentType != null && (IMAGE_PNG.equals(contentType) || IMAGE_JPG.equals(contentType) || IMAGE_JPEG.equals(contentType));
        }
        return true;
    }
}
