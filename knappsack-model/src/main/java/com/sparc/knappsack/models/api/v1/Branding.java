package com.sparc.knappsack.models.api.v1;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@ApiClass(value = "Branding", description = "Organization specific labeling")
public class Branding extends ParentModel {

    @ApiProperty(dataType = "ImageModel", required = false, notes = "The image logo of your organization")
    private ImageModel logo;
    private String emailHeader;
    private String emailFooter;

    public ImageModel getLogo() {
        return logo;
    }

    public void setLogo(ImageModel logo) {
        this.logo = logo;
    }

    public String getEmailHeader() {
        return emailHeader;
    }

    public void setEmailHeader(String emailHeader) {
        this.emailHeader = emailHeader;
    }

    public String getEmailFooter() {
        return emailFooter;
    }

    public void setEmailFooter(String emailFooter) {
        this.emailFooter = emailFooter;
    }
}
