let map;

let origen = null;
let destino = null;

let paradas = [];
let marcadores = new Map();
const MAX_PARADAS = 4;

let lineaRuta = null;

// Lista de ciudades obtenida desde el backend
let ciudades = [];

async function cargarCiudades(){
    const respuesta = await fetch("/direcciones/listar");
    ciudades = await respuesta.json();
    llenarCombos();
    initMap();
}

function llenarCombos(){
    const origenSelect = document.getElementById("origenSelect");
    const destinoSelect = document.getElementById("destinoSelect");

    ciudades.forEach(c=>{
        origenSelect.innerHTML += `
            <option value="${c.id}">
                ${c.direccionTexto}
            </option>`;

        destinoSelect.innerHTML += `
            <option value="${c.id}">
                ${c.direccionTexto}
            </option>`;
    });
    
    document.getElementById("origenSelect").addEventListener("change", actualizarOrigen);
    document.getElementById("destinoSelect").addEventListener("change", actualizarDestino);
}

async function initMap(){
    map = new google.maps.Map(
        document.getElementById("map"),
        {
            zoom:8,
            center:{
                lat:-1.2,
                lng:-78.6
            }
        });

    ciudades.forEach(c=>{
        const marcador = new google.maps.Marker({
            position:{
                lat:c.latitud,
                lng:c.longitud
            },
            map: null, // Nacen ocultos
            title:c.direccionTexto
        });

        marcadores.set(c.id, marcador);
    });
}

async function calcularRuta(){
    if(origen == null || destino == null){
        alert("Seleccione una ciudad de origen y una de destino.");
        return;
    }

    const idsParadas = paradas
        .filter(p => p != null)
        .map(p => p.id);

    const respuesta = await fetch("/grafo/calcular", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            origenId: origen.id,
            destinoId: destino.id,
            paradas: idsParadas
        })
    });

    const datos = await respuesta.json();
    console.log(datos);
    await dibujarRuta(datos.camino);
}

//Obtenemos la ruta entre dos puntos usando OSRM
async function obtenerRutaOSRM(origen, destino){
    const url =
        `https://router.project-osrm.org/route/v1/driving/` +
        `${origen.longitud},${origen.latitud};` +
        `${destino.longitud},${destino.latitud}` +
        `?overview=full&geometries=geojson`;

    const respuesta = await fetch(url);
    const datos = await respuesta.json();
    console.log("Respuesta OSRM:", datos);

    if(datos.routes.length === 0){
        return [];
    }

    return datos.routes[0].geometry.coordinates;
}

// Dibujar ruta mejorada con cálculo de Distancia y Tiempo (Versión Corregida)
async function dibujarRuta(camino){
    console.log("Camino Dijkstra completo:", camino);
    let coordenadas = [];
    
    // Variables para acumular las métricas del viaje completo
    let distanciaTotalMetros = 0;
    let tiempoTotalSegundos = 0;

    for(let i = 0; i < camino.length - 1; i++){
        // Buscamos las ciudades ignorando espacios y mayúsculas
        const ciudadOrigen = ciudades.find(c => (c.direccionTexto || "").toString().trim().toLowerCase() === camino[i].toString().trim().toLowerCase());
        const ciudadDestino = ciudades.find(c => (c.direccionTexto || "").toString().trim().toLowerCase() === camino[i + 1].toString().trim().toLowerCase());

        if(!ciudadOrigen || !ciudadDestino){
            console.error(`¡Error de mapeo! No encontré las coordenadas para el tramo: "${camino[i]}" -> "${camino[i+1]}"`);
            continue; 
        }

        console.log(`Dibujando Tramo OSRM: ${ciudadOrigen.direccionTexto} -> ${ciudadDestino.direccionTexto}`);
        
        try {
            // 1. Llamamos a OSRM para este tramo específico
            const url = `https://router.project-osrm.org/route/v1/driving/${ciudadOrigen.longitud},${ciudadOrigen.latitud};${ciudadDestino.longitud},${ciudadDestino.latitud}?overview=full&geometries=geojson`;
            const respuesta = await fetch(url);
            const datos = await respuesta.json();

            if (datos.routes && datos.routes.length > 0) {
                // 2. ACUMULAMOS: Aquí estaba el detalle, ahora sí sumamos los valores reales de OSRM
                distanciaTotalMetros += datos.routes[0].distance;
                tiempoTotalSegundos += datos.routes[0].duration;

                // 3. Guardamos las coordenadas para pintar la línea en el mapa
                const tramoCoordenadas = datos.routes[0].geometry.coordinates;
                tramoCoordenadas.forEach(p => {
                    coordenadas.push({ lat: p[1], lng: p[0] });
                });
            }
        } catch (error) {
            console.error("Error consultando el servidor OSRM en este tramo:", error);
        }
    }

    // Renderizado de la línea roja en el mapa
    if(lineaRuta) lineaRuta.setMap(null);

    lineaRuta = new google.maps.Polyline({
        path: coordenadas,
        geodesic: true,
        strokeColor: "#FF0000",
        strokeOpacity: 1,
        strokeWeight: 5
    });

    lineaRuta.setMap(map);

    // =============================================================
    // INYECTAR DATOS EN EL PANEL HTML
    // =============================================================
    const panel = document.getElementById("panelDetalles");
    
    if (panel && coordenadas.length > 0) {
        // Convertimos metros a kilómetros
        const km = (distanciaTotalMetros / 1000).toFixed(1);
        
        // Calculamos horas y minutos partiendo de los segundos
        const horas = Math.floor(tiempoTotalSegundos / 3600);
        const minutos = Math.floor((tiempoTotalSegundos % 3600) / 60);

        // Construimos el contenido visual
        panel.innerHTML = `
            <h4 style="margin: 0 0 8px 0; color: #333; font-weight: bold;">📊 Resumen del Recorrido</h4>
            <p style="margin: 4px 0; color: #555;"><strong>🛣️ Distancia Total:</strong> ${km} km</p>
            <p style="margin: 4px 0; color: #555;"><strong>⏱️ Tiempo Estimado:</strong> ${horas > 0 ? horas + ' h ' : ''}${minutos} min</p>
        `;
        panel.style.display = "block"; // Volvemos a hacer visible el cuadro gris
    }
}

