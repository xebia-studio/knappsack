package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.UserDomainDao;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("userDomainService")
public class UserDomainServiceImpl implements UserDomainService {

    @Qualifier("userDomainDao")
    @Autowired(required = true)
    private UserDomainDao userDomainDao;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("roleService")
    @Autowired(required = true)
    private RoleService roleService;

    @Override
    public UserDomain get(Long id) {
        return userDomainDao.get(id);
    }

    @Override
    public void delete(Long id) {
        userDomainDao.delete(get(id));
    }

    @Override
    public void update(UserDomain userDomain) {
        userDomainDao.update(userDomain);
    }

    @Override
    public void add(UserDomain userDomain) {
        userDomainDao.add(userDomain);
    }

    @Override
    public UserDomain get(User user, Long domainId, DomainType domainType, UserRole userRole) {
        return userDomainDao.getUserDomain(user, domainId, domainType, userRole);
    }

    @Override
    public UserDomain get(User user, Long domainId, DomainType domainType) {
        UserDomain userDomain = null;
        if (user != null && domainId != null && domainId > 0 && domainType != null) {
            userDomain = userDomainDao.getUserDomain(user, domainId, domainType);
        }
        return userDomain;
    }

    @Override
    public List<UserDomain> getAll(User user, DomainType domainType) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();

        if (user != null && domainType != null) {
            userDomains.addAll(userDomainDao.getUserDomains(user, domainType));
        }
        return userDomains;
    }

    @Override
    public List<UserDomain> getAll(User user, DomainType domainType, UserRole userRole) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();

        if (user != null && domainType != null && userRole != null) {
            List<UserDomain> allUserDomains = getAll(user, domainType);

            for (UserDomain userDomain : allUserDomains) {
                if (userDomain.getRole().getUserRole().equals(userRole) && !userDomains.contains(userDomain)) {
                    userDomains.add(userDomain);
                }
            }
        }

        return userDomains;
    }

    @Override
    public List<UserDomain> getAll(Long domainId, DomainType domainType) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();
        if (domainId != null && domainId > 0 && domainType != null) {
            userDomains.addAll(userDomainDao.getUserDomainsForDomain(domainId, domainType));
        }
        return userDomains;
    }

    @Override
    public List<UserDomain> getAll(Long domainId, DomainType domainType, UserRole userRole) {
        List<UserDomain> userDomains = new ArrayList<UserDomain>();

        if (userRole != null) {
            for (UserDomain userDomain : getAll(domainId, domainType)) {
                if (userRole.equals(userDomain.getRole().getUserRole()) && !userDomains.contains(userDomain)) {
                    userDomains.add(userDomain);
                }
            }
        }

        return userDomains;
    }

    @Override
    public void removeUserDomainFromDomain(Long domainId, DomainType domainType, Long userId) {
        if (domainId != null && domainId > 0 && domainType != null && userId != null && userId > 0) {
            User user = userService.get(userId);
            if (user != null && user.getUserDomains() != null) {
                UserDomain userDomain = userDomainDao.getUserDomain(user, domainId, domainType);
                if (user.getUserDomains().contains(userDomain)) {
                    user.getUserDomains().remove(userDomain);
                    userService.save(user);
                }

            }
        }
    }

    @Override
    public void updateUserDomainRole(Long userId, Long domainId, DomainType domainType, UserRole userRole) {
        User user = userService.get(userId);
        UserDomain userDomain = get(user, domainId, domainType);

        if (userDomain != null) {
            userDomain.setRole(roleService.getRoleByAuthority(userRole.toString()));

            update(userDomain);
        }
    }

    @Override
    public void removeAllFromDomain(Long domainId, DomainType domainType) {
        if (domainId != null && domainId > 0 && domainType != null) {
            userDomainDao.removeAllFromDomain(domainId, domainType);
        }
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
