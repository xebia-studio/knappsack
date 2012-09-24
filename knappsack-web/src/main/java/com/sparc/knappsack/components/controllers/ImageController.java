package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.services.AppFileService;
import com.sparc.knappsack.components.services.LocalStorageService;
import com.sparc.knappsack.components.services.StorageService;
import com.sparc.knappsack.components.services.StorageServiceFactory;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
public class ImageController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @RequestMapping(value = "/image/{id}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@PathVariable long id) {
        AppFile appFile = appFileService.get(id);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(appFile.getType()));

        StorageService storageService = storageServiceFactory.getStorageService(appFile.getStorageType());

        if (storageService instanceof LocalStorageService) {
            byte[] bytes = readImageFromFile(appFile);

            return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.CREATED);
        }

        return null;
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
}
