package com.sparc.knappsack.components.mapper;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.services.AppFileService;

import com.sparc.knappsack.models.api.v1.ImageModel;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("imageConverter")
public class ImageConverter extends CustomConverter<AppFile, ImageModel> {

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Override
    public ImageModel convert(AppFile source, Type<? extends ImageModel> destinationType) {
        com.sparc.knappsack.models.ImageModel model = appFileService.createImageModel(source);
        ImageModel imageModel = new ImageModel();
        imageModel.setId(model.getId());
        imageModel.setUrl(model.getUrl());
        return imageModel;
    }
}
