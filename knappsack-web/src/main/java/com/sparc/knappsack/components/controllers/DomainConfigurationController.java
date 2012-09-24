package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainConfiguration;
import com.sparc.knappsack.components.services.DomainConfigurationService;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.forms.DomainConfigurationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DomainConfigurationController extends AbstractController {

    @Autowired(required = true)
    private DomainService domainService;

    @Autowired(required = true)
    private DomainConfigurationService domainConfigurationService;

    @PreAuthorize("hasDomainConfigurationAccess(#domainId, #domainType)")
    @RequestMapping(value = "/manager/domainConfiguration/{domainId}/{domainType}", method = RequestMethod.GET)
    public String domainConfiguration(Model model, @PathVariable Long domainId, @PathVariable String domainType) {
        DomainType domainTypeValue = DomainType.valueOf(domainType);
        Domain domain = domainService.get(domainId, domainTypeValue);
        DomainConfiguration domainConfiguration = domain.getDomainConfiguration() == null ? new DomainConfiguration() : domain.getDomainConfiguration();
        DomainConfigurationForm domainConfigurationForm = getDomainConfigurationForm(domain.getId(), domain.getDomainType(), domainConfiguration);

        model.addAttribute("domain", domain);
        model.addAttribute("domainConfiguration", domainConfigurationForm);
        model.addAttribute("domainType", domain.getDomainType().name());
        setSideBarNavAttribute(model, domainTypeValue);

        return "manager/domainConfigurationTH";
    }

    @PreAuthorize("hasDomainConfigurationAccess(#domainConfigurationForm.domainId, #domainConfigurationForm.domainType)")
    @RequestMapping(value = "/manager/saveDomainConfiguration", method = RequestMethod.POST)
    public String saveDomainConfiguration(Model model, @ModelAttribute DomainConfigurationForm domainConfigurationForm) {
        Domain domain = domainService.get(domainConfigurationForm.getDomainId(), domainConfigurationForm.getDomainType());
        model.addAttribute("domain", domain);

        DomainConfiguration domainConfiguration = getDomainConfiguration(domain, domainConfigurationForm);
        domainConfigurationService.update(domainConfiguration);
        model.addAttribute("domainConfiguration", domainConfigurationForm);
        model.addAttribute("domainType", domain.getDomainType().name());
        model.addAttribute("success", true);
        setSideBarNavAttribute(model, domainConfigurationForm.getDomainType());

        return "manager/domainConfigurationTH";
    }

    private DomainConfigurationForm getDomainConfigurationForm(Long domainId, DomainType domainType, DomainConfiguration domainConfiguration) {
        DomainConfigurationForm domainConfigurationForm = new DomainConfigurationForm();
        domainConfigurationForm.setApplicationLimit(domainConfiguration.getApplicationLimit());
        domainConfigurationForm.setApplicationVersionLimit(domainConfiguration.getApplicationVersionLimit());
        domainConfigurationForm.setDisabled(domainConfiguration.isDisabledDomain());
        domainConfigurationForm.setDisableLimitValidations(domainConfiguration.isDisableLimitValidations());
        domainConfigurationForm.setDomainId(domainId);
        domainConfigurationForm.setDomainType(domainType);
        domainConfigurationForm.setId(domainConfiguration.getId());
        domainConfigurationForm.setMegabyteStorageLimit(domainConfiguration.getMegabyteStorageLimit());
        domainConfigurationForm.setUserLimit(domainConfiguration.getUserLimit());

        return domainConfigurationForm;
    }

    private DomainConfiguration getDomainConfiguration(Domain domain, DomainConfigurationForm domainConfigurationForm) {
        DomainConfiguration domainConfiguration = domain.getDomainConfiguration() == null ? new DomainConfiguration() : domain.getDomainConfiguration();
        domainConfiguration.setApplicationLimit(domainConfigurationForm.getApplicationLimit());
        domainConfiguration.setApplicationVersionLimit(domainConfigurationForm.getApplicationVersionLimit());
        domainConfiguration.setDisabledDomain(domainConfigurationForm.isDisabled());
        domainConfiguration.setDisableLimitValidations(domainConfigurationForm.isDisableLimitValidations());
        domainConfiguration.setMegabyteStorageLimit(domainConfigurationForm.getMegabyteStorageLimit());
        domainConfiguration.setUserLimit(domainConfigurationForm.getUserLimit());

        return domainConfiguration;
    }

    private void setSideBarNavAttribute(Model model, DomainType domainType) {
        if (DomainType.GROUP.equals(domainType)) {
            model.addAttribute("subNav", "groupNav");
        } else if (DomainType.ORGANIZATION.equals(domainType)) {
            model.addAttribute("subNav", "organizationNav");
        }
    }
}
