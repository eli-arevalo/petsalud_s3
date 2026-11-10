
package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AutorizacionPropietarioService {

    private final UsuarioRepository usuarioRepository;

    public AutorizacionPropietarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void validarAccesoAPropietario(Long propietarioId) {
        Authentication autenticacion =
                SecurityContextHolder.getContext().getAuthentication();

        // Comprobamo que exista un usuario autenticado
        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para acceder a este recurso");
        }

        // buscamos al usuario autenticado mediante su email
        Usuario usuarioAutenticado = usuarioRepository
                .findByEmail(autenticacion.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Usuario autenticado no encontrado"));

        Rol rol = usuarioAutenticado.getRol();

        // Comprobamos que la autoridad autenticada coincida con el rol vigente
        boolean autoridadVigente = autenticacion.getAuthorities()
                .stream()
                .anyMatch(autoridad ->
                        autoridad.getAuthority().equals(rol.name()));

        if (!autoridadVigente) {
            throw new AccessDeniedException(
                    "No tienes permisos para acceder a este recurso");
        }

        // prmitimos el acceso al personal autorizado
        if (rol == Rol.EMPLEADO || rol == Rol.GERENTE) {
            return;
        }

        //un cliente solo puede acceder a sus propios datos 
        if (rol == Rol.CLIENTE
                && Objects.equals(usuarioAutenticado.getId(), propietarioId)) {
            return;
        }

        // Rechazamos cualquier otro acceso
        throw new AccessDeniedException(
                "No puedes acceder a los datos de otro propietario");
    }
}
