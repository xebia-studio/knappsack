package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.forms.GroupForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("groupValidator")
public class GroupValidator implements Validator {

    private static final String NAME_FIELD = "name";
    @Autowired(required = true)
    private GroupService groupService;

    @Override
    public boolean supports(Class<?> aClass) {
        return GroupForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        GroupForm groupForm = (GroupForm) o;
        if (groupForm.getName() == null || "".equals(groupForm.getName())) {
            errors.rejectValue(NAME_FIELD, "groupValidator.emptyName");
        }

        if (!errors.hasFieldErrors(NAME_FIELD)) {
            Group group = groupService.get(groupForm.getName(), groupForm.getOrganizationId());
            if (group != null) {
                errors.rejectValue(NAME_FIELD, "groupValidator.groupNameExistsInOrganization");
            }
        }
    }
}
