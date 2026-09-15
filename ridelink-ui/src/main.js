const API = {
  account: "http://localhost:8081",
  driver: "http://localhost:8082",
  ride: "http://localhost:8083",
  fare: "http://localhost:8084",
};

const state = {
  mode: "login",
  token: localStorage.getItem("ridelink_token") || "",
  user: JSON.parse(localStorage.getItem("ridelink_user") || "null"),
  message: "",
  messageType: "ok",
  estimate: null,
  rides: [],
  driverProfile: null,
  payment: null,
};

const app = document.querySelector("#app");

async function api(base, path, { method = "GET", body, token } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const auth = token ?? state.token;
  if (auth) headers.Authorization = `Bearer ${auth}`;

  const res = await fetch(`${base}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = { message: text };
  }

  if (!res.ok) {
    const err = new Error(data?.message || data?.error || `Request failed (${res.status})`);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

function setSession(token, user) {
  state.token = token;
  state.user = user;
  localStorage.setItem("ridelink_token", token);
  localStorage.setItem("ridelink_user", JSON.stringify(user));
}

function clearSession() {
  state.token = "";
  state.user = null;
  state.rides = [];
  state.driverProfile = null;
  state.estimate = null;
  state.payment = null;
  localStorage.removeItem("ridelink_token");
  localStorage.removeItem("ridelink_user");
}

function flash(message, type = "ok") {
  state.message = message;
  state.messageType = type;
  render();
}

function msgHtml() {
  if (!state.message) return "";
  return `<div class="msg show ${state.messageType}">${escapeHtml(state.message)}</div>`;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function renderGate() {
  const isLogin = state.mode === "login";
  app.innerHTML = `
    <section class="gate">
      <div class="gate-visual">
        <h1 class="brand">RideLink</h1>
        <p class="brand-sub">Book a ride, assign a driver, and settle fare — demo UI for the IT3130 microservices backend.</p>
      </div>
      <div class="gate-panel">
        <div class="panel">
          <h2>${isLogin ? "Welcome back" : "Create account"}</h2>
          <p class="hint">${isLogin ? "Sign in with your passenger or driver account." : "Register as passenger or driver to try the full flow."}</p>
          <div class="tabs">
            <button class="tab ${isLogin ? "active" : ""}" data-mode="login">Login</button>
            <button class="tab ${!isLogin ? "active" : ""}" data-mode="register">Register</button>
          </div>
          <form id="auth-form">
            ${
              !isLogin
                ? `
              <label>Full name
                <input name="fullName" required placeholder="Ayesha Perera" />
              </label>
              <label>Phone
                <input name="phone" placeholder="0771111111" />
              </label>
              <label>Role
                <select name="role">
                  <option value="PASSENGER">Passenger</option>
                  <option value="DRIVER">Driver</option>
                </select>
              </label>`
                : ""
            }
            <label>Email
              <input name="email" type="email" required placeholder="ayesha@ridelink.lk" />
            </label>
            <label>Password
              <input name="password" type="password" required minlength="6" placeholder="pass123" />
            </label>
            <button class="btn btn-primary" type="submit">${isLogin ? "Sign in" : "Create account"}</button>
          </form>
          ${msgHtml()}
        </div>
      </div>
    </section>
  `;

  app.querySelectorAll(".tab").forEach((btn) => {
    btn.addEventListener("click", () => {
      state.mode = btn.dataset.mode;
      state.message = "";
      render();
    });
  });

  app.querySelector("#auth-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);
    const payload = Object.fromEntries(form.entries());
    try {
      const path = isLogin ? "/api/auth/login" : "/api/auth/register";
      const data = await api(API.account, path, { method: "POST", body: payload, token: "" });
      setSession(data.accessToken, data.user);
      flash(`Signed in as ${data.user.fullName} (${data.user.role})`);
      await bootstrapWorkspace();
    } catch (err) {
      flash(err.message, "error");
    }
  });
}

function renderShell(content) {
  const user = state.user;
  app.innerHTML = `
    <div class="shell">
      <header class="topbar">
        <strong>RideLink</strong>
        <div class="user-meta">
          <span class="chip">${escapeHtml(user.role)}</span>
          <span>${escapeHtml(user.fullName)}</span>
          <button class="btn btn-ghost" id="logout">Sign out</button>
        </div>
      </header>
      <main class="workspace">${content}${msgHtml()}</main>
    </div>
  `;
  app.querySelector("#logout").addEventListener("click", () => {
    clearSession();
    state.mode = "login";
    state.message = "";
    render();
  });
}

function passengerView() {
  const estimate = state.estimate
    ? `<div class="fare-box"><div class="muted">Estimated fare</div><strong>LKR ${escapeHtml(state.estimate.estimatedFare)}</strong>
        <div class="muted">${escapeHtml(state.estimate.ruleDescription || "")}</div></div>`
    : "";

  const payment = state.payment
    ? `<div class="fare-box"><div class="muted">Payment ${escapeHtml(state.payment.status)}</div>
        <strong>LKR ${escapeHtml(state.payment.amount)}</strong>
        <div class="muted">Receipt ${escapeHtml(state.payment.receiptNumber)}</div></div>`
    : "";

  const rides = state.rides.length
    ? state.rides
        .map(
          (r) => `
        <article class="ride-item">
          <header>
            <strong>#${r.id}</strong>
            <span class="status-pill">${escapeHtml(r.status)}</span>
          </header>
          <div>${escapeHtml(r.pickup)} → ${escapeHtml(r.destination)}</div>
          <div class="muted">Driver #${r.driverId ?? "—"} · Est ${r.estimatedFare ?? "—"} · Final ${r.finalFare ?? "—"}</div>
          <div class="btn-row">
            ${r.status !== "COMPLETED" && r.status !== "CANCELLED" ? `<button class="btn btn-danger" data-cancel="${r.id}">Cancel</button>` : ""}
            ${r.status === "COMPLETED" ? `<button class="btn btn-ghost" data-pay="${r.id}">Check payment</button>` : ""}
          </div>
        </article>`
        )
        .join("")
    : `<p class="muted">No rides yet.</p>`;

  renderShell(`
    <section class="block">
      <h3>Fare estimate</h3>
      <p class="lead">Ask the Fare service before you request a ride.</p>
      <form id="estimate-form">
        <label>Pickup <input name="pickup" required value="SLIIT Malabe" /></label>
        <label>Destination <input name="destination" required value="Colombo Fort" /></label>
        <button class="btn btn-primary" type="submit">Get estimate</button>
      </form>
      ${estimate}
    </section>
    <section class="block">
      <h3>Request a ride</h3>
      <p class="lead">Ride service assigns the first available driver.</p>
      <form id="ride-form">
        <label>Pickup <input name="pickup" required value="SLIIT Malabe" /></label>
        <label>Destination <input name="destination" required value="Colombo Fort" /></label>
        <button class="btn btn-primary" type="submit">Request ride</button>
      </form>
      ${payment}
    </section>
    <section class="block wide">
      <h3>My rides</h3>
      <p class="lead">Passenger ride history from the Ride service.</p>
      <div class="ride-list">${rides}</div>
      <div class="btn-row" style="margin-top:1rem">
        <button class="btn btn-ghost" id="refresh-rides">Refresh</button>
      </div>
    </section>
  `);

  app.querySelector("#estimate-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const body = Object.fromEntries(new FormData(e.target).entries());
    try {
      state.estimate = await api(API.fare, "/api/fares/estimate", { method: "POST", body });
      flash("Fare estimate ready");
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelector("#ride-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const body = Object.fromEntries(new FormData(e.target).entries());
    try {
      const ride = await api(API.ride, "/api/rides", { method: "POST", body });
      flash(`Ride #${ride.id} created · ${ride.status}`);
      await loadPassengerRides();
      render();
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelector("#refresh-rides")?.addEventListener("click", async () => {
    try {
      await loadPassengerRides();
      flash("Rides refreshed");
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelectorAll("[data-cancel]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      try {
        await api(API.ride, `/api/rides/${btn.dataset.cancel}/cancel`, { method: "PATCH" });
        flash("Ride cancelled");
        await loadPassengerRides();
        render();
      } catch (err) {
        flash(err.message, "error");
      }
    });
  });

  app.querySelectorAll("[data-pay]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      try {
        state.payment = await api(API.fare, `/api/payments/ride/${btn.dataset.pay}`);
        flash("Payment loaded");
      } catch (err) {
        flash(err.message + " (wait ~2s after complete, then retry)", "error");
      }
    });
  });
}

