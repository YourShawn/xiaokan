package com.xiaokan.repo;

import com.xiaokan.domain.Scan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    @EntityGraph(attributePaths = "photos")
    List<Scan> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "photos")
    Optional<Scan> findById(Long id);
}
