package com.sparc.knappsack.models;

public class CategoryModel {

    private Long id;
    private String name;
    private String description;
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
