package com.sparc.knappsack.components.validators;

import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.forms.GroupForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component("groupValidator")
public class GroupValidator implements Validator {

    private static final String NAME_FIELD = "name";

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

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
            User user = userService.getUserFromSecurityContext();
            if (user == null) {
                errors.reject("groupValidator.generic");
                return;
            }

            Organization organization = user.getActiveOrganization();
            if (organization == null) {
                errors.reject("groupValidator.generic");
            }

            Group group = groupService.get(groupForm.getName(), organization.getId());
            if (group != null) {
                errors.rejectValue(NAME_FIELD, "groupValidator.groupNameExistsInOrganization");
            }
        }
    }
}