function agregarParada(){
    if(paradas.length >= MAX_PARADAS){
        alert("Solo puede agregar hasta 4 ciudades.");
        return;
    }

    paradas.push(null);
    renderizarParadas();
}

function eliminarParada(indice){
    paradas.splice(indice,1);
    renderizarParadas();
    actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA: Apaga el pin al eliminar la parada
}

function renderizarParadas(){
    const contenedor = document.getElementById("contenedorParadas");
    contenedor.innerHTML = "";

    paradas.forEach((p, indice) => {
        const div = document.createElement("div");
        div.style.marginBottom = "10px";

        div.innerHTML = `
            <label>Ciudad intermedia ${indice + 1}</label>
            <br>
            <select id="parada${indice}">
                <option value="">Seleccione...</option>
            </select>
            <button type="button" onclick="eliminarParada(${indice})">
                Eliminar
            </button>
        `;

        contenedor.appendChild(div);

        const combo = document.getElementById(`parada${indice}`);

        ciudades.forEach(c => {
            const estaSeleccionada = (p && p.id === c.id) ? "selected" : "";
            combo.innerHTML += `
                <option value="${c.id}" ${estaSeleccionada}>
                    ${c.direccionTexto}
                </option>
            `;
        });

        combo.addEventListener("change", () => {
            const id = Number(combo.value);

            if (!id) {
                paradas[indice] = null;
                actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA: Actualiza si se limpia la selección
                return;
            }

            paradas[indice] = ciudades.find(c => c.id === id);
            enfocarCiudad(paradas[indice]);
            actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA: Prende el pin Naranja
        });
    });
}

function actualizarOrigen(){
    const id = Number(document.getElementById("origenSelect").value);

    if(!id){
        origen = null;
        actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA
        return;
    }

    origen = ciudades.find(c => c.id === id);
    enfocarCiudad(origen);
    actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA: Prende el pin Verde

    if (origen && destino && origen.id === destino.id) {
        alert("La ciudad de destino no puede ser igual a la de origen.");
        document.getElementById("destinoSelect").value = "";
        destino = null;
        actualizarMarcadoresVisibles();
        return;
    }
}

function actualizarDestino(){
    const id = Number(document.getElementById("destinoSelect").value);

    if(!id){
        destino = null;
        actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA
        return;
    }

    destino = ciudades.find(c => c.id === id);
    enfocarCiudad(destino);
    actualizarMarcadoresVisibles(); // <--- LLAMADA AGREGADA: Prende el pin Rojo
}

function enfocarCiudad(ciudad){
    map.panTo({
        lat: ciudad.latitud,
        lng: ciudad.longitud
    });
    map.setZoom(12);
}

function actualizarMarcadoresVisibles() {
    // 1. Apagamos absolutamente todos los marcadores primero
    marcadores.forEach(marcador => {
        marcador.setMap(null);
    });

    // Función auxiliar para reescalar iconos y que Google Maps no los bloquee
    const crearIcono = (url) => ({
        url: url,
        scaledSize: new google.maps.Size(34, 34)
    });

    // 2. Definición de URLs e iconos estructurados
    const iconoVerde = crearIcono("https://maps.google.com/mapfiles/ms/icons/green-dot.png");
    const iconoNaranja = crearIcono("https://maps.google.com/mapfiles/ms/icons/orange-dot.png");
    const iconoRojo = crearIcono("https://maps.google.com/mapfiles/ms/icons/red-dot.png");

    // 3. Procesamos el origen (Verde)
    if (origen && marcadores.has(origen.id)) {
        const mOrigen = marcadores.get(origen.id);
        mOrigen.setIcon(iconoVerde);
        mOrigen.setMap(map);
    }

    // 4. Procesamos las paradas intermedias (Naranja)
    paradas.forEach(p => {
        if (p && marcadores.has(p.id)) {
            const mParada = marcadores.get(p.id);
            mParada.setIcon(iconoNaranja);
            mParada.setMap(map);
        }
    });

    // 5. Procesamos el destino (Rojo)
    if (destino && marcadores.has(destino.id)) {
        const mDestino = marcadores.get(destino.id);
        mDestino.setIcon(iconoRojo);
        mDestino.setMap(map);
    }
}