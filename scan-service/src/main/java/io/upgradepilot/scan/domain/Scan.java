package io.upgradepilot.scan.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scans")
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String projectName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status;

    private String javaVersion;
    private String springBootVersion;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Issue> issues = new ArrayList<>();

    protected Scan() {
    }

    public Scan(String projectName) {
        this.projectName = projectName;
        this.status = ScanStatus.RUNNING;
    }

    public void addIssue(Issue issue) {
        issue.setScan(this);
        issues.add(issue);
    }

    public void complete(String javaVersion, String springBootVersion) {
        this.javaVersion = javaVersion;
        this.springBootVersion = springBootVersion;
        this.status = ScanStatus.COMPLETED;
    }

    public void fail(String message) {
        this.status = ScanStatus.FAILED;
        this.errorMessage = message;
    }

    public UUID getId() { return id; }
    public String getProjectName() { return projectName; }
    public ScanStatus getStatus() { return status; }
    public String getJavaVersion() { return javaVersion; }
    public String getSpringBootVersion() { return springBootVersion; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Issue> getIssues() { return issues; }
}
