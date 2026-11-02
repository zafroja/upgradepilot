import { useEffect, useState } from "react";
import { createScan, getScan, listScans } from "./api.js";

const SEVERITY_LABEL = { BLOCKER: "Blockers", WARNING: "Warnings", INFO: "Notes" };

export default function App() {
  const [file, setFile] = useState(null);
  const [projectName, setProjectName] = useState("");
  const [scan, setScan] = useState(null);
  const [recent, setRecent] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const refreshRecent = () => listScans().then(setRecent).catch(() => setRecent([]));

  useEffect(() => {
    refreshRecent();
  }, []);

  async function runScan() {
    if (!file) {
      setError("Choose a .zip file of your project first.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      const result = await createScan(file, projectName.trim());
      setScan(result);
      refreshRecent();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function openScan(id) {
    setError("");
    try {
      setScan(await getScan(id));
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="shell">
      <header className="topbar">
        <span className="brand">UpgradePilot</span>
        <span className="tagline">Java 17 and Spring Boot 3 readiness</span>
      </header>

      <div className="layout">
        <aside className="rail">
          <section className="panel">
            <h2>Scan a project</h2>
            <p className="hint">Upload a .zip of a Maven project. Source code is deleted after the scan.</p>

            <label className="field">
              <span>Project zip</span>
              <input type="file" accept=".zip" onChange={(e) => setFile(e.target.files[0] || null)} />
            </label>

            <label className="field">
              <span>Project name (optional)</span>
              <input
                type="text"
                value={projectName}
                placeholder="piggymetrics"
                onChange={(e) => setProjectName(e.target.value)}
              />
            </label>

            <button className="primary" onClick={runScan} disabled={busy}>
              {busy ? "Scanning…" : "Scan project"}
            </button>
            {busy && <p className="hint">Large projects with AI explanations can take up to a minute.</p>}
            {error && <p className="error" role="alert">{error}</p>}
          </section>

          <section className="panel">
            <h2>Recent scans</h2>
            {recent.length === 0 ? (
              <p className="hint">Scans you run will show up here.</p>
            ) : (
              <ul className="recent">
                {recent.map((s) => (
                  <li key={s.id}>
                    <button className={scan?.id === s.id ? "link active" : "link"} onClick={() => openScan(s.id)}>
                      <span>{s.projectName}</span>
                      <small>{new Date(s.createdAt).toLocaleString()}</small>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </aside>

        <main className="report">
          {scan ? <Report scan={scan} /> : (
            <div className="empty">
              <h1>Find out what stands between your project and Spring Boot 3.</h1>
              <p>Upload a project to get a list of upgrade blockers, the files they are in, and how to fix each one.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

function Report({ scan }) {
  if (scan.status === "FAILED") {
    return (
      <div className="empty">
        <h1>{scan.projectName}</h1>
        <p className="error">The scan failed: {scan.errorMessage || "unknown error"}. Check the zip and try again.</p>
      </div>
    );
  }

  const groups = ["BLOCKER", "WARNING", "INFO"]
    .map((sev) => ({ sev, items: scan.issues.filter((i) => i.severity === sev) }))
    .filter((g) => g.items.length > 0);

  return (
    <article>
      <h1 className="project">{scan.projectName}</h1>
      <dl className="versions">
        <div><dt>Java</dt><dd>{scan.javaVersion ?? "not declared"}</dd></div>
        <div><dt>Spring Boot</dt><dd>{scan.springBootVersion ?? "not detected"}</dd></div>
        {["BLOCKER", "WARNING", "INFO"].map((sev) => (
          <div key={sev} className={`count ${sev.toLowerCase()}`}>
            <dt>{SEVERITY_LABEL[sev]}</dt>
            <dd>{scan.counts[sev] ?? 0}</dd>
          </div>
        ))}
      </dl>

      {groups.length === 0 && (
        <p className="clean">No upgrade issues found. This project already meets the Java 17 and Spring Boot 3 baseline.</p>
      )}

      {groups.map(({ sev, items }) => (
        <section key={sev} className="group">
          <h2 className={`group-title ${sev.toLowerCase()}`}>{SEVERITY_LABEL[sev]}</h2>
          <ol className="issues">
            {items.map((issue, idx) => (
              <li key={idx} className={`issue ${sev.toLowerCase()}`}>
                <h3>{issue.title}</h3>
                {issue.filePath && (
                  <code className="path">{issue.filePath}{issue.line ? `:${issue.line}` : ""}</code>
                )}
                {issue.detail && <p>{issue.detail}</p>}
                {issue.explanation && (
                  <details>
                    <summary>How to fix it</summary>
                    <div className="explanation">{issue.explanation}</div>
                  </details>
                )}
              </li>
            ))}
          </ol>
        </section>
      ))}
    </article>
  );
}
