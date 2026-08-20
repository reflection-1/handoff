const elements = {
  totalMetric: document.querySelector("#totalMetric"),
  newMetric: document.querySelector("#newMetric"),
  highMetric: document.querySelector("#highMetric"),
  doneMetric: document.querySelector("#doneMetric"),
  resultCount: document.querySelector("#resultCount"),
  queryFilter: document.querySelector("#queryFilter"),
  priorityFilter: document.querySelector("#priorityFilter"),
  shiftFilter: document.querySelector("#shiftFilter"),
  newItems: document.querySelector("#newItems"),
  ackItems: document.querySelector("#ackItems"),
  doneItems: document.querySelector("#doneItems"),
  newCount: document.querySelector("#newCount"),
  ackCount: document.querySelector("#ackCount"),
  doneCount: document.querySelector("#doneCount"),
  newHandoffButton: document.querySelector("#newHandoffButton"),
  handoffDialog: document.querySelector("#handoffDialog"),
  handoffForm: document.querySelector("#handoffForm"),
  formMessage: document.querySelector("#formMessage"),
  board: document.querySelector("#handoffBoard"),
  historyDialog: document.querySelector("#historyDialog"),
  historyTitle: document.querySelector("#historyTitle"),
  historyItems: document.querySelector("#historyItems"),
};

const escapeHtml = (value) => String(value ?? "").replace(/[&<>'"]/g, (character) => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;",
})[character]);

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    throw new Error("the java server returned an unexpected response. make sure the app is running through maven.");
  }
  const body = await response.json();
  if (!response.ok) {
    const fieldMessage = body.fieldErrors && Object.values(body.fieldErrors)[0];
    throw new Error(fieldMessage || body.message || "something went wrong");
  }
  return body;
}

function humanize(value) {
  return String(value).toLowerCase().replaceAll("_", " ");
}

function formatDate(value) {
  return new Intl.DateTimeFormat("en-CA", {
    month: "short", day: "numeric", hour: "numeric", minute: "2-digit",
  }).format(new Date(value));
}

function renderCard(item) {
  const nextAction = item.status === "NEW"
    ? '<button class="card-action" data-action="ACKNOWLEDGED">acknowledge</button>'
    : item.status === "ACKNOWLEDGED"
      ? '<button class="card-action" data-action="DONE">mark done</button>'
      : "";

  return `
    <article class="handoff-card" data-id="${item.id}">
      <div class="card-topline">
        <span class="priority priority-${item.priority.toLowerCase()}">${humanize(item.priority)}</span>
        <span>${humanize(item.shiftType)} shift</span>
      </div>
      <h4>${escapeHtml(item.title)}</h4>
      <p>${escapeHtml(item.details)}</p>
      <dl>
        <div><dt>area</dt><dd>${escapeHtml(item.area)}</dd></div>
        <div><dt>owner</dt><dd>${escapeHtml(item.owner)}</dd></div>
      </dl>
      <div class="card-footer">
        <button class="history-button" data-history="true">view history</button>
        ${nextAction}
      </div>
    </article>`;
}

function renderColumn(items, container, count) {
  count.textContent = items.length;
  container.innerHTML = items.length
    ? items.map(renderCard).join("")
    : '<p class="empty-state">nothing here right now.</p>';
}

async function loadMetrics() {
  const metrics = await fetchJson("/api/metrics");
  elements.totalMetric.textContent = metrics.total;
  elements.newMetric.textContent = metrics.newItems;
  elements.highMetric.textContent = metrics.highPriorityOpen;
  elements.doneMetric.textContent = metrics.done;
}

async function loadHandoffs() {
  const params = new URLSearchParams();
  if (elements.queryFilter.value.trim()) params.set("query", elements.queryFilter.value.trim());
  if (elements.priorityFilter.value) params.set("priority", elements.priorityFilter.value);
  if (elements.shiftFilter.value) params.set("shiftType", elements.shiftFilter.value);

  const handoffs = await fetchJson(`/api/handoffs?${params.toString()}`);
  elements.resultCount.textContent = `${handoffs.length} note${handoffs.length === 1 ? "" : "s"}`;
  renderColumn(handoffs.filter((item) => item.status === "NEW"), elements.newItems, elements.newCount);
  renderColumn(handoffs.filter((item) => item.status === "ACKNOWLEDGED"), elements.ackItems, elements.ackCount);
  renderColumn(handoffs.filter((item) => item.status === "DONE"), elements.doneItems, elements.doneCount);
}

async function refresh() {
  await Promise.all([loadMetrics(), loadHandoffs()]);
}

elements.newHandoffButton.addEventListener("click", () => elements.handoffDialog.showModal());
document.querySelectorAll("[data-close-dialog]").forEach((button) => {
  button.addEventListener("click", () => document.querySelector(`#${button.dataset.closeDialog}`).close());
});

elements.handoffForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const submitButton = elements.handoffForm.querySelector('[type="submit"]');
  submitButton.disabled = true;
  elements.formMessage.textContent = "saving…";
  try {
    await fetchJson("/api/handoffs", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(Object.fromEntries(new FormData(elements.handoffForm))),
    });
    elements.handoffForm.reset();
    elements.formMessage.textContent = "";
    elements.handoffDialog.close();
    await refresh();
  } catch (error) {
    elements.formMessage.textContent = error.message;
  } finally {
    submitButton.disabled = false;
  }
});

elements.board.addEventListener("click", async (event) => {
  const card = event.target.closest(".handoff-card");
  if (!card) return;

  if (event.target.matches("[data-action]")) {
    event.target.disabled = true;
    try {
      await fetchJson(`/api/handoffs/${card.dataset.id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: event.target.dataset.action }),
      });
      await refresh();
    } catch (error) {
      window.alert(error.message);
      event.target.disabled = false;
    }
  }

  if (event.target.matches("[data-history]")) {
    elements.historyTitle.textContent = card.querySelector("h4").textContent;
    elements.historyItems.innerHTML = '<p class="empty-state">loading history…</p>';
    elements.historyDialog.showModal();
    try {
      const history = await fetchJson(`/api/handoffs/${card.dataset.id}/history`);
      elements.historyItems.innerHTML = history.map((item) => `
        <article class="history-item">
          <span>${formatDate(item.occurredAt)}</span>
          <strong>${humanize(item.eventType)}</strong>
          <p>${escapeHtml(item.note)}</p>
        </article>`).join("");
    } catch (error) {
      elements.historyItems.innerHTML = `<p class="form-message">${escapeHtml(error.message)}</p>`;
    }
  }
});

let searchTimer;
elements.queryFilter.addEventListener("input", () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(loadHandoffs, 180);
});
elements.priorityFilter.addEventListener("change", loadHandoffs);
elements.shiftFilter.addEventListener("change", loadHandoffs);

refresh().catch((error) => {
  elements.resultCount.textContent = error.message;
});
