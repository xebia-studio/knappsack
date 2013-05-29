package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.services.AppFileService;
import com.sparc.knappsack.components.services.RemoteStorageService;
import com.sparc.knappsack.components.services.StorageService;
import com.sparc.knappsack.components.services.StorageServiceFactory;
import com.sparc.knappsack.enums.ContentType;
import com.sparc.knappsack.enums.MimeType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Controller
public class ImageController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    @RequestMapping(value = "/image/{id}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@PathVariable long id, HttpServletRequest request, HttpServletResponse response) {
        checkRequiredEntity(appFileService, id);
        AppFile appFile = appFileService.get(id);

        if (!ContentType.IMAGE.equals(MimeType.getForFilename(appFile.getName()).getContentType())) {
            throw new RuntimeException(String.format("Attempted to pull file which wasn't an image: %s", id));
        }

        response.setHeader("Cache-Control", String.format("public, max-age=%s", 31556900 /*Seconds in a year*/));
        response.setDateHeader("Last-Modified", appFile.getLastUpdate().getTime());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appFile.getLastUpdate());
        calendar.add(Calendar.YEAR, 1);
        response.setHeader("Expires", httpDateFormat.format(calendar.getTime()));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(appFile.getType()));

        StorageService storageService = storageServiceFactory.getStorageService(appFile.getStorageType());

        long requestIfModifiedSince = request.getDateHeader("If-Modified-Since");
        if (requestIfModifiedSince >= appFile.getLastUpdate().getTime()) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
        }

        if (storageService instanceof RemoteStorageService) {
            byte[] bytes = readImageFromUrl(appFile, (RemoteStorageService) storageService);

            return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.CREATED);
        } else {
            byte[] bytes = readImageFromFile(appFile);

            return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.CREATED);
        }
    }

    private byte[] readImageFromFile(AppFile appFile) {
        String absolutePath = appFile.getAbsolutePath();
        File file = new File(absolutePath);

        try {
            return IOUtils.toByteArray(new FileInputStream(file));
        } catch (IOException e) {
            log.error("Error reading image from file for image: " + appFile.getRelativePath(), e);
        }
        return new byte[0];
    }

    private byte[] readImageFromUrl(AppFile appFile, RemoteStorageService storageService) {
        String urlString = storageService.getUrl(appFile, 60);
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            log.error("Error instantiating URL from String: " + urlString, e);
            return new byte[0];
        }

        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException e) {
            log.error("Error reading image from URL for image: " + urlString, e);
            return new byte[0];
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", byteArrayOutputStream);
        } catch (IOException e) {
            log.error("Error writing image to byte stream: " + appFile.getRelativePath(), e);
            return new byte[0];
        }

        try {
            byteArrayOutputStream.flush();
        } catch (IOException e) {
            log.error("Error flushing byte stream for image: " + appFile.getRelativePath(), e);
            return new byte[0];
        }

        byte[] imageInByte = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            log.error("Error closing byte stream for image: " + appFile.getRelativePath(), e);
            return new byte[0];
        }

        return imageInByte;
    }
}
