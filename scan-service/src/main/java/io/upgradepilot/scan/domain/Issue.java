package io.upgradepilot.scan.domain;

import io.upgradepilot.scan.analysis.Finding;
import jakarta.persistence.*;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id")
    private Scan scan;

    @Column(nullable = false)
    private String ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String title;

    private String filePath;
    private Integer lineNumber;

    @Column(columnDefinition = "text")
    private String detail;

    /** AI explanation from rag-service; null if RAG was off or failed. */
    @Column(columnDefinition = "text")
    private String explanation;

    protected Issue() {
    }

    public Issue(Finding f, String explanation) {
        this.ruleId = f.ruleId();
        this.severity = f.severity();
        this.title = f.title();
        this.filePath = f.filePath();
        this.lineNumber = f.line();
        this.detail = f.detail();
        this.explanation = explanation;
    }

    void setScan(Scan scan) { this.scan = scan; }

    public Long getId() { return id; }
    public String getRuleId() { return ruleId; }
    public Severity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public Integer getLineNumber() { return lineNumber; }
    public String getDetail() { return detail; }
    public String getExplanation() { return explanation; }
}