function driverView() {
  const profile = state.driverProfile;
  const profileHtml = profile
    ? `<div class="fare-box">
        <div class="muted">Vehicle ${escapeHtml(profile.vehicleNumber)} · ${escapeHtml(profile.vehicleType)}</div>
        <strong>${profile.available ? "Available" : "Busy / offline"}</strong>
        <div class="muted">${escapeHtml(profile.serviceArea)} · id ${profile.id}</div>
      </div>`
    : `<p class="muted">No driver profile yet. Register your vehicle below.</p>`;

  const rides = state.rides.length
    ? state.rides
        .map(
          (r) => `
        <article class="ride-item">
          <header>
            <strong>#${r.id}</strong>
            <span class="status-pill">${escapeHtml(r.status)}</span>
          </header>
          <div>${escapeHtml(r.pickup)} → ${escapeHtml(r.destination)}</div>
          <div class="btn-row">
            ${r.status === "ASSIGNED" ? `<button class="btn btn-primary" data-act="accept" data-id="${r.id}">Accept</button>` : ""}
            ${r.status === "ACCEPTED" ? `<button class="btn btn-primary" data-act="start" data-id="${r.id}">Start</button>` : ""}
            ${r.status === "IN_PROGRESS" ? `<button class="btn btn-primary" data-act="complete" data-id="${r.id}">Complete</button>` : ""}
            ${!["COMPLETED", "CANCELLED"].includes(r.status) ? `<button class="btn btn-danger" data-act="cancel" data-id="${r.id}">Cancel</button>` : ""}
          </div>
        </article>`
        )
        .join("")
    : `<p class="muted">No assigned rides yet. Ask a passenger to request one.</p>`;

  renderShell(`
    <section class="block">
      <h3>Driver profile</h3>
      <p class="lead">Owned by Driver & Vehicle service.</p>
      ${profileHtml}
      ${
        profile
          ? `<div class="btn-row" style="margin-top:1rem">
              <button class="btn btn-primary" id="go-available">Set available</button>
              <button class="btn btn-warn" id="go-busy">Set unavailable</button>
            </div>`
          : `<form id="driver-form">
              <label>Full name <input name="fullName" required value="${escapeHtml(state.user.fullName)}" /></label>
              <label>Phone <input name="phone" value="${escapeHtml(state.user.phone || "")}" /></label>
              <label>Vehicle number <input name="vehicleNumber" required value="CAB-1024" /></label>
              <label>Vehicle type <input name="vehicleType" required value="CAR" /></label>
              <label>Service area <input name="serviceArea" required value="Malabe" /></label>
              <label>Latitude <input name="latitude" type="number" step="any" required value="6.9147" /></label>
              <label>Longitude <input name="longitude" type="number" step="any" required value="79.9729" /></label>
              <input type="hidden" name="available" value="true" />
              <button class="btn btn-primary" type="submit">Register vehicle</button>
            </form>`
      }
    </section>
    <section class="block">
      <h3>Assigned rides</h3>
      <p class="lead">Accept → start → complete to finish the demo.</p>
      <div class="ride-list">${rides}</div>
      <div class="btn-row" style="margin-top:1rem">
        <button class="btn btn-ghost" id="refresh-driver-rides">Refresh</button>
      </div>
    </section>
  `);

  app.querySelector("#driver-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const raw = Object.fromEntries(new FormData(e.target).entries());
    const body = {
      ...raw,
      latitude: Number(raw.latitude),
      longitude: Number(raw.longitude),
      available: true,
    };
    try {
      state.driverProfile = await api(API.driver, "/api/drivers", { method: "POST", body });
      flash("Driver profile created");
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelector("#go-available")?.addEventListener("click", async () => {
    try {
      state.driverProfile = await api(API.driver, `/api/drivers/${state.driverProfile.id}/availability`, {
        method: "PATCH",
        body: { available: true },
      });
      flash("You are available for rides");
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelector("#go-busy")?.addEventListener("click", async () => {
    try {
      state.driverProfile = await api(API.driver, `/api/drivers/${state.driverProfile.id}/availability`, {
        method: "PATCH",
        body: { available: false },
      });
      flash("Marked unavailable");
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelector("#refresh-driver-rides")?.addEventListener("click", async () => {
    try {
      await loadDriverRides();
      flash("Rides refreshed");
    } catch (err) {
      flash(err.message, "error");
    }
  });

  app.querySelectorAll("[data-act]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const act = btn.dataset.act;
      const id = btn.dataset.id;
      try {
        await api(API.ride, `/api/rides/${id}/${act}`, { method: "PATCH" });
        flash(`Ride #${id} → ${act}`);
        await loadDriverRides();
        render();
      } catch (err) {
        flash(err.message, "error");
      }
    });
  });
}

async function loadPassengerRides() {
  state.rides = await api(API.ride, "/api/rides/passenger/me");
}

async function loadDriverRides() {
  state.rides = await api(API.ride, "/api/rides/driver/me");
}

async function loadDriverProfile() {
  try {
    state.driverProfile = await api(API.driver, `/api/drivers/by-account/${state.user.id}`);
  } catch {
    state.driverProfile = null;
  }
}

async function bootstrapWorkspace() {
  try {
    if (state.user.role === "DRIVER") {
      await loadDriverProfile();
      await loadDriverRides();
    } else {
      await loadPassengerRides();
    }
  } catch (err) {
    flash(err.message, "error");
  }
  render();
}

function render() {
  if (!state.token || !state.user) {
    renderGate();
    return;
  }
  if (state.user.role === "DRIVER") driverView();
  else passengerView();
}

render();
if (state.token && state.user) {
  bootstrapWorkspace();
}
