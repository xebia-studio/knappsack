package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.QRole;
import com.sparc.knappsack.components.entities.Role;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleDaoImpl extends BaseDao implements RoleDao {
    QRole role = QRole.role;

    @Override
    public void add(Role role) {
        getEntityManager().persist(role);
    }

    @Override
    public Role get(Long id) {
        return getEntityManager().find(Role.class, id);
    }

    @Override
    public void delete(Role role) {
        getEntityManager().remove(getEntityManager().merge(role));
    }

    @Override
    public void update(Role role) {
        getEntityManager().merge(role);
    }

    public List<Role> getAll() {
        return query().from(role).distinct().list(role);
    }

    public Role getByAuthority(String authority) {
        return query().from(role).where(role.authority.eq(authority)).uniqueResult(role);
    }
    
}
