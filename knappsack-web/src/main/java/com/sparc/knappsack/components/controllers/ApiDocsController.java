package com.sparc.knappsack.components.controllers;

import com.knappsack.swagger4springweb.controller.ApiDocumentationController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/api")
public class ApiDocsController extends ApiDocumentationController {

    private static final String CONTROLLER_PKG = "com.sparc.knappsack.components.controllers.api.v1";
    private static final String MODEL_PKG = "com.sparc.knappsack.models.api.v1";
    private static final String API_VERSION = "v1";

    public ApiDocsController() {
        setBaseControllerPackage(CONTROLLER_PKG);
        setBaseModelPackage(MODEL_PKG);
        setApiVersion(API_VERSION);
    }

    @RequestMapping(value = "/v1/docs", method = RequestMethod.GET)
    public String getDocs() {
        return "api/docIndexTH";
    }
}
