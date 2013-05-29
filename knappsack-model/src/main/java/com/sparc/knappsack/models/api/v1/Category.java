package com.sparc.knappsack.models.api.v1;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@ApiClass(value = "Category", description = "A category is a division of applications with a specific purpose.")
public class Category extends ParentModel {

    @ApiProperty(dataType = "long", required = true, notes = "The ID of this category")
    private Long id;
    @ApiProperty(dataType = "string", required = true, notes = "The name of this category")
    private String name;
    @ApiProperty(dataType = "string", required = false, notes = "The description of this category")
    private String description;
    @ApiProperty(dataType = "ImageModel", required = false, notes = "The icon image for this category")
    private ImageModel icon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ImageModel getIcon() {
        return icon;
    }

    public void setIcon(ImageModel icon) {
        this.icon = icon;
    }
}
