const API_URL = window.location.origin && window.location.origin !== "null"
    ? window.location.origin
    : "http://localhost:8080";

function getToken() {
    return localStorage.getItem("token");
}

function setToken(token) {
    localStorage.setItem("token", token);
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    localStorage.removeItem("subastaId");
    window.location.href = "login.html";
}

function authHeaders(extraHeaders = {}) {
    const token = getToken();
    return {
        ...extraHeaders,
        ...(token ? { Authorization: "Bearer " + token } : {})
    };
}

function requireLogin() {
    if (!getToken()) {
        window.location.href = "login.html";
        return false;
    }
    return true;
}

async function leerError(res, fallback) {
    const text = await res.text();
    if (!text) return fallback;

    try {
        const json = JSON.parse(text);
        return json.message || json.error || fallback;
    } catch {
        return text;
    }
}

async function apiFetch(url, options = {}) {
    const res = await fetch(url, options);

    if (res.status === 401 || res.status === 403) {
        logout();
        throw new Error("Sesión vencida. Volvé a iniciar sesión.");
    }

    return res;
}

async function getUsuarioActual() {
    const guardado = localStorage.getItem("usuario");
    if (guardado) return JSON.parse(guardado);

    const res = await apiFetch(`${API_URL}/usuarios/me`, {
        headers: authHeaders()
    });

    if (!res.ok) {
        throw new Error(await leerError(res, "No se pudo obtener el usuario actual"));
    }

    const usuario = await res.json();
    localStorage.setItem("usuario", JSON.stringify(usuario));
    return usuario;
}

function tieneRol(usuario, rol) {
    return usuario.roles && usuario.roles.includes(rol);
}

function formatearMoneda(valor) {
    return Number(valor || 0).toLocaleString("es-AR", {
        style: "currency",
        currency: "ARS",
        maximumFractionDigits: 0
    });
}

function formatearFecha(fecha) {
    if (!fecha) return "Sin fecha";

    return new Date(fecha + "Z").toLocaleString("es-AR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("loginForm");
    const registroForm = document.getElementById("registroForm");
    const crearSubastaForm = document.getElementById("crearSubastaForm");

    if (document.getElementById("adminListaUsuarios")) cargarPanelAdmin();
    if (loginForm) loginForm.addEventListener("submit", login);
    if (registroForm) registroForm.addEventListener("submit", register);
    if (crearSubastaForm) crearSubastaForm.addEventListener("submit", crearSubasta);

    if (document.getElementById("contenedorSubastas")) cargarSubastas();
    if (document.getElementById("titulo")) cargarDetalle();
    if (document.getElementById("perfilNombre")) cargarPerfil();
    configurarMenuPorRol();
});

async function configurarMenuPorRol() {
    const link = document.getElementById("linkCrearSubasta");

    if (!link || !getToken()) return;

    const linkAdmin = document.getElementById("linkAdmin");

    if (linkAdmin && (!tieneRol(usuario, "ADMIN"))) {
        linkAdmin.style.display = "none";
    }

    const usuario = await getUsuarioActual();

    if (!tieneRol(usuario, "VENDEDOR") && !tieneRol(usuario, "ADMIN")) {
        link.style.display = "none";
    }
}
async function login(e) {
    e.preventDefault();

    const dto = {
        email: document.getElementById("email").value.trim(),
        password: document.getElementById("password").value
    };

    try {
        const res = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            alert(await leerError(res, "Credenciales incorrectas"));
            return;
        }

        const token = await res.text();

        setToken(token);
        localStorage.removeItem("usuario");

        window.location.href = "subastas.html";
    } catch (error) {
        alert(error.message || "Error al iniciar sesión");
    }
}

