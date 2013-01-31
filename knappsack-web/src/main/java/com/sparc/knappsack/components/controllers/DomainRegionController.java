package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.components.services.RegionService;
import com.sparc.knappsack.components.validators.DomainRegionValidator;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.CommaDelimitedStringEditor;
import com.sparc.knappsack.forms.DomainRegionForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.RegionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/manager/regions")
public class DomainRegionController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(DomainRegionController.class);

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("regionService")
    @Autowired(required = true)
    private RegionService regionService;

    @Qualifier("domainRegionValidator")
    @Autowired(required = true)
    private DomainRegionValidator domainRegionValidator;

    @InitBinder("domainRegionForm")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(domainRegionValidator);

        binder.registerCustomEditor(Set.class, new CommaDelimitedStringEditor(true));
    }

    @PreAuthorize("isDomainAdmin(#domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{domainId}", method = RequestMethod.GET)
    public String viewDomainRegionsPage(HttpServletRequest request, Model model, @PathVariable Long domainId) {
        Domain domain = domainService.get(domainId);

        if (domain != null) {

            if (!model.containsAttribute("domainRegionForm")) {
                DomainRegionForm domainRegionForm = new DomainRegionForm();
                domainRegionForm.setDomainId(domainId);

                model.addAttribute("domainRegionForm", domainRegionForm);
            }

            model.addAttribute("domainId", domainId);
            model.addAttribute("domainName", domain.getName());
            model.addAttribute("domainType", domain.getDomainType());

            if (domain != null) {
                setSideBarNavAttribute(model, domain.getDomainType());
            }
        } else {
            log.info(String.format("Domain not found with id: %s", domainId));
            throw new EntityNotFoundException(String.format("Domain not found with id: %s", domainId));
        }

        return "manager/manageDomainRegionsTH";
    }

    @PreAuthorize("isDomainAdmin(#domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/getAllForDomain", method = RequestMethod.GET)
    public @ResponseBody
    List<RegionModel> getAllForDomain(@RequestParam(value = "id", required = true) Long domainId) {
        List<RegionModel> models = new ArrayList<RegionModel>();

        Domain domain = domainService.get(domainId);

        if (domain != null) {
            for (Region region : domain.getRegions()) {
                RegionModel model = regionService.createRegionModel(region);

                if (model != null) {
                    models.add(model);
                }
            }
        }

        return models;
    }

    @PreAuthorize("isDomainAdmin(#domainRegionForm.domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/saveDomainRegion", method = RequestMethod.POST)
    public String saveDomainRegion(@ModelAttribute(value = "domainRegionForm") @Validated DomainRegionForm domainRegionForm, BindingResult bindingResult, Model model, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.domainRegionForm", bindingResult);
            redirectAttributes.addFlashAttribute("domainRegionForm", domainRegionForm);
        } else {

            Region region;
            if (domainRegionForm.getId() == null || domainRegionForm.getId() <= 0) {
                region = regionService.createRegion(domainRegionForm);
            } else {
                region = regionService.editRegion(domainRegionForm);
            }

            if (region != null && region.getId() != null && region.getId() > 0) {
                redirectAttributes.addFlashAttribute("updateSuccess", true);
            } else {
                log.info(String.format("Unable to save Region for domain: Domain Id: %s", domainRegionForm.getDomainId()));
                String[] codes = {"desktop.manager.domainRegions.generic.error"};
                ObjectError error = new ObjectError("domainRegionForm", codes, null, null);
                bindingResult.addError(error);

                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.domainRegionForm", bindingResult);
                redirectAttributes.addFlashAttribute("domainRegionForm", domainRegionForm);
            }
        }


        return "redirect:/manager/regions/" + domainRegionForm.getDomainId();
    }

    @PreAuthorize("canEditDomainRegion(#domainRegionId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/deleteDomainRegion", method = RequestMethod.POST)
    public @ResponseBody
    Result deleteDomainRegion(@RequestParam(value = "id", required = true) Long domainRegionId) {
        Result result = new Result();
        try {
            checkRequiredEntity(regionService, domainRegionId);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete a non-existent DomainRegion: %s", domainRegionId));
            result.setResult(false);
            return result;
        }

        regionService.delete(domainRegionId);

        if (regionService.get(domainRegionId) != null) {
            log.info(String.format("Region not deleted: %s", domainRegionId));
        } else {
            result.setResult(true);
        }

        return result;
    }

    private void setSideBarNavAttribute(Model model, DomainType domainType) {
        if (DomainType.GROUP.equals(domainType)) {
            model.addAttribute("subNav", "groupNav");
        } else if (DomainType.ORGANIZATION.equals(domainType)) {
            model.addAttribute("subNav", "organizationNav");
        }
    }

}
