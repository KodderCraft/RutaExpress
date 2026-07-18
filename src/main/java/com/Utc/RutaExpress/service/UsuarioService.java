package com.Utc.RutaExpress.service;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.Utc.RutaExpress.repository.UsuarioRepository;
import com.Utc.RutaExpress.DTO.loginValidar;
import com.Utc.RutaExpress.entity.Usuario;
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository; 

    public UsuarioService (UsuarioRepository cliente){
        this.usuarioRepository = cliente; 
    }

    public Usuario guardarCliente(Usuario cliente){
        return usuarioRepository.save(cliente);
    }
    public boolean existeCorreo(String email){
    return usuarioRepository.existsByEmail(email);
}
    public Usuario login(loginValidar login){
        Optional<Usuario> cliente = usuarioRepository.findBynombre(login.getnombre());

        
        if(cliente.isEmpty()){
            return null;
        }

        Usuario usuario = cliente.get();

        if (usuario.getPassword().equals(login.getPassword())){
            return usuario;
        }

        return null;
    }
    
    

}
