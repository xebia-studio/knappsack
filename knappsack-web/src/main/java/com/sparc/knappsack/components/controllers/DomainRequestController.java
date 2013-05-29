package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.comparators.LanguageComparator;
import com.sparc.knappsack.comparators.RegionNameComparator;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainRequest;
import com.sparc.knappsack.components.entities.Region;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.components.services.DomainRequestService;
import com.sparc.knappsack.components.services.DomainService;
import com.sparc.knappsack.components.services.RegionService;
import com.sparc.knappsack.components.validators.DomainRequestValidator;
import com.sparc.knappsack.enums.DeviceType;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.Language;
import com.sparc.knappsack.forms.DomainRequestForm;
import com.sparc.knappsack.models.RegionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping(value = "/auth")
public class DomainRequestController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(DomainRequestController.class);

    @Qualifier("domainService")
    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("domainRequestValidator")
    @Autowired(required = true)
    private DomainRequestValidator domainRequestValidator;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Qualifier("domainRequestService")
    @Autowired(required = true)
    private DomainRequestService domainRequestService;

    @Qualifier("regionService")
    @Autowired(required = true)
    private RegionService regionService;

    @InitBinder("domainRequestForm")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(domainRequestValidator);
    }

    @RequestMapping(value = "/request/{domainUUID}", method = RequestMethod.GET)
    public String showDomainRequestPage(Model model, @PathVariable String domainUUID, @RequestParam(value = "success", required = false) Boolean success) {
        Domain domain = domainService.getByUUID(domainUUID);

        if (domain != null) {
            if (!model.containsAttribute("domainRequestForm")) {
                DomainRequestForm domainRequestForm = new DomainRequestForm();
                domainRequestForm.setDomainUUID(domainUUID);

                model.addAttribute("domainRequestForm", domainRequestForm);
                model.addAttribute("domainName", domain.getName());
            }

            model.addAttribute("deviceTypes", DeviceType.getAlliPad());
            List<RegionModel> regions = new ArrayList<RegionModel>();

            //Sort regions alphabetically in ascending order
            List<Region> sortedRegions = new ArrayList<Region>(domain.getRegions());
            Collections.sort(sortedRegions, new RegionNameComparator());
            for (Region region : sortedRegions) {
                RegionModel regionModel = regionService.createRegionModel(region);
                if (regionModel != null) {
                    regions.add(regionModel);
                }
            }
            model.addAttribute("regions", regions);
            List<Language> languages = Arrays.asList(Language.values());
            Collections.sort(languages, new LanguageComparator());
            model.addAttribute("languages", languages);

            return "domain_public_requestTH";
        } else {
            return "redirect:/auth/register";
        }
    }

    @RequestMapping(value = "/request/process", method = RequestMethod.POST)
    public String createRequest(@ModelAttribute("domainRequestForm") @Validated DomainRequestForm domainRequestForm, BindingResult bindingResult, Model model, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.domainRequestForm", bindingResult);
            redirectAttributes.addFlashAttribute("domainRequestForm", domainRequestForm);
        } else {

            Domain domain = domainService.getByUUID(domainRequestForm.getDomainUUID());

            if (domain != null) {
                DomainRequest domainRequest = domainRequestService.createDomainRequest(domainRequestForm);

                if (domainRequest != null && domainRequest.getId() != null && domainRequest.getId() > 0) {
                    redirectAttributes.addFlashAttribute("success", true);

                    boolean emailsSent = false;
                    EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.DOMAIN_ACCESS_REQUEST);
                    if (deliveryMechanism != null) {
                        emailsSent = deliveryMechanism.sendNotifications(domainRequest);
                    }
                    if (!emailsSent) {
                        log.info("Error sending DomainAccessRequest email.", domainRequest);
                    }
                } else {
                    log.info(String.format("Unable to process public request for domain.  Entity not persisted to database. Domain UUID: %s", domain.getUuid()));

                    String[] codes = {"desktop.domain_public_request.generic.error"};
                    ObjectError error = new ObjectError("domainRequestForm", codes, null, null);
                    bindingResult.addError(error);

                    redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.domainRequestForm", bindingResult);
                    redirectAttributes.addFlashAttribute("domainRequestForm", domainRequestForm);
                }
            } else {
                log.info(String.format("Unable to process public request for domain.  Domain does not exist for UUID: %s", domainRequestForm.getDomainUUID()));

                return "redirect:/errorTH.html";
            }

        }

        return "redirect:/auth/request/" + domainRequestForm.getDomainUUID();
    }
}
