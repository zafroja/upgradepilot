async function handle(res) {
  if (res.ok) return res.json();
  let message = `Request failed (${res.status})`;
  try {
    const body = await res.json();
    if (body.message || body.detail) message = body.message || body.detail;
  } catch {
    /* non-JSON error body */
  }
  throw new Error(message);
}

export function listScans() {
  return fetch("/api/scans").then(handle);
}

export function getScan(id) {
  return fetch(`/api/scans/${id}`).then(handle);
}

export function createScan(file, projectName) {
  const body = new FormData();
  body.append("file", file);
  if (projectName) body.append("projectName", projectName);
  return fetch("/api/scans", { method: "POST", body }).then(handle);
}
