const API_BASE = window.location.origin + "/api";

function getToken() {
  return localStorage.getItem("token") || "";
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = "login.html";
  }
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("nombre");
  localStorage.removeItem("roles");
  window.location.href = "login.html";
}

function esAdmin() {
  const roles = JSON.parse(localStorage.getItem("roles") || "[]");
  return roles.includes("ROLE_ADMIN");
}

// Wrapper de fetch: agrega el header de autorizacion y homogeniza el manejo
// de errores usando el formato {mensaje} que ya devuelve GlobalExceptionHandler.
async function apiFetch(path, options = {}) {
  const headers = Object.assign(
    { "Authorization": `Bearer ${getToken()}` },
    options.body ? { "Content-Type": "application/json" } : {},
    options.headers || {}
  );
  const res = await fetch(`${API_BASE}${path}`, Object.assign({}, options, { headers }));

  if (!res.ok) {
    let mensaje = "Error en la peticion";
    try {
      mensaje = (await res.json()).mensaje || mensaje;
    } catch (e) {
      // respuesta sin body/JSON, se usa el mensaje generico
    }
    throw new Error(mensaje);
  }
  if (res.status === 204) return null;
  return res.json();
}

// ---------- SIDEBAR / SHELL ----------
function initSidebar(paginaActiva) {
  requireAuth();

  const nombreEl = document.getElementById("sidebarNombre");
  const rolEl = document.getElementById("sidebarRol");
  const roles = JSON.parse(localStorage.getItem("roles") || "[]");
  if (nombreEl) nombreEl.innerText = localStorage.getItem("nombre") || "";
  if (rolEl) rolEl.innerText = roles.map(r => r.replace("ROLE_", "")).join(", ");

  document.querySelectorAll(".sidebar-nav a").forEach(a => {
    if (a.dataset.pagina === paginaActiva) a.classList.add("activo");
    if (a.dataset.soloAdmin === "1" && !esAdmin()) a.classList.add("oculto");
  });

  const btnHamburguesa = document.getElementById("btnHamburguesa");
  const sidebar = document.getElementById("sidebar");
  const fondoMovil = document.getElementById("fondoSidebarMovil");
  if (btnHamburguesa && sidebar && fondoMovil) {
    btnHamburguesa.addEventListener("click", () => {
      sidebar.classList.toggle("abierta");
      fondoMovil.classList.toggle("abierta");
    });
    fondoMovil.addEventListener("click", () => {
      sidebar.classList.remove("abierta");
      fondoMovil.classList.remove("abierta");
    });
  }
}

function mostrarMensajeEn(divId, texto, tipo) {
  const div = document.getElementById(divId);
  if (!div) return;
  div.innerHTML = `<div class="msg ${tipo}">${texto}</div>`;
  setTimeout(() => { div.innerHTML = ""; }, 2500);
}
