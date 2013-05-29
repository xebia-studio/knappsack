package com.sparc.knappsack.components.mapper;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.services.AppFileService;
import com.sparc.knappsack.models.api.v1.ImageModel;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("imageCollectionConverter")
public class ImageCollectionConverter extends CustomConverter<Collection<AppFile>, Collection<ImageModel>> {

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Override
    public Collection<ImageModel> convert(Collection<AppFile> source, Type<? extends Collection<ImageModel>> destinationType) {
        List<ImageModel> imageModels = new ArrayList<ImageModel>();
        if(source == null) {
            return imageModels;
        }

        for (AppFile appFile : source) {
            com.sparc.knappsack.models.ImageModel model = appFileService.createImageModel(appFile);
            ImageModel imageModel = new ImageModel();
            imageModel.setId(model.getId());
            imageModel.setUrl(model.getUrl());
            imageModels.add(imageModel);
        }

        return imageModels;
    }
}
