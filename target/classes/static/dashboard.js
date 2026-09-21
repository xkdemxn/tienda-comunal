// Aviso tipo "Self-XSS" (el mismo que usan Facebook/Google en su consola):
// no oculta el codigo -esto no existe en el navegador-, pero desalienta que
// alguien pegue ahi un script que le paso un tercero.
console.log(
  "%c¡Alto!",
  "color:#dc2626; font-size:60px; font-weight:bold; text-shadow: 2px 2px #7f1d1d;"
);
console.log(
  "%cEsta es una funcion del navegador pensada para desarrolladores. Si alguien te dijo que copies y pegues algo aca para 'activar' o 'arreglar' algo del sistema, es un engaño: le estarias dando acceso a tu cuenta y a los datos de la tienda.",
  "font-size:15px; color:#0f172a;"
);

const API_BASE = window.location.origin + "/api";

// OJO: nunca usar date.toISOString().slice(0,10) para "hoy" - toISOString()
// convierte a UTC, y en Ecuador (UTC-5) desde ~7pm ya cae en el dia siguiente
// en UTC, dando fechas equivocadas (esto causaba que "Ventas de hoy" no
// encontrara nada). Esta usa los componentes de fecha LOCALES del navegador.
function fechaLocalISO(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

// ---------- GRAFICO DE PASTEL (canvas puro, sin librerias externas) ----------
const PALETA_GRAFICO = [
  "#0d7730", "#1d709d", "#e6a83c", "#a1330f", "#7c3aed",
  "#0891b2", "#be185d", "#65a30d", "#c2410c", "#4338ca"
];

// datos: [{ label, valor }]. Dibuja el pastel dentro del <canvas> y, si se
// pasa idLeyenda, arma debajo una leyenda con color + nombre + valor + %.
function dibujarGraficoPastel(idCanvas, datos, idLeyenda, formatoValor) {
  const canvas = document.getElementById(idCanvas);
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const total = datos.reduce((acc, d) => acc + Number(d.valor), 0);
  const cx = canvas.width / 2;
  const cy = canvas.height / 2;
  const radio = Math.min(cx, cy) - 6;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  if (total <= 0 || datos.length === 0) {
    ctx.fillStyle = "#94a3b8";
    ctx.font = "13px sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("Sin datos", cx, cy);
    if (idLeyenda) document.getElementById(idLeyenda).innerHTML = "";
    return;
  }

  let anguloInicio = -Math.PI / 2;
  datos.forEach((d, i) => {
    const porcion = Number(d.valor) / total;
    const anguloFin = anguloInicio + porcion * Math.PI * 2;
    const color = d.color || PALETA_GRAFICO[i % PALETA_GRAFICO.length];

    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.arc(cx, cy, radio, anguloInicio, anguloFin);
    ctx.closePath();
    ctx.fillStyle = color;
    ctx.fill();
    ctx.strokeStyle = "#ffffff";
    ctx.lineWidth = 2;
    ctx.stroke();

    anguloInicio = anguloFin;
  });

  if (idLeyenda) {
    const div = document.getElementById(idLeyenda);
    div.innerHTML = datos.map((d, i) => {
      const color = d.color || PALETA_GRAFICO[i % PALETA_GRAFICO.length];
      const pct = ((Number(d.valor) / total) * 100).toFixed(1);
      const valorTexto = formatoValor ? formatoValor(d.valor) : d.valor;
      return `
        <div style="display:flex; align-items:center; gap:8px; padding:4px 0; font-size:13px">
          <span style="width:10px; height:10px; border-radius:50%; background:${color}; flex-shrink:0; display:inline-block"></span>
          <span style="flex:1">${d.label}</span>
          <span style="color:var(--muted)">${valorTexto} · ${pct}%</span>
        </div>`;
    }).join("");
  }
}

// ---------- GRAFICO DE LINEAS (canvas puro, sin librerias externas) ----------
// series: [{ label, valores: [12 numeros o null], color }]. Un valor null
// (ej: un mes que todavia no llega en el anio en curso) corta la linea en
// ese punto en vez de dibujarlo como si fuera un dato real.
function dibujarGraficoLineas(idCanvas, etiquetas, series, idLeyenda, formatoValor) {
  const canvas = document.getElementById(idCanvas);
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const w = canvas.width;
  const h = canvas.height;
  ctx.clearRect(0, 0, w, h);

  const pad = { top: 16, right: 16, bottom: 28, left: 56 };
  const areaW = w - pad.left - pad.right;
  const areaH = h - pad.top - pad.bottom;

  const todosLosValores = series.flatMap(s => s.valores).filter(v => v !== null && v !== undefined);
  if (todosLosValores.length === 0) {
    ctx.fillStyle = "#94a3b8";
    ctx.font = "13px sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("Sin datos todavia", w / 2, h / 2);
    if (idLeyenda) document.getElementById(idLeyenda).innerHTML = "";
    return;
  }

  let min = Math.min(0, ...todosLosValores);
  let max = Math.max(0, ...todosLosValores);
  if (min === max) { min -= 1; max += 1; }
  const margen = (max - min) * 0.1;
  min -= margen;
  max += margen;

  const nPuntos = etiquetas.length;
  const xDe = i => pad.left + (nPuntos === 1 ? areaW / 2 : (areaW * i) / (nPuntos - 1));
  const yDe = v => pad.top + areaH - ((v - min) / (max - min)) * areaH;

  // Grilla horizontal + etiquetas del eje Y
  const DIVISIONES = 4;
  ctx.strokeStyle = "#e5e5ea";
  ctx.lineWidth = 1;
  ctx.font = "11px sans-serif";
  ctx.fillStyle = "#94a3b8";
  ctx.textAlign = "right";
  ctx.textBaseline = "middle";
  for (let i = 0; i <= DIVISIONES; i++) {
    const valor = min + ((max - min) * i) / DIVISIONES;
    const y = yDe(valor);
    ctx.beginPath();
    ctx.moveTo(pad.left, y);
    ctx.lineTo(w - pad.right, y);
    ctx.stroke();
    ctx.fillText(formatoValor ? formatoValor(valor) : valor.toFixed(0), pad.left - 8, y);
  }

  // Linea del cero (si el rango cruza valores negativos, ej un mes en perdida)
  if (min < 0 && max > 0) {
    ctx.strokeStyle = "#c7c7cc";
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(pad.left, yDe(0));
    ctx.lineTo(w - pad.right, yDe(0));
    ctx.stroke();
  }

  // Etiquetas del eje X (meses)
  ctx.fillStyle = "#64748b";
  ctx.textAlign = "center";
  ctx.textBaseline = "top";
  etiquetas.forEach((etq, i) => ctx.fillText(etq, xDe(i), h - pad.bottom + 8));

  // Lineas de cada serie
  series.forEach((serie, si) => {
    const color = serie.color || PALETA_GRAFICO[si % PALETA_GRAFICO.length];
    ctx.strokeStyle = color;
    ctx.fillStyle = color;
    ctx.lineWidth = 2.5;

    let trazando = false;
    serie.valores.forEach((v, i) => {
      if (v === null || v === undefined) { trazando = false; return; }
      const x = xDe(i);
      const y = yDe(v);
      if (!trazando) {
        ctx.beginPath();
        ctx.moveTo(x, y);
        trazando = true;
      } else {
        ctx.lineTo(x, y);
      }
    });
    ctx.stroke();

    serie.valores.forEach((v, i) => {
      if (v === null || v === undefined) return;
      ctx.beginPath();
      ctx.arc(xDe(i), yDe(v), 3, 0, Math.PI * 2);
      ctx.fill();
    });
  });

  if (idLeyenda) {
    const div = document.getElementById(idLeyenda);
    div.innerHTML = series.map((s, i) => {
      const color = s.color || PALETA_GRAFICO[i % PALETA_GRAFICO.length];
      const valoresConocidos = s.valores.filter(v => v !== null && v !== undefined);
      const ultimo = valoresConocidos.length > 0 ? valoresConocidos[valoresConocidos.length - 1] : null;
      const textoUltimo = ultimo !== null ? (formatoValor ? formatoValor(ultimo) : ultimo) : "sin datos";
      return `
        <div style="display:flex; align-items:center; gap:8px; padding:4px 0; font-size:13px">
          <span style="width:10px; height:10px; border-radius:50%; background:${color}; flex-shrink:0; display:inline-block"></span>
          <span style="flex:1">${s.label}</span>
          <span style="color:var(--muted)">ultimo dato: ${textoUltimo}</span>
        </div>`;
    }).join("");
  }
}

function getToken() {
  return localStorage.getItem("token") || "";
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = "login";
  }
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("nombre");
  localStorage.removeItem("roles");
  window.location.href = "login";
}

