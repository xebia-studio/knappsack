package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.KeyVaultEntry;
import com.sparc.knappsack.components.entities.QKeyVaultEntry;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("keyVaultEntryDao")
public class KeyVaultEntryDaoImpl extends BaseDao implements KeyVaultEntryDao {
    QKeyVaultEntry keyVaultEntry = QKeyVaultEntry.keyVaultEntry;

    @Override
    public void add(KeyVaultEntry keyVaultEntry) {
        getEntityManager().persist(keyVaultEntry);
    }

    @Override
    public KeyVaultEntry get(Long id) {
        return getEntityManager().find(KeyVaultEntry.class, id);
    }

    @Override
    public void delete(KeyVaultEntry keyVaultEntry) {
        getEntityManager().remove(getEntityManager().merge(keyVaultEntry));
    }

    @Override
    public void update(KeyVaultEntry keyVaultEntry) {
        getEntityManager().merge(keyVaultEntry);
    }


    @Override
    public List<KeyVaultEntry> getAllForDomain(Domain domain) {
        return query().from(keyVaultEntry).where(keyVaultEntry.parentDomain.eq(domain)).list(keyVaultEntry);
    }

    @Override
    public long deleteAllForDomain(Domain domain) {
        return deleteClause(keyVaultEntry).where(keyVaultEntry.parentDomain.eq(domain)).execute();
    }
}
