package io.upgradepilot.scan.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanRepository extends JpaRepository<Scan, UUID> {

    @EntityGraph(attributePaths = "issues")
    Optional<Scan> findWithIssuesById(UUID id);

    List<Scan> findTop20ByOrderByCreatedAtDesc();
}