// Se llama sola cuando una peticion da 401 (token invalido/vencido).
// Evita que se dispare mas de una vez si varias peticiones fallan a la vez
// (ej: una pagina que carga 3 cosas en paralelo al abrir).
let cerrandoSesionPorError = false;
function cerrarSesionPorTokenInvalido() {
  if (cerrandoSesionPorError) return;
  cerrandoSesionPorError = true;
  localStorage.removeItem("token");
  localStorage.removeItem("nombre");
  localStorage.removeItem("roles");
  localStorage.setItem("mensajeLogin", "Tu sesion vencio o hubo un problema cargando datos. Inicia sesion de nuevo.");
  window.location.href = "login";
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

  // 401 = el token no es valido (vencio, se cerro sesion en otro lado, etc).
  // No tiene sentido seguir mostrando pantallas a medias con datos que no
  // cargaron: se cierra la sesion y se manda a loguear de nuevo, asi al
  // volver a entrar todo carga limpio y sin inconsistencias. Los demas
  // errores (400, 403, 409, 500...) siguen mostrandose donde ya se mostraban,
  // porque cerrar sesion ahi no arreglaria nada (ej: "falta el nombre").
  if (res.status === 401) {
    cerrarSesionPorTokenInvalido();
    throw new Error("Tu sesion vencio, iniciando sesion de nuevo...");
  }

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
    if (a.dataset.pagina === paginaActiva) {
      a.classList.add("activo");
      // si el link activo esta dentro de un grupo desplegable, lo abrimos
      const grupo = a.closest(".grupo-links");
      if (grupo) {
        grupo.classList.remove("oculto");
        const toggle = document.querySelector(`.grupo-toggle[data-grupo="${grupo.dataset.grupo}"]`);
        if (toggle) toggle.classList.add("abierto");
      }
    }
    if (a.dataset.soloAdmin === "1" && !esAdmin()) a.classList.add("oculto");
  });

  document.querySelectorAll(".grupo-toggle").forEach(btn => {
    if (btn.dataset.soloAdmin === "1" && !esAdmin()) btn.classList.add("oculto");
  });

  // Barra de navegacion inferior (solo celular): mismo resaltado de la
  // pagina activa que el sidebar, pero es una lista aparte (no esta dentro
  // de .sidebar-nav) porque vive fija al fondo de la pantalla.
  document.querySelectorAll(".barra-inferior a").forEach(a => {
    if (a.dataset.pagina === paginaActiva) a.classList.add("activo");
  });

  const sidebar = document.getElementById("sidebar");
  const fondoMovil = document.getElementById("fondoSidebarMovil");
  const abrirCerrarSidebarMovil = () => {
    if (!sidebar || !fondoMovil) return;
    sidebar.classList.toggle("abierta");
    fondoMovil.classList.toggle("abierta");
  };

  const btnHamburguesa = document.getElementById("btnHamburguesa");
  const btnMasMovil = document.getElementById("btnMasMovil");
  if (btnHamburguesa) btnHamburguesa.addEventListener("click", abrirCerrarSidebarMovil);
  if (btnMasMovil) btnMasMovil.addEventListener("click", abrirCerrarSidebarMovil);
  if (fondoMovil) {
    fondoMovil.addEventListener("click", () => {
      sidebar.classList.remove("abierta");
      fondoMovil.classList.remove("abierta");
    });
  }
}

function toggleGrupo(nombre) {
  const grupo = document.getElementById(`grupo-${nombre}`);
  const toggle = document.querySelector(`.grupo-toggle[data-grupo="${nombre}"]`);
  if (!grupo) return;
  grupo.classList.toggle("oculto");
  if (toggle) toggle.classList.toggle("abierto");
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
    if (!camaras || camaras.length === 0) return { facingMode: "environment" };

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

    return elegida.id;
  } catch (e) {
    // Sin permiso o sin poder enumerar: volvemos al comportamiento por defecto
    return { facingMode: "environment" };
  }
}
