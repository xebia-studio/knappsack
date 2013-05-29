package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.KeyVaultEntry;

import java.util.List;

public interface KeyVaultEntryDao extends Dao<KeyVaultEntry> {

    List<KeyVaultEntry> getAllForDomain(Domain domain);

    long deleteAllForDomain(Domain domain);
}
