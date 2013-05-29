package com.sparc.knappsack.components.mapper;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.CustomBranding;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.models.api.v1.ApplicationVersion;
import com.sparc.knappsack.models.api.v1.Organization;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

@Component("mapper")
public class CustomMapperFacadeImpl extends ConfigurableMapper implements Mapper {

    private static final Logger log = LoggerFactory.getLogger(CustomMapperFacadeImpl.class);

    @Qualifier("imageConverter")
    @Autowired(required = true)
    private ImageConverter imageConverter;

    @Qualifier("imageCollectionConverter")
    @Autowired(required = true)
    private ImageCollectionConverter imageCollectionConverter;

    @PostConstruct
    private void registerMaps() {
        Field privateFactory;
        try {
            privateFactory = ConfigurableMapper.class.getDeclaredField("factory");
        } catch (NoSuchFieldException e) {
            log.error("NoSuchFieldException caught trying to get the DefaultMapperFactory from ConfigurableMapper", e);
            return;
        }

        privateFactory.setAccessible(true);
        DefaultMapperFactory factory;
        try {
            factory = (DefaultMapperFactory) privateFactory.get(this);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException caught trying to get the DefaultMapperFactory from ConfigurableMapper", e);
            return;
        }

        factory.getConverterFactory().registerConverter("imageConverter", imageConverter);
        factory.getConverterFactory().registerConverter("imageCollectionConverter", imageCollectionConverter);
        registerApplication(factory);
        registerCategory(factory);
        registerOrganization(factory);
        registerApplicationVersion(factory);
        registerGroup(factory);
        registerBranding(factory);
    }

    private void registerOrganization(MapperFactory factory) {
        factory.registerClassMap(factory.classMap(Organization.class, com.sparc.knappsack.models.api.v1.Organization.class).byDefault().toClassMap());
    }

    private void registerApplication(MapperFactory factory) {
        factory.registerClassMap(factory.classMap(Application.class, com.sparc.knappsack.models.api.v1.Application.class)
                .fieldMap("icon").converter("imageConverter").add()
                .fieldMap("screenshots", "screenShots").converter("imageCollectionConverter").add()
                .field("ownedGroup.id", "groupId")
                .field("category.id", "categoryId")
                .byDefault()
                .toClassMap());
    }

    private void registerApplicationVersion(MapperFactory factory) {
        factory.registerClassMap(factory.classMap(ApplicationVersion.class, com.sparc.knappsack.models.api.v1.ApplicationVersion.class).byDefault().toClassMap());
    }

    private void registerCategory(MapperFactory factory) {
        factory.registerClassMap(factory.classMap(Category.class, com.sparc.knappsack.models.api.v1.Category.class)
                .fieldMap("icon").converter("imageConverter").add()
                .byDefault().toClassMap());
    }

    private void registerGroup(MapperFactory factory) {
        factory.registerClassMap(factory.classMap(Group.class, com.sparc.knappsack.models.api.v1.Group.class).byDefault().toClassMap());
    }

    private void registerBranding(MapperFactory factory) {
        factory.registerClassMap(factory.classMap(CustomBranding.class, com.sparc.knappsack.models.api.v1.Branding.class)
                .fieldMap("logo").converter("imageConverter").add()
                .byDefault().toClassMap());
    }
}
