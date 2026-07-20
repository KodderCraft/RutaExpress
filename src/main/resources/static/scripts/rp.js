let mapa2;
let mar;

function initMap() {
    // 1. Coordenadas iniciales
    const miUbicacion = { lat: -0.2222, lng: -78.5222 };

    // 2. Iniciar el mapa
    mapa2 = new google.maps.Map(document.getElementById("mapa2"), {
        zoom: 15,
        center: miUbicacion
    });

    // 3. Crear marcador MÓVIL (draggable: true)
    mar = new google.maps.Marker({
        position: miUbicacion,
        map: mapa2,
        title: "Ubicación de entrega",
        draggable: true
    });

    // 4. Evento: Al terminar de arrastrar el marcador (dragend)
    mar.addListener("dragend", function() {
        const lat = mar.getPosition().lat();
        const lng = mar.getPosition().lng();

        alert("Nueva ubicación seleccionada:\nLatitud: " + lat + "\nLongitud: " + lng);
    });

    // 5. Evento: Si solo le hacen clic sin moverlo
    mar.addListener("click", function() {
        const lat = mar.getPosition().lat();
        const lng = mar.getPosition().lng();

        alert("Ubicación actual:\nLatitud: " + lat + "\nLongitud: " + lng);
    });
}

function guarCe() {
    if (!mar) {
        alert("Seleccione una ubicación");
        return;
    }

    // ⚠️ CORRECCIÓN AQUÍ: Usar getPosition() de Google Maps
    let latitud = mar.getPosition().lat();
    let longitud = mar.getPosition().lng();

    if (tipoDireccion === "recogida") {
        puntoRecogida = {
            lat: latitud,
            lng: longitud
        };

        // Guardar coordenadas en campos ocultos del formulario
        if (document.getElementById('latitudRecogida')) {
            document.getElementById('latitudRecogida').value = latitud;
        }
        if (document.getElementById('longitudRecogida')) {
            document.getElementById('longitudRecogida').value = longitud;
        }
        if (document.getElementById('direccionRecogida') && typeof direccionSeleccionada !== 'undefined') {
            document.getElementById('direccionRecogida').value = direccionSeleccionada;
        }

    } else {
        puntoEntrega = {
            lat: latitud,
            lng: longitud
        };

        // Guardar coordenadas en campos ocultos del formulario
        if (document.getElementById('latitudEntrega')) {
            document.getElementById('latitudEntrega').value = latitud;
        }
        if (document.getElementById('longitudEntrega')) {
            document.getElementById('longitudEntrega').value = longitud;
        }
        if (document.getElementById('direccionEntrega') && typeof direccionSeleccionada !== 'undefined') {
            document.getElementById('direccionEntrega').value = direccionSeleccionada;
        }
    }

    // Cerrar el modal
    const modal = document.getElementById("modalMapa");
    if (modal) {
        modal.style.display = "none";
    }

    // Si ya existen ambos puntos, calcular la ruta automáticamente
    if (typeof puntoRecogida !== 'undefined' && typeof puntoEntrega !== 'undefined' && puntoRecogida && puntoEntrega) {
        if (typeof verRuta === "function") {
            verRuta();
        }
    }
}