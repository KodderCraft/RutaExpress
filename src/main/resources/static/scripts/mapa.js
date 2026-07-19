

let mapaSeleccion = null;

let mapaRuta = null;

let marcador = null;

let tipoDireccion = "";

let direccionSeleccionada = "";
let puntoRecogida = null;
let puntoEntrega = null;  
let lineaRuta = null;

function abrirMapa(tipo){

    tipoDireccion = tipo;


    document.getElementById("modalMapa")
    .style.display="flex";


    setTimeout(()=>{

        crearMapaSeleccion();

    },300);

}

function crearMapaSeleccion(){


    if(mapaSeleccion){

        mapaSeleccion.invalidateSize();

        return;

    }


    mapaSeleccion = L.map('mapSeleccion')
    .setView(
        [-2.90055,-79.00453],
        14
    );


    L.tileLayer(
        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        {
            attribution:'OpenStreetMap'
        }
    ).addTo(mapaSeleccion);



    mapaSeleccion.on('click',function(e){


        if(marcador){

            mapaSeleccion.removeLayer(marcador);

        }


        marcador =
        L.marker([
            e.latlng.lat,
            e.latlng.lng
        ])

        .addTo(mapaSeleccion);



        obtenerDireccion(
            e.latlng.lat,
            e.latlng.lng
        );


    });





}

function obtenerDireccion(lat,lng){


fetch(
`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=es`
)

.then(res=>res.json())

.then(data=>{


    direccionSeleccionada =
    data.display_name;


    console.log(direccionSeleccionada);


});


}


function guardarDireccion(){


    if(!marcador){

        alert("Seleccione una ubicación");

        return;

    }



    let pos = marcador.getLatLng();


    if(tipoDireccion==="recogida"){
      puntoRecogida={
        lat:pos.lat,
        lng:pos.lng
      };
      // guardar coordenadas en campos ocultos
      document.getElementById('latitudRecogida').value = pos.lat;
      document.getElementById('longitudRecogida').value = pos.lng;
      document.getElementById('direccionRecogida').value = direccionSeleccionada;
    }else{
      puntoEntrega={
        lat:pos.lat,
        lng:pos.lng
      };
      // guardar coordenadas en campos ocultos
      document.getElementById('latitudEntrega').value = pos.lat;
      document.getElementById('longitudEntrega').value = pos.lng;
      document.getElementById('direccionEntrega').value = direccionSeleccionada;
    }


    cerrarMapa();

    // En cuanto el usuario ya eligió recogida Y entrega, se calcula la ruta (distancia,
    // tiempo y costo) automáticamente — no hace falta que presione "Ver ruta" a mano.
    if (puntoRecogida && puntoEntrega) {
        verRuta();
    }

}

function cerrarMapa(){


    document.getElementById("modalMapa")
    .style.display="none";


    if(marcador){

        mapaSeleccion.removeLayer(marcador);

        marcador=null;

    }

}

function crearMapaRuta(){
    if(mapaRuta){

        mapaRuta.invalidateSize();

        return;

    }
    mapaRuta = L.map('mapRuta')
    .setView(
        [-2.90055,-79.00453],
        14
    );


    L.tileLayer(
        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        {
            attribution:'OpenStreetMap'
        }
    )
    .addTo(mapaRuta);

}

let ultimaDistanciaCalculada = null;

// Calcula y muestra en pantalla el costo real del envío = precio por km del tipo de
// servicio elegido × distancia recorrida. Usa las mismas tarifas (TARIFAS_POR_KM,
// inyectadas desde la BD) que aplica el backend, para que lo que se ve en pantalla
// coincida con lo que realmente se va a cobrar.
function calcularCostoEnvio(distanciaKm) {
  const selectTipo = document.getElementById('tipoServicio');
  const tipo = selectTipo ? selectTipo.value : 'estandar';
  const tarifas = window.TARIFAS_POR_KM || { estandar: 0.10, express: 0.25, prioritario: 0.50 };
  const precioPorKm = tarifas[tipo] !== undefined ? tarifas[tipo] : 5.00;
  const costo = (parseFloat(distanciaKm) * precioPorKm).toFixed(2);

  document.getElementById("costoTotal").value = costo;
  const costoRutaEl = document.getElementById("costoRuta");
  if (costoRutaEl) costoRutaEl.innerText = "$" + costo;
  const costoVisibleEl = document.getElementById("costoTotalVisible");
  if (costoVisibleEl) costoVisibleEl.value = "$" + costo;

  return costo;
}

