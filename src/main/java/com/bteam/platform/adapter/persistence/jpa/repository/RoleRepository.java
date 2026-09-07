package com.bteam.platform.adapter.persistence.jpa.repository;

import com.bteam.platform.adapter.persistence.jpa.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    List<RoleEntity> findByNameIn(Collection<String> names);
    boolean existsByName(String name);
}
