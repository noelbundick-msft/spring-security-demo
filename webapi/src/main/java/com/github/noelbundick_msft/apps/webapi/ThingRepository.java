package com.github.noelbundick_msft.apps.webapi;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

// Deny access by default so we don't accidentally expose data that we didn't intend to
@PreAuthorize("hasRole('BOGUS')")
public interface ThingRepository extends JpaRepository<Thing, Long> {

  @Override
  @PreAuthorize("hasPermission('*', 'org', 'global_admin')")
  Page<Thing> findAll(Pageable pageable);

  @Override
  @PreAuthorize("hasPermission(#id, 'org', 'read')")
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
