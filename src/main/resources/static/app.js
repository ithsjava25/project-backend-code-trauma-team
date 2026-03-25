(() => {
  const STORAGE_KEY = "case-ui-auth";

  function loadAuth() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  function saveAuth(auth) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  }

  function ensureDefaultAuth() {
    const existing = loadAuth();
    if (existing?.userId && existing?.role) return existing;
    const defaultAuth = {
      userId: crypto.randomUUID(),
      role: "ADMIN",
    };
    saveAuth(defaultAuth);
    return defaultAuth;
  }

  function setAuthInputs(auth) {
    const userId = document.getElementById("authUserId");
    const role = document.getElementById("authRole");
    if (userId) userId.value = auth.userId ?? "";
    if (role) role.value = auth.role ?? "ADMIN";
  }

  function bindAuthBar() {
    const btn = document.getElementById("authSave");
    if (!btn) return;

    btn.addEventListener("click", () => {
      const userId = document.getElementById("authUserId")?.value?.trim();
      const role = document.getElementById("authRole")?.value?.trim();

      if (!userId) {
        alert("X-User-Id is required (UUID).");
        return;
      }
      if (!role) {
        alert("X-Role is required.");
        return;
      }

      saveAuth({ userId, role });
      alert("Saved headers for API requests.");
    });
  }

  async function apiFetch(path, init = {}) {
    const auth = ensureDefaultAuth();

    const headers = new Headers(init.headers || {});
    headers.set("X-User-Id", auth.userId);
    headers.set("X-Role", auth.role);

    return fetch(path, { ...init, headers });
  }

  function bindCreateCaseForm() {
    const form = document.getElementById("createCaseForm");
    if (!form) return;

    const result = document.getElementById("createCaseResult");
    const createdCaseId = document.getElementById("createdCaseId");
    const openCaseLink = document.getElementById("openCaseLink");

    const errorPanel = document.getElementById("createCaseError");
    const errorText = document.getElementById("createCaseErrorText");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (result) result.hidden = true;
      if (errorPanel) errorPanel.hidden = true;

      const fd = new FormData(form);
      const payload = {
        title: String(fd.get("title") || ""),
        description: String(fd.get("description") || ""),
      };

      try {
        const res = await apiFetch("/cases", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        if (!res.ok) {
          const body = await res.text();
          throw new Error(`HTTP ${res.status}\n${body}`);
        }

        const json = await res.json();
        const caseId = json.caseId;

        if (createdCaseId) createdCaseId.textContent = caseId;
        if (openCaseLink) openCaseLink.href = `/ui/cases/${encodeURIComponent(caseId)}`;
        if (result) result.hidden = false;
      } catch (err) {
        if (errorText) errorText.textContent = String(err?.message || err);
        if (errorPanel) errorPanel.hidden = false;
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    const auth = ensureDefaultAuth();
    setAuthInputs(auth);
    bindAuthBar();
    bindCreateCaseForm();
    window.caseUi = { apiFetch };
  });
})();

