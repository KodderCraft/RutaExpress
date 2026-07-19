 const ICONS = {
    box:'<path d="M3 8l9-4 9 4-9 4-9-4z"/><path d="M3 8v9l9 4 9-4V8"/><path d="M12 12v9"/>',
    send:'<path d="M4 12l17-8-6 17-3-7-8-2z"/>',
    mapPin:'<path d="M12 21s7-6.5 7-11a7 7 0 1 0-14 0c0 4.5 7 11 7 11z"/><circle cx="12" cy="10" r="2.4"/>',
    truck:'<rect x="2" y="8" width="12" height="8" rx="1"/><path d="M14 11h4l3 3v2h-7z"/><circle cx="6.5" cy="18" r="1.6"/><circle cx="17" cy="18" r="1.6"/>',
    chart:'<path d="M4 20V10"/><path d="M11 20V4"/><path d="M18 20v-7"/>',
    home:'<path d="M4 11.5 12 4l8 7.5"/><path d="M6 10v9h12v-9"/>',
    list:'<path d="M8 6h13M8 12h13M8 18h13"/><circle cx="3.5" cy="6" r="1.3"/><circle cx="3.5" cy="12" r="1.3"/><circle cx="3.5" cy="18" r="1.3"/>',
    search:'<circle cx="10.5" cy="10.5" r="6.5"/><path d="M20 20l-4.3-4.3"/>',
    wallet:'<rect x="3" y="6" width="18" height="13" rx="1.5"/><path d="M3 10h18"/><circle cx="17" cy="14" r="1.1"/>',
    bell:'<path d="M6 10a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6z"/><path d="M10 19a2 2 0 0 0 4 0"/>',
    plus:'<path d="M12 5v14M5 12h14"/>',
    check:'<path d="M4 12.5l5 5L20 6"/>',
    alert:'<path d="M12 4 2 20h20L12 4z"/><path d="M12 10v4"/><path d="M12 17h.01"/>',
    star:'<path d="M12 3l2.7 5.9 6.3.7-4.7 4.3 1.3 6.2L12 17l-5.6 3.1 1.3-6.2-4.7-4.3 6.3-.7z"/>',
    phone:'<path d="M6 3h4l1 5-2.5 2a12 12 0 0 0 5.5 5.5l2-2.5 5 1v4a2 2 0 0 1-2.2 2A17 17 0 0 1 4 6.2 2 2 0 0 1 6 3z"/>',
    clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/>',
    qr:'<rect x="3" y="3" width="6" height="6"/><rect x="15" y="3" width="6" height="6"/><rect x="3" y="15" width="6" height="6"/><path d="M15 15h3v3h-3zM19 19h1.5v1.5H19zM15 20h1v1h-1z"/>',
    user:'<circle cx="12" cy="8" r="3.4"/><path d="M5 20c1-4 4-6 7-6s6 2 7 6"/>'
  };
  
  document.querySelectorAll('i[data-icon]').forEach(el=>{
    const name = el.getAttribute('data-icon');
    const svg = document.createElementNS('http://www.w3.org/2000/svg','svg');
    svg.setAttribute('viewBox','0 0 24 24');
    svg.setAttribute('class','icon');
    svg.innerHTML = ICONS[name] || '';
    el.replaceWith(svg);
  });

  // ===== Pantalla de acceso =====

    let selectedAuthRole = 'remitente';

  document.querySelectorAll('.auth-tab').forEach(tab=>{
    tab.addEventListener('click', ()=>{
      document.querySelectorAll('.auth-tab').forEach(t=>t.classList.remove('active'));
      tab.classList.add('active');
      document.querySelectorAll('.auth-subpanel').forEach(p=>p.classList.toggle('active', p.dataset.authpanel===tab.dataset.authtab));
    });
  });
  
  
  document.querySelectorAll('.role-pick').forEach(pick=>{
    pick.addEventListener('click', ()=>{
        document.querySelectorAll('.role-pick')
            .forEach(p=>p.classList.remove('active'));
        pick.classList.add('active');
        const radio = pick.querySelector("input");
        radio.checked = true;
        selectedAuthRole = pick.dataset.authrole;
        const datosRepartidor = document.querySelectorAll('.datosRepartidor');
        datosRepartidor.forEach(div=>{
            if(radio.value === "REPARTIDOR"){
                div.style.display = "block";
            }else{
                div.style.display = "none";
            }
        });
    });
});


  
  function enterApp(){
    document.getElementById('authScreen').style.display = 'none';
    document.getElementById('mainTopbrand').style.display = 'flex';
    document.getElementById('mainApp').style.display = 'block';
    document.querySelectorAll('.role-tab').forEach(b=>b.classList.toggle('active', b.dataset.role===selectedAuthRole));
    document.querySelectorAll('.app').forEach(a=>a.classList.toggle('active', a.id==='app-'+selectedAuthRole));
  }

  function logout(){
    document.getElementById('mainTopbrand').style.display = 'none';
    document.getElementById('mainApp').style.display = 'none';
    document.getElementById('authScreen').style.display = 'flex';
  }

  // Role switcher
  document.querySelectorAll('.role-tab').forEach(btn=>{
    btn.addEventListener('click', ()=>{
      document.querySelectorAll('.role-tab').forEach(b=>b.classList.remove('active'));
      btn.classList.add('active');
      // document.querySelectorAll('.app').forEach(a=>a.classList.remove('active'));
      // document.getElementById('app-'+btn.dataset.role).classList.add('active');
    });
  });
  // function goPanel(role, panel){
  //   document.querySelectorAll('.role-tab').forEach(b=>b.classList.toggle('active', b.dataset.role===role));
  //   document.querySelectorAll('.app').forEach(a=>a.classList.toggle('active', a.id==='app-'+role));
  //   const scope = document.getElementById('app-'+role);
  //   scope.querySelectorAll('.nav-item').forEach(n=>n.classList.toggle('active', n.dataset.panel===panel));
  //   scope.querySelectorAll(':scope > .main > .panel').forEach(p=>p.classList.toggle('active', p.dataset.panel===panel));
  // }

  // Sidebar nav within each role
  document.querySelectorAll('.app').forEach(app=>{
    const navItems = app.querySelectorAll('.nav-item');
    const panels = app.querySelectorAll(':scope > .main > .panel');
    navItems.forEach(item=>{
      item.addEventListener('click', ()=>{
        navItems.forEach(n=>n.classList.remove('active'));
        item.classList.add('active');
        panels.forEach(p=>p.classList.toggle('active', p.dataset.panel===item.dataset.panel));
      });
    });
  });

  // Subtabs (Usuarios: remitentes/destinatarios)
  document.querySelectorAll('.subtabs').forEach(group=>{
    const tabs = group.querySelectorAll('.subtab');
    const container = group.parentElement;
    const subpanels = container.querySelectorAll('.subpanel');
    tabs.forEach(tab=>{
      tab.addEventListener('click', ()=>{
        tabs.forEach(t=>t.classList.remove('active'));
        tab.classList.add('active');
        subpanels.forEach(p=>p.classList.toggle('active', p.dataset.sub===tab.dataset.sub));
      });
    });
  });

  // Pills (filter look, visual only)
  document.querySelectorAll('.pill-list').forEach(group=>{
    group.querySelectorAll('.pill').forEach(p=>{
      p.addEventListener('click', ()=>{
        group.querySelectorAll('.pill').forEach(x=>x.classList.remove('active'));
        p.classList.add('active');
      });
    });
  });

  // Stars (rating widget, visual only)
  document.querySelectorAll('.stars').forEach(group=>{
    const stars = group.querySelectorAll('.icon');
    stars.forEach((s,i)=>{
      s.addEventListener('click', ()=>{
        stars.forEach((st,j)=>st.classList.toggle('filled', j<=i));
      });
    });
  });

  // Quote reveal
  function showQuote(){
    document.getElementById('quoteCard').style.display = 'block';
  }

  const modalUsuario = document.getElementById('modalUsuario');
  const modalUsuarioAvatar = document.getElementById('modalUsuarioAvatar');
  const modalUsuarioNombre = document.getElementById('modalUsuarioNombre');
  const modalUsuarioRol = document.getElementById('modalUsuarioRol');
  const modalUsuarioId = document.getElementById('modalUsuarioId');
  const modalUsuarioEmail = document.getElementById('modalUsuarioEmail');
  const modalUsuarioTelefono = document.getElementById('modalUsuarioTelefono');
  const modalUsuarioDireccion = document.getElementById('modalUsuarioDireccion');
  const modalUsuarioFecha = document.getElementById('modalUsuarioFecha');

  const modalDestinatario = document.getElementById('modalDestinatario');
  const modalDestinatarioAvatar = document.getElementById('modalDestinatarioAvatar');
  const modalDestinatarioNombre = document.getElementById('modalDestinatarioNombre');
  const modalDestinatarioMeta = document.getElementById('modalDestinatarioMeta');
  const modalDestinatarioDireccion = document.getElementById('modalDestinatarioDireccion');
  const modalDestinatarioTelefono = document.getElementById('modalDestinatarioTelefono');
  const modalDestinatarioHistorial = document.getElementById('modalDestinatarioHistorial');

  const modalRegistroRepartidor = document.getElementById('modalRegistroRepartidor');
  const modalPerfilRepartidor = document.getElementById('modalPerfilRepartidor');
  const modalPerfilAvatar = document.getElementById('modalPerfilAvatar');
  const modalPerfilZona = document.getElementById('modalPerfilZona');
  const modalPerfilEstado = document.getElementById('modalPerfilEstado');
  const modalPerfilEntregas = document.getElementById('modalPerfilEntregas');
  const modalPerfilEstrellas = document.getElementById('modalPerfilEstrellas');
  const modalPerfilVehiculo = document.getElementById('modalPerfilVehiculo');
  const modalPerfilPlaca = document.getElementById('modalPerfilPlaca');
  const modalPerfilPedidos = document.getElementById('modalPerfilPedidos');

  function abrirModalUsuario(button){
    if (!modalUsuario) return;

    const nombre = button.dataset.nombre || 'Usuario';
    const iniciales = nombre
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');

    modalUsuarioAvatar.textContent = iniciales || 'US';
    modalUsuarioNombre.textContent = nombre;
    modalUsuarioRol.textContent = button.dataset.rol || 'Sin rol';
    modalUsuarioId.textContent = button.dataset.id || '-';
    modalUsuarioEmail.textContent = button.dataset.email || '-';
    modalUsuarioTelefono.textContent = button.dataset.telefono || '-';
    modalUsuarioDireccion.textContent = button.dataset.direccion || '-';
    modalUsuarioFecha.textContent = button.dataset.fecha || '-';

    modalUsuario.classList.add('active');
    modalUsuario.setAttribute('aria-hidden', 'false');
  }

  function cerrarModalUsuario(){
    if (!modalUsuario) return;
    modalUsuario.classList.remove('active');
    modalUsuario.setAttribute('aria-hidden', 'true');
  }

  function mostrarDetallesDestinatario(button){
    if (!modalDestinatario) return;

    const nombre = button.dataset.nombre || 'Destinatario';
    const direccion = button.dataset.direccion || 'Sin dirección registrada';
    const telefono = button.dataset.telefono || 'Sin teléfono';
    const historialRaw = button.dataset.historial || '';
    const historial = historialRaw
      .split(';')
      .map(entry => entry.trim())
      .filter(Boolean)
      .map(entry => {
        const [remitente, fecha, estado] = entry.split('|');
        return { remitente: remitente || 'Sin remitente', fecha: fecha || 'Sin fecha', estado: estado || 'Sin estado' };
      });

    const iniciales = nombre
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part.charAt(0).toUpperCase())
      .join('');

    modalDestinatarioAvatar.textContent = iniciales || 'DR';
    modalDestinatarioNombre.textContent = nombre;
    modalDestinatarioMeta.textContent = 'Destinatario';
    modalDestinatarioDireccion.textContent = direccion;
    modalDestinatarioTelefono.textContent = telefono;

    if (modalDestinatarioHistorial) {
      modalDestinatarioHistorial.innerHTML = '';

      if (!historial.length) {
        modalDestinatarioHistorial.innerHTML = '<div class="usuario-modal__item"><span>No hay paquetes recibidos registrados.</span></div>';
      } else {
        historial.forEach(item => {
          const wrapper = document.createElement('div');
          wrapper.className = 'usuario-modal__item';
          wrapper.innerHTML = '<strong>' + item.remitente + '</strong><span>' + item.fecha + ' · ' + item.estado + '</span>';
          modalDestinatarioHistorial.appendChild(wrapper);
        });
      }
    }

    modalDestinatario.classList.add('active');
    modalDestinatario.setAttribute('aria-hidden', 'false');
  }

  function cerrarModalDestinatario(){
    if (!modalDestinatario) return;
    modalDestinatario.classList.remove('active');
    modalDestinatario.setAttribute('aria-hidden', 'true');
  }

  function abrirModalRegistroRepartidor() {
    if (!modalRegistroRepartidor) return;
    modalRegistroRepartidor.classList.add('active');
    modalRegistroRepartidor.setAttribute('aria-hidden', 'false');
  }

  function cerrarModalRegistroRepartidor() {
    if (!modalRegistroRepartidor) return;
    modalRegistroRepartidor.classList.remove('active');
    modalRegistroRepartidor.setAttribute('aria-hidden', 'true');
  }

  function renderEstrellas(valor) {
    if (!modalPerfilEstrellas) return;
    modalPerfilEstrellas.innerHTML = '';
    const rating = Number(valor) || 0;
    for (let i = 1; i <= 5; i++) {
      const star = document.createElement('i');
      star.setAttribute('data-icon', 'star');
      star.classList.add(rating >= i ? 'filled' : '');
      modalPerfilEstrellas.appendChild(star);
    }
    document.querySelectorAll('#modalPerfilEstrellas i[data-icon="star"]').forEach(el => {
      el.classList.add('icon');
      el.innerHTML = '<path d="M12 3l2.7 5.9 6.3.7-4.7 4.3 1.3 6.2L12 17l-5.6 3.1 1.3-6.2-4.7-4.3 6.3-.7z"/>';
    });
  }

  function verPerfilRepartidor(id) {
    const button = document.querySelector(`.btn-ver-perfil-repartidor[data-id="${id}"]`);
    if (!button || !modalPerfilRepartidor) return;

    const nombre = button.dataset.nombre || 'Repartidor';
    const zona = button.dataset.zona || 'Sin zona';
    const estado = button.dataset.estado || 'Disponible';
    const entregas = button.dataset.entregas || '0';
    const calificacion = button.dataset.calificacion || '4.5';
    const vehiculo = button.dataset.vehiculo || 'Moto';
    const placa = button.dataset.placa || 'Sin placa';
    const pedidos = (button.dataset.pedidos || '').split(',').map(item => item.trim()).filter(Boolean);

    const iniciales = nombre.split(' ').filter(Boolean).slice(0, 2).map(part => part.charAt(0).toUpperCase()).join('');
    modalPerfilAvatar.textContent = iniciales || 'RP';
    modalPerfilZona.textContent = 'Zona asignada · ' + zona;
    modalPerfilEstado.textContent = estado;
    modalPerfilEntregas.textContent = entregas;
    modalPerfilVehiculo.textContent = vehiculo;
    modalPerfilPlaca.textContent = placa;
    modalPerfilPedidos.innerHTML = pedidos.length ? pedidos.map(pedido => '<div class="pedido-lista__item">' + pedido + '</div>').join('') : '<div class="pedido-lista__item">Sin pedidos asignados hoy.</div>';
    renderEstrellas(calificacion);

    modalPerfilRepartidor.classList.add('active');
    modalPerfilRepartidor.setAttribute('aria-hidden', 'false');
  }

  function cerrarModalPerfilRepartidor() {
    if (!modalPerfilRepartidor) return;
    modalPerfilRepartidor.classList.remove('active');
    modalPerfilRepartidor.setAttribute('aria-hidden', 'true');
  }

  document.querySelectorAll('.btn-ver-usuario').forEach(button => {
    button.addEventListener('click', () => abrirModalUsuario(button));
  });

  document.querySelectorAll('.btn-ver-destinatario').forEach(button => {
    button.addEventListener('click', () => mostrarDetallesDestinatario(button));
  });

  document.querySelectorAll('.btn-ver-perfil-repartidor').forEach(button => {
    button.addEventListener('click', () => verPerfilRepartidor(button.dataset.id));
  });

  const btnAbrirModalRegistroRepartidor = document.getElementById('btnAbrirModalRegistroRepartidor');
  if (btnAbrirModalRegistroRepartidor) {
    btnAbrirModalRegistroRepartidor.addEventListener('click', abrirModalRegistroRepartidor);
  }

  const formRegistroRepartidor = document.getElementById('formRegistroRepartidor');
  if (formRegistroRepartidor) {
    formRegistroRepartidor.addEventListener('submit', async event => {
      event.preventDefault();
      const feedback = document.getElementById('registroRepartidorFeedback');
      const payload = Object.fromEntries(new FormData(formRegistroRepartidor).entries());

      try {
        const response = await fetch('/api/admin/repartidores', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        const result = await response.json();
        if (feedback) {
          feedback.style.display = 'block';
          feedback.textContent = result.message || 'Registro completado.';
          feedback.style.color = result.success ? 'var(--teal)' : 'var(--coral)';
        }

        if (result.success) {
          formRegistroRepartidor.reset();
          setTimeout(cerrarModalRegistroRepartidor, 700);
        }
      } catch (error) {
        if (feedback) {
          feedback.style.display = 'block';
          feedback.textContent = 'No se pudo registrar el repartidor.';
          feedback.style.color = 'var(--coral)';
        }
      }
    });
  }

  document.querySelectorAll('[data-close="modalUsuario"]').forEach(element => {
    element.addEventListener('click', cerrarModalUsuario);
  });

  document.querySelectorAll('[data-close="modalDestinatario"]').forEach(element => {
    element.addEventListener('click', cerrarModalDestinatario);
  });

  document.querySelectorAll('[data-close="modalRegistroRepartidor"]').forEach(element => {
    element.addEventListener('click', cerrarModalRegistroRepartidor);
  });

  document.querySelectorAll('[data-close="modalPerfilRepartidor"]').forEach(element => {
    element.addEventListener('click', cerrarModalPerfilRepartidor);
  });

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
      if (modalUsuario && modalUsuario.classList.contains('active')) {
        cerrarModalUsuario();
      }
      if (modalDestinatario && modalDestinatario.classList.contains('active')) {
        cerrarModalDestinatario();
      }
      if (modalRegistroRepartidor && modalRegistroRepartidor.classList.contains('active')) {
        cerrarModalRegistroRepartidor();
      }
      if (modalPerfilRepartidor && modalPerfilRepartidor.classList.contains('active')) {
        cerrarModalPerfilRepartidor();
      }
    }
  });