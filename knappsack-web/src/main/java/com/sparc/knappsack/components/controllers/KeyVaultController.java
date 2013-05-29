package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.components.services.KeyVaultEntryService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.components.validators.KeyVaultEntryValidator;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import com.sparc.knappsack.forms.EnumEditor;
import com.sparc.knappsack.forms.KeyVaultEntryForm;
import com.sparc.knappsack.forms.Result;
import com.sparc.knappsack.models.InternationalizedObject;
import com.sparc.knappsack.models.KeyVaultEntryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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

@Controller
public class KeyVaultController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(KeyVaultController.class);

    @Qualifier("keyVaultEntryValidator")
    @Autowired(required = true)
    private KeyVaultEntryValidator keyVaultEntryValidator;

    @Qualifier("keyVaultEntryService")
    @Autowired(required = true)
    private KeyVaultEntryService keyVaultEntryService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("messageSource")
    @Autowired(required = true)
    private MessageSource messageSource;

    @InitBinder("keyVaultEntryForm")
    public void initBinder(WebDataBinder binder, HttpServletRequest request) {
        binder.setValidator(keyVaultEntryValidator);

        binder.registerCustomEditor(DomainType.class, new EnumEditor(DomainType.class));
        binder.registerCustomEditor(ApplicationType.class, new EnumEditor(ApplicationType.class));
        binder.setBindEmptyMultipartFiles(false);
    }

    @PreAuthorize("isOrganizationAdminWithResignerEnabled() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getAllKeyVaultEntriesForUser", method = RequestMethod.GET)
    public @ResponseBody List<KeyVaultEntryModel> getAllKeyVaultEntriesForUser() {
        List<KeyVaultEntryModel> entries = new ArrayList<KeyVaultEntryModel>();

        for(KeyVaultEntry entity : keyVaultEntryService.getAllForUser(userService.getUserFromSecurityContext())) {
            entries.add(keyVaultEntryService.convertToModel(entity));
        }

        return entries;
    }

    @PreAuthorize("isDomainAdmin(#domainId) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/getAllKeyVaultEntriesForDomain", method = RequestMethod.GET)
    public @ResponseBody List<KeyVaultEntryModel> getAllKeyVaultEntriesForDomain(@RequestParam(required = true) Long domainId, @RequestParam(required = true) ApplicationType applicationType) {
        List<KeyVaultEntryModel> entries = new ArrayList<KeyVaultEntryModel>();
        for (KeyVaultEntry entry : keyVaultEntryService.getAllForDomainAndApplicationType(domainId, applicationType)) {
            entries.add(keyVaultEntryService.convertToModel(entry));
        }

        return entries;
    }

    @PreAuthorize("isOrganizationAdminWithResignerEnabled() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/viewKeyVault", method = RequestMethod.GET)
    public String viewKeyVault(HttpServletRequest requst, Model model) {
        List<InternationalizedObject> applicationTypes = new ArrayList<InternationalizedObject>();
        for (ApplicationType applicationType : ApplicationType.getAllKeyVaultCandidates()) {
            try {
                applicationTypes.add(new InternationalizedObject(applicationType, messageSource.getMessage(applicationType.getMessageKey(), null, requst.getLocale())));
            } catch (NoSuchMessageException ex) {
                log.error(String.format("No message for applicationType: %s", applicationType.name()), ex);

                // Put the applicationType name so that the application doesn't error out.
                applicationTypes.add(new InternationalizedObject(applicationType, applicationType.name()));
            }
        }
        model.addAttribute("applicationTypes", applicationTypes);

        if (!model.containsAttribute("keyVaultEntryForm")) {
            KeyVaultEntryForm keyVaultEntryForm = new KeyVaultEntryForm();
            model.addAttribute("keyVaultEntryForm", keyVaultEntryForm);
        }

        return "manager/manageKeyVaultTH";
    }

    @PreAuthorize("isOrganizationAdminWithResignerEnabled() or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/saveKeyVaultEntry", method = RequestMethod.POST)
    public String saveKeyVaultEntry(@ModelAttribute(value = "keyVaultEntryForm") @Validated KeyVaultEntryForm keyVaultEntryForm, BindingResult bindingResult, Model model, final RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.keyVaultEntryForm", bindingResult);
            redirectAttributes.addFlashAttribute("keyVaultEntryForm", keyVaultEntryForm);
        } else {

            KeyVaultEntry keyVaultEntry;
            if (keyVaultEntryForm.getId() == null || keyVaultEntryForm.getId() <= 0) {
                keyVaultEntry = keyVaultEntryService.createKeyVaultEntry(keyVaultEntryForm);
            } else {
                keyVaultEntry = keyVaultEntryService.editKeyVaultEntry(keyVaultEntryForm);
            }

            if (keyVaultEntry != null && keyVaultEntry.getId() != null && keyVaultEntry.getId() > 0) {
                redirectAttributes.addFlashAttribute("success", true);
            } else {
                log.info(String.format("Unable to save KeyVaultEntry for domain: %s", userService.getUserFromSecurityContext().getActiveOrganization().getId()));
                String[] codes = {"desktop.manager.keyVault.generic.error"};
                ObjectError error = new ObjectError("keyVaultEntryForm", codes, null, null);
                bindingResult.addError(error);

                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.keyVaultEntryForm", bindingResult);
                redirectAttributes.addFlashAttribute("keyVaultEntryForm", keyVaultEntryForm);
            }
        }

        return "redirect:/manager/viewKeyVault";
    }

    @PreAuthorize("canEditKeyVaultEntry(#id) or hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteKeyVaultEntry", method = RequestMethod.POST)
    public @ResponseBody
    Result deleteKeyVaultEntry(@RequestParam(value = "id", required = true) Long id) {
        Result result = new Result();
        try {
            checkRequiredEntity(keyVaultEntryService, id);
        } catch (EntityNotFoundException ex) {
            log.info(String.format("Attempted to delete a non-existent KeyVaultEntry: %s", id));
            result.setResult(false);
            return result;
        }

        keyVaultEntryService.delete(id);

        if (keyVaultEntryService.get(id) != null) {
            log.info(String.format("KeyVaultEntry not deleted: %s", id));
        } else {
            result.setResult(true);
        }

        return result;
    }

}
