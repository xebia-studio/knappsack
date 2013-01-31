package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainConfiguration;
import com.sparc.knappsack.components.services.DomainConfigurationService;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.DomainConfigurationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DomainConfigurationController extends AbstractController {

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("domainConfigurationService")
    @Autowired(required = true)
    private DomainConfigurationService domainConfigurationService;

    @PreAuthorize("hasDomainConfigurationAccess(#domainId)")
    @RequestMapping(value = "/manager/domainConfiguration/{domainId}", method = RequestMethod.GET)
    public String domainConfiguration(Model model, @PathVariable Long domainId) {
        Domain domain = domainService.get(domainId);
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while viewing DomainConfiguration page: %s", domainId));
        }
        DomainConfiguration domainConfiguration = domain.getDomainConfiguration() == null ? new DomainConfiguration() : domain.getDomainConfiguration();
        DomainConfigurationForm domainConfigurationForm = getDomainConfigurationForm(domain.getId(), domainConfiguration);

        model.addAttribute("domain", domain);
        model.addAttribute("domainConfiguration", domainConfigurationForm);
        model.addAttribute("domainType", domain.getDomainType().name());
        setSideBarNavAttribute(model, domain.getDomainType());

        return "manager/domainConfigurationTH";
    }

    @PreAuthorize("hasDomainConfigurationAccess(#domainConfigurationForm.domainId)")
    @RequestMapping(value = "/manager/saveDomainConfiguration", method = RequestMethod.POST)
    public String saveDomainConfiguration(Model model, @ModelAttribute DomainConfigurationForm domainConfigurationForm) {
        Domain domain = domainService.get(domainConfigurationForm.getDomainId());
        if (domain == null) {
            throw new EntityNotFoundException(String.format("Domain entity not found while saving DomainConfiguration: %s", domainConfigurationForm.getDomainId()));
        }
        model.addAttribute("domain", domain);

        DomainConfiguration domainConfiguration = getDomainConfiguration(domain, domainConfigurationForm);
        domainConfigurationService.update(domainConfiguration);
        model.addAttribute("domainConfiguration", domainConfigurationForm);
        model.addAttribute("domainType", domain.getDomainType().name());
        model.addAttribute("success", true);
        setSideBarNavAttribute(model, domain.getDomainType());

        return "manager/domainConfigurationTH";
    }

    private DomainConfigurationForm getDomainConfigurationForm(Long domainId, DomainConfiguration domainConfiguration) {
        DomainConfigurationForm domainConfigurationForm = new DomainConfigurationForm();
        domainConfigurationForm.setMonitorBandwidth(domainConfiguration.isMonitorBandwidth());
        domainConfigurationForm.setMegabyteBandwidthLimit(domainConfiguration.getMegabyteBandwidthLimit());
        domainConfigurationForm.setApplicationLimit(domainConfiguration.getApplicationLimit());
        domainConfigurationForm.setApplicationVersionLimit(domainConfiguration.getApplicationVersionLimit());
        domainConfigurationForm.setDisabled(domainConfiguration.isDisabledDomain());
        domainConfigurationForm.setDisableLimitValidations(domainConfiguration.isDisableLimitValidations());
        domainConfigurationForm.setDomainId(domainId);
        domainConfigurationForm.setId(domainConfiguration.getId());
        domainConfigurationForm.setMegabyteStorageLimit(domainConfiguration.getMegabyteStorageLimit());
        domainConfigurationForm.setUserLimit(domainConfiguration.getUserLimit());

        return domainConfigurationForm;
    }

    private DomainConfiguration getDomainConfiguration(Domain domain, DomainConfigurationForm domainConfigurationForm) {
        DomainConfiguration domainConfiguration = domain.getDomainConfiguration() == null ? new DomainConfiguration() : domain.getDomainConfiguration();
        domainConfiguration.setMonitorBandwidth(domainConfigurationForm.isMonitorBandwidth());
        //First check to see if the new bandwidth limit amount is higher than the existing limit.  If it is reset the bandwidth limit reached flag to false.
        if(domainConfigurationForm.getMegabyteBandwidthLimit() > domainConfiguration.getMegabyteBandwidthLimit()) {
            domainConfiguration.setBandwidthLimitReached(false);
        }
        domainConfiguration.setMegabyteBandwidthLimit(domainConfigurationForm.getMegabyteBandwidthLimit());
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
