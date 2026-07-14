package com.Utc.RutaExpress.service;

import java.util.List;

import com.Utc.RutaExpress.entity.Usuario;

public interface UsuarioService {

    List<Usuario> listarTodos();

    Usuario buscarPorId(Long id);

    Usuario guardar(Usuario usuario);

    Usuario actualizar(Long id, Usuario usuario);

    void eliminar(Long id);

}