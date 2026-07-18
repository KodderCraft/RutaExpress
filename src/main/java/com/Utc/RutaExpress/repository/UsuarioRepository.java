
package com.Utc.RutaExpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Utc.RutaExpress.entity.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findBynombre(String nombre);
    Optional<Usuario> findByNombreAndTelefono(String nombre, String telefono);
    boolean existsByEmail(String email);
}