package com.sparc.knappsack.components.dao;

public interface Dao<Entity> {

    /**
     * @param entity - persist a new Entity object to the database
     * @see Entity
     */
    void add(Entity entity);

    /**
     * @param id Long - the primary key of the Entity
     * @return Entity with the given id
     * @see Entity
     */
    Entity get(Long id);

    /**
     * @param entity Entity - removes this entity from the cache and database
     * @see Entity
     */
    void delete(Entity entity);

    /**
     * @param entity Entity - merges changes to this Entity and persists the changes to the database
     * @see Entity
     */
    void update(Entity entity);
}
