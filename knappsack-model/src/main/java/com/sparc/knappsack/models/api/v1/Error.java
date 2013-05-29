package com.sparc.knappsack.models.api.v1;


import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@ApiClass(value = "Error", description = "Information pertaining to any errors that might have occurred during a request or processing.")
public class Error {

    @ApiProperty(dataType = "string", required = true, notes = "The error identification generated for this event.")
    private String errorId = "";
    @ApiProperty(dataType = "string", required = true, allowableValues = "200,400,403,500", notes = "The HTTP status code for the response")
    private String httpStatusCode = "200";
    @ApiProperty(dataType = "string", required = false, notes = "Description of the problem for the app developer.")
    private String developerMessage;
    @ApiProperty(dataType = "string", required = false, notes = "General message to pass on to the app user.")
    private String userMessage;
    @ApiProperty(dataType = "string", required = false, notes = "Additional information")
    private String moreInfo;

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }
}
