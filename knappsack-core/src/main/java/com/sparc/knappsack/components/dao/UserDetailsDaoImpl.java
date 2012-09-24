package com.sparc.knappsack.components.dao;

import com.mysema.query.types.path.StringPath;
import com.sparc.knappsack.components.entities.QUser;
import com.sparc.knappsack.components.entities.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userDetailsDao")
public class UserDetailsDaoImpl extends BaseDao implements UserDetailsDao {
    QUser userDetails = QUser.user;

    public void add(User user) {
        getEntityManager().persist(user);
    }

    public List<User> getAll() {
        return query().from(userDetails).listDistinct(userDetails);
    }

    public User get(Long id) {
        return getEntityManager().find(User.class, id);
    }

    public void delete(User user) {
        getEntityManager().remove(getEntityManager().merge(user));
    }

    public User findByOpenIdIdentifier(String openIdIdentifier) {
        StringPath stringPath = new StringPath("openId");
        return query().from(userDetails).where(userDetails.openIdIdentifiers.contains(openIdIdentifier)).uniqueResult(userDetails);
    }

    public User findByEmail(String email) {
        return query().from(userDetails).where(userDetails.email.equalsIgnoreCase(email)).uniqueResult(userDetails);
    }
    
    public User findByUserName(String userName) {
        return query().from(userDetails).where(userDetails.username.equalsIgnoreCase(userName)).uniqueResult(userDetails);
    }

    @Override
    public List<User> getBatch(List<Long> ids) {
        return query().from(userDetails).where(userDetails.id.in(ids)).listDistinct(userDetails);
    }

    public void update(User user) {
        getEntityManager().merge(user);
    }

    public long countAll() {
        return query().from(userDetails).count();
    }
    
}
