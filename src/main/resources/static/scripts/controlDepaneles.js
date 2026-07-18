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


  function goPanel(role, panel){
    document.querySelectorAll('.role-tab').forEach(b=>b.classList.toggle('active', b.dataset.role===role));
    document.querySelectorAll('.app').forEach(a=>a.classList.toggle('active', a.id==='app-'+role));
    const scope = document.getElementById('app-'+role);
    scope.querySelectorAll('.nav-item').forEach(n=>n.classList.toggle('active', n.dataset.panel===panel));
    scope.querySelectorAll(':scope > .main > .panel').forEach(p=>p.classList.toggle('active', p.dataset.panel===panel));
  }

  // Sidebar nav within each role
  
  document.querySelectorAll('.app').forEach(app=>{
    const navItems = app.querySelectorAll('.nav-item');
    const panels = app.querySelectorAll(':scope > .main > .panel');
    navItems.forEach(item=>{
      item.addEventListener('click', ()=>{
        navItems.forEach(n=>n.classList.remove('active'));
        item.classList.add('active');
        panels.forEach(p=>p.classList.toggle('active', p.dataset.panel===item.dataset.panel));
        ;
      });
    });
    const panelActivo = app.querySelector(':scope > .main > .panel.active');
  if (panelActivo) {
    const nombrePanel = panelActivo.dataset.panel;
    navItems.forEach(item => {
      item.classList.toggle('active', item.dataset.panel === nombrePanel);
    });
  }
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