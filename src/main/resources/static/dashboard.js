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

// ---------- CAMARA: elegir la lente trasera "normal" (1x) ----------
// facingMode: "environment" solo pide "alguna camara trasera": en iPhones con
// varias lentes, desde iOS 16.4 hay un bug de WebKit que a veces elige la
// ultra angular en vez de la normal. Por eso enumeramos las camaras y evitamos
// especificamente la ultra angular, telefoto, macro, etc. — nunca la lente
// principal (que en iPhones de 3 camaras se llama justamente "Back Wide
// Camera", por eso NO se excluye "wide" a secas, solo "ultra wide").
//
// En Android, Chrome suele exponer etiquetas genericas sin pistas del tipo de
// lente (ej: "camera 0, facing back", "camera 2, facing back"). Ahi se usa la
// convencion de Android: la camara trasera principal casi siempre tiene el
// indice numerico mas bajo; las lentes extra (angular, macro, tele) quedan
// con indices mas altos.
async function obtenerCamaraTrasera() {
  try {
    const camaras = await Html5Qrcode.getCameras();
    if (!camaras || camaras.length === 0) {
      alert("DEBUG camaras: ninguna detectada"); // TEMPORAL: quitar despues de diagnosticar
      return { facingMode: "environment" };
    }

    const traseras = camaras.filter(c => /back|trasera|rear|environment/i.test(c.label));
    const candidatas = traseras.length > 0 ? traseras : camaras;

    const esOtraLente = c => /ultra.?wide|ultra.?angular|gran.?angular|tele(photo)?|macro|dual|triple/i.test(c.label);
    const sinOtraLente = candidatas.filter(c => !esOtraLente(c));
    const base = sinOtraLente.length > 0 ? sinOtraLente : candidatas;

    const indiceDe = c => {
      const match = c.label.match(/(\d+)/);
      return match ? parseInt(match[1], 10) : 0;
    };
    const ordenadas = [...base].sort((a, b) => indiceDe(a) - indiceDe(b));
    const elegida = ordenadas[0];

    alert("DEBUG todas: " + camaras.map(c => c.label).join(" | ") + "\nDEBUG elegida: " + elegida.label); // TEMPORAL
    return elegida.id;
  } catch (e) {
    alert("DEBUG error enumerando camaras: " + e.message); // TEMPORAL
    // Sin permiso o sin poder enumerar: volvemos al comportamiento por defecto
    return { facingMode: "environment" };
  }
}