async function register(e) {
    e.preventDefault();

    const roles = Array.from(document.querySelectorAll(".rolRegistro:checked"))
        .map(check => check.value);
    if (roles.length === 0) {
        alert("Debe seleccionar al menos un rol de usuario.");
        return;
    }
    const dto = {
        nombre: document.getElementById("nombre").value.trim(),
        email: document.getElementById("email").value.trim(),
        password: document.getElementById("password").value,
        roles: roles
    };

    try {
        const res = await fetch(`${API_URL}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            alert(await leerError(res, "Error al registrar"));
            return;
        }

        alert("Registro exitoso");
        window.location.href = "login.html";
    } catch (error) {
        alert(error.message || "Error al registrar");
    }
}

async function cargarSubastas() {
    if (!requireLogin()) return;

    const contenedor = document.getElementById("contenedorSubastas");
    contenedor.innerHTML = `<p class="mensaje">Cargando subastas...</p>`;

    try {

        const usuario = await getUsuarioActual();

        let url = `${API_URL}/subastas`;

        if (tieneRol(usuario, "VENDEDOR") || tieneRol(usuario, "ADMIN")) {
            url = `${API_URL}/subastas/mias`;
        }

        const res = await apiFetch(url, {
            headers: authHeaders()
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "Error cargando subastas"));
        }

        const data = await res.json();

        if (!data.length) {
            contenedor.innerHTML = `<p class="mensaje">No hay subastas disponibles por el momento.</p>`;
            return;
        }

        contenedor.innerHTML = data.map(s => `
            <div class="card">
                <h3>${s.producto?.titulo || s.titulo || "Subasta sin título"}</h3>

                <p>${s.producto?.descripcion || s.descripcion || ""}</p>

                <p><strong>${formatearMoneda(s.precioActual || s.precioBase)}</strong></p>

                <p>Estado: ${s.estado}</p>

                <p>Inicio: ${formatearFecha(s.fechaInicio)}</p>

                <p>Cierre: ${formatearFecha(s.fechaCierre)}</p>

                ${s.estado === "BORRADOR" ? `
                    <button class="btn-principal" onclick="publicarSubasta(${s.id})">
                        Publicar
                    </button>
                ` : ""}
            </div>
        `).join("");

    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}


function verDetalle(id) {
    localStorage.setItem("subastaId", id);
    window.location.href = "detalle.html";
}

async function crearSubasta(e) {
    e.preventDefault();

    if (!requireLogin()) return;

    try {
        const usuario = await getUsuarioActual();

        if (!tieneRol(usuario, "VENDEDOR") && !tieneRol(usuario, "ADMIN")) {
            alert("Solo los vendedores pueden crear subastas.");
            return;
        }

        const fechaInicio = document.getElementById("fechaInicio").value;
        const fechaCierre = document.getElementById("fechaFin").value;

        if (!fechaInicio || !fechaCierre) {
            alert("Debés indicar fecha de inicio y fecha de cierre.");
            return;
        }

        if (new Date(fechaCierre) <= new Date(fechaInicio)) {
            alert("La fecha de cierre debe ser posterior a la fecha de inicio.");
            return;
        }

        const dto = {
            titulo: document.getElementById("tituloSubasta").value.trim(),
            descripcion: document.getElementById("descripcionSubasta").value.trim(),
            precioBase: Number(document.getElementById("precioInicial").value),
            incrementoMinimo: Number(document.getElementById("incrementoMinimo").value),
            fechaInicio: convertirFechaLocalAUTC(fechaInicio),
            fechaCierre: convertirFechaLocalAUTC(fechaCierre),
            estado: document.getElementById("estadoSubasta").value
        };

        const res = await apiFetch(`${API_URL}/subastas`, {
            method: "POST",
            headers: authHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "No se pudo crear la subasta"));
        }

        alert("Subasta creada correctamente");
        window.location.href = "subastas.html";
    } catch (error) {
        alert(error.message || "Error al crear la subasta");
    }
}
function convertirFechaLocalAUTC(fechaLocal) {
    return new Date(fechaLocal).toISOString().slice(0, 19);
}

async function cargarDetalle() {
    if (!requireLogin()) return;

    const id = localStorage.getItem("subastaId");

    if (!id) {
        window.location.href = "subastas.html";
        return;
    }

    try {
        const res = await apiFetch(`${API_URL}/subastas/${id}`, {
            headers: authHeaders()
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "Error cargando subasta"));
        }

        const s = await res.json();

        document.getElementById("titulo").innerText =
            s.producto?.titulo || s.titulo || "Subasta sin título";

        document.getElementById("descripcion").innerText =
            s.producto?.descripcion || s.descripcion || "";

        document.getElementById("precio").innerText =
            formatearMoneda(s.precioActual || s.precioBase);
        const incremento = document.getElementById("incrementoMinimo");

        if (incremento) {
            incremento.innerText = formatearMoneda(s.incrementoMinimo);
        }

        const estado = document.getElementById("estadoSubasta");
        if (estado) estado.innerText = s.estado;

        const fechaInicio = document.getElementById("fechaInicioDetalle");
        if (fechaInicio) fechaInicio.innerText = formatearFecha(s.fechaInicio);

        const fechaCierre = document.getElementById("fechaFinDetalle");
        if (fechaCierre) fechaCierre.innerText = formatearFecha(s.fechaCierre);

        await cargarPujas(id);
    } catch (error) {
        alert(error.message || "Error cargando subasta");
        window.location.href = "subastas.html";
    }
}

async function pujar() {
    if (!requireLogin()) return;

    const id = localStorage.getItem("subastaId");
    const monto = Number(document.getElementById("monto").value);

    if (!monto || monto <= 0) {
        alert("Monto inválido");
        return;
    }

    try {
        const usuario = await getUsuarioActual();

        if (!tieneRol(usuario, "COMPRADOR") && !tieneRol(usuario, "ADMIN")) {
            alert("No tenés permiso para realizar pujas.");
            return;
        }

        const dto = { monto };

        const res = await apiFetch(`${API_URL}/pujas?subastaId=${id}`, {
            method: "POST",
            headers: authHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "Error al pujar"));
        }

        alert("Puja realizada correctamente");
        document.getElementById("monto").value = "";

        await cargarDetalle();
    } catch (error) {
        alert(error.message || "Error al pujar");
    }
}

async function cargarPujas(id) {
    const contenedor = document.getElementById("listaPujas");

    if (!contenedor) return;

    contenedor.innerHTML = `<p class="mensaje">Cargando pujas...</p>`;

    try {
        const res = await apiFetch(`${API_URL}/pujas/subasta/${id}`, {
            headers: authHeaders()
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "No se pudieron cargar las pujas"));
        }

        const pujas = await res.json();

        if (!pujas.length) {
            contenedor.innerHTML = `<p class="mensaje">Todavía no hay pujas.</p>`;
            return;
        }

        contenedor.innerHTML = pujas.map(p => `
            <div class="card">
                <h3>${formatearMoneda(p.monto)}</h3>
                <p>Oferente: ${p.oferente || "Anónimo"}</p>
                <p>Fecha: ${formatearFecha(p.fechaHora)}</p>
            </div>
        `).join("");
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}

async function cargarPerfil() {
    if (!requireLogin()) return;

    try {
        const usuario = await getUsuarioActual();

        document.getElementById("perfilNombre").innerText = usuario.nombre;
        document.getElementById("perfilEmail").innerText = usuario.email;
        document.getElementById("perfilRol").innerText = usuario.roles?.join(", ") || "Sin roles";
        await cargarNotificaciones(usuario.id);
    } catch (error) {
        alert(error.message || "Error cargando perfil");
        logout();
    }
}
async function cargarPanelAdmin() {
    if (!requireLogin()) return;

    const usuario = await getUsuarioActual();

    if (!tieneRol(usuario, "ADMIN")) {
        alert("No tenés permiso para acceder al panel de administración.");
        window.location.href = "subastas.html";
        return;
    }

    await adminCargarUsuarios();
    await adminCargarSubastas();
}

async function adminCargarUsuarios() {
    const contenedor = document.getElementById("adminListaUsuarios");

    const res = await apiFetch(`${API_URL}/admin/usuarios`, {
        headers: authHeaders()
    });

    const usuarios = await res.json();

    contenedor.innerHTML = usuarios.map(u => `
        <div class="card">
            <h3>${u.nombre}</h3>
            <p>Email: ${u.email}</p>
            <p>Roles: ${u.roles?.join(", ") || "Sin roles"}</p>
            <p>Estado: ${u.bloqueado ? "Suspendido" : "Activo"}</p>
        </div>
    `).join("");
}

async function adminCargarSubastas() {
    const contenedor = document.getElementById("adminListaSubastas");

    const res = await apiFetch(`${API_URL}/admin/subastas`, {
        headers: authHeaders()
    });

    const subastas = await res.json();

    contenedor.innerHTML = subastas.map(s => `
        <div class="card">
            <h3>${s.producto?.titulo || "Subasta sin producto"}</h3>
            <p>ID: ${s.id}</p>
            <p>Estado: ${s.estado}</p>
            <p>Precio actual: ${formatearMoneda(s.precioActual)}</p>
        </div>
    `).join("");
}

async function adminAsignarRoles() {
    const email = document.getElementById("adminEmailRoles").value.trim();

    const roles = Array.from(document.querySelectorAll(".rolCheck:checked"))
        .map(check => check.value);

    if (!email || roles.length === 0) {
        alert("Ingresá email y al menos un rol.");
        return;
    }

    const res = await apiFetch(`${API_URL}/admin/usuarios/${encodeURIComponent(email)}/roles`, {
        method: "PUT",
        headers: authHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify(roles)
    });

    if (!res.ok) {
        alert(await leerError(res, "No se pudieron actualizar los roles"));
        return;
    }

    alert("Roles actualizados correctamente");
    await adminCargarUsuarios();
}

async function adminSuspenderUsuario() {
    const email = document.getElementById("adminEmailSuspender").value.trim();

    if (!email) {
        alert("Ingresá el email del usuario.");
        return;
    }

    const res = await apiFetch(`${API_URL}/admin/usuarios/${encodeURIComponent(email)}/suspender`, {
        method: "PUT",
        headers: authHeaders()
    });

    if (!res.ok) {
        alert(await leerError(res, "No se pudo suspender el usuario"));
        return;
    }

    alert("Usuario suspendido");
    await adminCargarUsuarios();
}

async function adminReactivarUsuario() {
    const email = document.getElementById("adminEmailSuspender").value.trim();

    if (!email) {
        alert("Ingresá el email del usuario.");
        return;
    }

    const res = await apiFetch(`${API_URL}/admin/usuarios/${encodeURIComponent(email)}/reactivar`, {
        method: "PUT",
        headers: authHeaders()
    });

    if (!res.ok) {
        alert(await leerError(res, "No se pudo reactivar el usuario"));
        return;
    }

    alert("Usuario reactivado");
    await adminCargarUsuarios();
}

async function adminCancelarSubasta() {
    const usuario = await getUsuarioActual();

    const id = document.getElementById("adminSubastaId").value;
    const motivo = document.getElementById("adminMotivoCancelacion").value.trim();

    if (!id || !motivo) {
        alert("Ingresá ID de subasta y motivo.");
        return;
    }

    const res = await apiFetch(`${API_URL}/admin/subastas/${id}/cancelar?adminId=${usuario.id}`, {
        method: "PUT",
        headers: authHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify({ motivo })
    });

    if (!res.ok) {
        alert(await leerError(res, "No se pudo cancelar la subasta"));
        return;
    }

    alert("Subasta cancelada correctamente");
    await adminCargarSubastas();
}

async function cargarNotificaciones(usuarioId) {

    const contenedor = document.getElementById("contenedorNotificaciones");

    if (!contenedor) return;

    try {

        const res = await apiFetch(`${API_URL}/notificaciones/usuario/${usuarioId}`, {
            headers: authHeaders()
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "Error cargando notificaciones"));
        }

        const notificaciones = await res.json();

        if (notificaciones.length === 0) {
            contenedor.innerHTML = `
                <p class="mensaje">No tenés notificaciones.</p>
            `;
            return;
        }

        contenedor.innerHTML = notificaciones.map(n => `
            <div class="card ${n.leida ? "" : "notificacion-no-leida"}">
                <h3>${n.titulo}</h3>
                <p>${n.mensaje}</p>
                <small>${formatearFecha(n.fecha)}</small>
                <br><br>

                ${
                    n.leida
                    ? "<span>✔ Leída</span>"
                    : `<button class="btn-secundario"
                        onclick="marcarNotificacionLeida(${n.id})">
                        Marcar como leída
                    </button>`
                }
            </div>
        `).join("");

    } catch (error) {

        contenedor.innerHTML = `
            <p class="mensaje error">${error.message}</p>
        `;
    }
}

async function marcarNotificacionLeida(id) {

    try {

        const res = await apiFetch(`${API_URL}/notificaciones/${id}/leida`, {
            method: "PUT",
            headers: authHeaders()
        });

        if (!res.ok) {
            throw new Error(await leerError(res, "No se pudo marcar la notificación"));
        }

        const usuario = await getUsuarioActual();

        await cargarNotificaciones(usuario.id);

    } catch (error) {

        alert(error.message);
    }
}

async function publicarSubasta(id) {

    const usuario = await getUsuarioActual();

    try {

        const res = await apiFetch(
            `${API_URL}/subastas/${id}/publicar?vendedorId=${usuario.id}`,
            {
                method: "POST",
                headers: authHeaders()
            }
        );

        if (!res.ok) {
            throw new Error(await leerError(res, "No se pudo publicar la subasta"));
        }

        alert("Subasta publicada correctamente");

        await cargarSubastas();

    } catch (error) {
        alert(error.message);
    }
}