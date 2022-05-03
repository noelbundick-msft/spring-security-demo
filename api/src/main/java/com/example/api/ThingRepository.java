package com.example.api;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

// Allow only system access by default so we don't accidentally expose data that we didn't intend to
@PreAuthorize("hasRole('SYSTEM')")
public interface ThingRepository extends JpaRepository<Thing, Long> {

  @Override
  @PreAuthorize("hasPermission('*', 'thing', 'read')")
  Page<Thing> findAll(Pageable pageable);

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