function verRuta() {
  if (!puntoRecogida || !puntoEntrega) {
    alert("Seleccione origen y destino");
    return;
  }

  crearMapaRuta();

  let url = `https://router.project-osrm.org/route/v1/driving/${puntoRecogida.lng},${puntoRecogida.lat};${puntoEntrega.lng},${puntoEntrega.lat}?overview=full&geometries=geojson`;

  fetch(url)
    .then(res => res.json())
    .then(data => {
      // 1. Dibujar la ruta en el mapa
      let coordenadas = data.routes[0].geometry.coordinates;
      let puntos = coordenadas.map(p => [p[1], p[0]]);

      if (lineaRuta) {
        mapaRuta.removeLayer(lineaRuta);
      }

      lineaRuta = L.polyline(puntos, {
        color: "blue",
        weight: 5
      }).addTo(mapaRuta);

      mapaRuta.fitBounds(lineaRuta.getBounds());


      let rutaData = data.routes[0];

      let distancia = (rutaData.distance / 1000).toFixed(2);

      let tiempo = Math.ceil(rutaData.duration / 60);

      document.getElementById("distanciaRuta").innerText = distancia + " km";
      document.getElementById("tiempoRuta").innerText = tiempo + " min";
      document.getElementById("distanciaKm").value = distancia;
      document.getElementById("tiempoEstimadoMin").value = tiempo;
      ultimaDistanciaCalculada = distancia;
      calcularCostoEnvio(distancia);
    })
    .catch(error => {
      console.error("Error al obtener la ruta:", error);
    });
}

function validarPaquete() {
  const tipo = document.getElementById('tipo_modal') ? document.getElementById('tipo_modal').value : '';
  const peso = document.getElementById('peso_modal') ? document.getElementById('peso_modal').value : '';
  const alto = document.getElementById('alto_modal') ? document.getElementById('alto_modal').value : '';

  if (!tipo || !peso || !alto) {
    alert('Completa los campos obligatorios del paquete: tipo, peso y alto.');
    return null; // Cambiamos a null para controlarlo fácil
  }
  
  return { tipo, peso, alto };
}

function guardarDatosPaquete() {
  const datosPaquete = validarPaquete();
  
  // Si la validación falló (es null), detenemos la ejecución
  if (!datosPaquete) return; 

  const { tipo, peso, alto } = datosPaquete;

  // Asignación a los inputs del DTO (Thymeleaf)
  document.getElementById('tipo').value = tipo;
  document.getElementById('peso').value = peso;
  document.getElementById('alto').value = alto;
  document.getElementById('ancho').value = document.getElementById('ancho_modal').value || '0.0';
  document.getElementById('largo').value = document.getElementById('largo_modal').value || '0.0';
  document.getElementById('descripcion').value = document.getElementById('descripcion_modal').value || '';
  
  // Transferir valor declarado si el usuario lo llenó en el modal
  const valorModal = document.getElementById('valor_declarado_modal').value;
  if (valorModal) {
    document.getElementById('valorDeclarado').value = valorModal;
  }

  // Pasar el valor booleano de 'fragil'
  document.getElementById('fragil').value = document.getElementById('fragil_modal').checked;

  // Cerrar modal de forma segura
  try { 
    document.getElementById('modal-paquete').close(); 
  } catch(e) {}

}
// Validación antes de enviar el formulario
document.getElementById('registroEnvioForm').addEventListener('submit', function(e){
    // if(!document.getElementById('latitudRecogida').value || !document.getElementById('latitudEntrega').value){
    //   alert('Selecciona ambas direcciones (recogida y entrega) en el mapa.');
    //   e.preventDefault(); return;
    // }
  
  // if(!document.getElementById('tipo').value || !document.getElementById('peso').value || !document.getElementById('alto').value){
  //   alert('Completa los datos del paquete: tipo, peso y alto.');
  //   e.preventDefault(); return;
  // }

  // El costo real se calcula en verRuta() a partir de la distancia (OSRM). Si el usuario
  // no presiona "Ver ruta" antes de enviar, este campo queda vacío y el backend cobraría
  // el precio plano de la tarifa en vez del costo real del trayecto.
  // if(!document.getElementById('costoTotal').value){
  //   alert('Presiona "Ver ruta" para calcular la distancia y el costo antes de generar el envío.');
  //   e.preventDefault(); return;
  // }

});

// Si el usuario cambia el tipo de servicio después de haber calculado la ruta,
// recalcula el precio mostrado sin necesidad de volver a presionar "Ver ruta".
const selectTipoServicio = document.getElementById('tipoServicio');
if (selectTipoServicio) {
  selectTipoServicio.addEventListener('change', function () {
    if (ultimaDistanciaCalculada !== null) {
      calcularCostoEnvio(ultimaDistanciaCalculada);
    }
  });
}
