(() => {
  document.documentElement.classList.add("js-enhanced");

  const landingRoot = document.querySelector(".landing-page");
  if (!landingRoot) {
    return;
  }

  const sectionIds = [
    "problem-solution",
    "case-lifecycle",
    "role-based-access",
    "documents-reliability",
    "audit-trust",
    "architecture-quality",
  ];

  const navLinks = Array.from(document.querySelectorAll(".landing-nav-link"));
  const sections = sectionIds
    .map((id) => document.getElementById(id))
    .filter((section) => section !== null);
  const progressBar = document.getElementById("landingProgressBar");
  const revealTargets = Array.from(document.querySelectorAll(".js-reveal"));
  const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // Smooth scroll between module anchors for presentation flow.
  navLinks.forEach((link) => {
    link.addEventListener("click", (event) => {
      const href = link.getAttribute("href");
      if (!href || !href.startsWith("#")) {
        return;
      }
      const target = document.querySelector(href);
      if (!target) {
        return;
      }

      event.preventDefault();
      target.scrollIntoView({ behavior: prefersReducedMotion ? "auto" : "smooth", block: "start" });
      history.replaceState(null, "", href);
    });
  });

  const updateActiveLink = (activeId) => {
    navLinks.forEach((link) => {
      const isActive = link.getAttribute("href") === `#${activeId}`;
      link.classList.toggle("active", isActive);
      if (isActive) {
        link.setAttribute("aria-current", "true");
      } else {
        link.removeAttribute("aria-current");
      }
    });
  };

  const sectionObserver = new IntersectionObserver(
    (entries) => {
      let topVisible = null;
      for (const entry of entries) {
        if (!entry.isIntersecting) {
          continue;
        }
        if (!topVisible || entry.boundingClientRect.top < topVisible.boundingClientRect.top) {
          topVisible = entry;
        }
      }
      if (topVisible && topVisible.target.id) {
        updateActiveLink(topVisible.target.id);
      }
    },
    { threshold: 0.45, rootMargin: "-10% 0px -45% 0px" }
  );

  sections.forEach((section) => sectionObserver.observe(section));

  const updateProgress = () => {
    if (!progressBar) {
      return;
    }
    const scrollTop = window.scrollY || window.pageYOffset;
    const documentHeight = document.documentElement.scrollHeight - window.innerHeight;
    const progress = documentHeight <= 0 ? 100 : Math.min(100, Math.max(0, (scrollTop / documentHeight) * 100));
    progressBar.style.width = `${progress}%`;
  };

  window.addEventListener("scroll", updateProgress, { passive: true });
  window.addEventListener("resize", updateProgress);
  updateProgress();

  if (prefersReducedMotion) {
    revealTargets.forEach((node) => node.classList.add("revealed"));
  } else {
    const revealObserver = new IntersectionObserver(
      (entries, observer) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            return;
          }
          entry.target.classList.add("revealed");
          observer.unobserve(entry.target);
        });
      },
      { threshold: 0.18 }
    );

    revealTargets.forEach((node) => revealObserver.observe(node));
  }

  const finalCta = document.getElementById("final-cta");
  const primaryCta = finalCta ? finalCta.querySelector(".button-strong") : null;
  if (finalCta && primaryCta && !prefersReducedMotion) {
    const ctaObserver = new IntersectionObserver(
      (entries, observer) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            return;
          }
          primaryCta.classList.add("cta-attention");
          window.setTimeout(() => primaryCta.classList.remove("cta-attention"), 1200);
          observer.unobserve(entry.target);
        });
      },
      { threshold: 0.45 }
    );
    ctaObserver.observe(finalCta);
  }

  const tabButtons = Array.from(document.querySelectorAll(".segmented-tabs__btn"));
  tabButtons.forEach((button) => {
    button.addEventListener("click", () => {
      const targetSelector = button.getAttribute("data-target");
      if (!targetSelector) return;

      tabButtons.forEach((btn) => {
        const selected = btn === button;
        btn.classList.toggle("is-active", selected);
        btn.setAttribute("aria-selected", String(selected));
      });

      document.querySelectorAll(".tab-panel").forEach((panel) => {
        panel.hidden = true;
        panel.classList.remove("is-active");
      });

      const target = document.querySelector(targetSelector);
      if (target) {
        target.hidden = false;
        target.classList.add("is-active");
      }
    });
  });

  const flowData = {
    create: {
      title: "🆕 Skapa ärende",
      chips: ["🧑 Patientkontext", "🏁 Startstatus", "🧭 Tydlig start"],
    },
    assign: {
      title: "👤 Tilldela ansvar",
      chips: ["👑 Ägare", "🛠️ Handläggare", "🎯 Klart ansvar"],
    },
    update: {
      title: "📝 Uppdatera och kommunicera",
      chips: ["🗒️ Anteckningar", "📍 Status", "🤝 Samarbete"],
    },
    close: {
      title: "✅ Avsluta med spårbarhet",
      chips: ["🏁 Avslut", "📚 Historik", "📈 Uppföljning"],
    },
  };

  const flowDetail = document.getElementById("flowDetail");
  const renderFlow = (key) => {
    const item = flowData[key];
    if (!flowDetail || !item) return;
    flowDetail.innerHTML = `
      <h3>${item.title}</h3>
      <div class="metrics-row">
        ${item.chips.map((chip) => `<span class="metric-chip">${chip}</span>`).join("")}
      </div>
    `;
  };

  const flowSteps = Array.from(document.querySelectorAll(".flow-step"));
  flowSteps.forEach((button) => {
    button.addEventListener("click", () => {
      flowSteps.forEach((btn) => btn.classList.remove("is-active"));
      button.classList.add("is-active");
      renderFlow(button.getAttribute("data-step"));
    });
  });
  renderFlow("create");

  const roleData = {
    manager: {
      allow: ["👀 Se alla ärenden", "🧩 Hantera tilldelning", "📜 Granska loggar"],
      deny: ["🚫 Ingen patientimitation"],
    },
    doctor: {
      allow: ["✏️ Uppdatera tilldelade", "🗒️ Lägga anteckningar", "📁 Hantera dokument"],
      deny: ["🚫 Ingen användaradministration"],
    },
    nurse: {
      allow: ["👀 Se handlagda ärenden", "🗒️ Lägga anteckningar"],
      deny: ["🚫 Ingen full adminbehörighet"],
    },
    patient: {
      allow: ["📲 Följa egna ärenden", "💬 Lägga kommunikationsnotis"],
      deny: ["🚫 Ingen åtkomst till andras ärenden"],
    },
  };

  const rolePanel = document.getElementById("rolePanel");
  const renderRole = (roleKey) => {
    const role = roleData[roleKey];
    if (!role || !rolePanel) return;
    rolePanel.innerHTML = `
      <div class="permission-chips">
        ${role.allow.map((item) => `<span class="metric-chip permission-chip--allow">${item}</span>`).join("")}
        ${role.deny.map((item) => `<span class="metric-chip permission-chip--deny">${item}</span>`).join("")}
      </div>
    `;
  };

  const roleButtons = Array.from(document.querySelectorAll(".role-tabs__btn"));
  roleButtons.forEach((button) => {
    button.addEventListener("click", () => {
      roleButtons.forEach((btn) => btn.classList.remove("is-active"));
      button.classList.add("is-active");
      renderRole(button.getAttribute("data-role"));
    });
  });
  renderRole("manager");

  document.querySelectorAll("[data-accordion]").forEach((accordion) => {
    const triggers = Array.from(accordion.querySelectorAll(".accordion__trigger"));
    triggers.forEach((trigger) => {
      trigger.addEventListener("click", () => {
        const expanded = trigger.getAttribute("aria-expanded") === "true";
        triggers.forEach((btn) => {
          btn.setAttribute("aria-expanded", "false");
          const panel = document.getElementById(btn.getAttribute("aria-controls") || "");
          if (panel) panel.hidden = true;
        });
        if (!expanded) {
          trigger.setAttribute("aria-expanded", "true");
          const panel = document.getElementById(trigger.getAttribute("aria-controls") || "");
          if (panel) panel.hidden = false;
        }
      });
    });
  });
})();
