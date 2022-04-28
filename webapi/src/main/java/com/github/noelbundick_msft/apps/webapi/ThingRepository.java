package com.github.noelbundick_msft.apps.webapi;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ThingRepository extends JpaRepository<Thing, Long> {

  @Override
  @PreAuthorize("hasPermission(#id, 'thing', 'read')")
  Optional<Thing> findById(Long id);

  @Override
  @PreAuthorize("hasPermission(#entity, 'write')")
  <S extends Thing> S save(S entity);

  @Override
  @PreAuthorize("hasPermission(#id, 'thing', 'write')")
  void deleteById(Long id);

  @Override
  @PreAuthorize("hasPermission(#entity, 'write')")
  void delete(Thing entity);

}
