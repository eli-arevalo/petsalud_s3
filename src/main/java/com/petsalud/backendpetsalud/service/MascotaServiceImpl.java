
package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaServiceImpl implements MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AutorizacionPropietarioService autorizacionPropietarioService;

    @Override
    public Mascota registrarMascota(Mascota mascota, Long propietarioId) {
        autorizacionPropietarioService.validarAccesoAPropietario(propietarioId);

        Usuario propietario = usuarioRepository.findById(propietarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Propietario no encontrado"));

        mascota.setPropietario(propietario);

        return mascotaRepository.save(mascota);
    }

    @Override
    public Optional<Mascota> obtenerMascotaPorId(Long id) {

        return mascotaRepository.findById(id).map(mascota -> {

            // verificamos que el user pueda consultar la mascota
            autorizacionPropietarioService.validarAccesoAPropietario(
                    mascota.getPropietario().getId()
            );

            return mascota;
        });
    }

    @Override
    public Mascota actualizarMascota(Long id, Mascota mascotaActualizada) {

        return mascotaRepository.findById(id).map(mascota -> {

            // validamos el acceso antes de modificar los datos :o
            autorizacionPropietarioService.validarAccesoAPropietario(
                    mascota.getPropietario().getId()
            );

            mascota.setNombre(mascotaActualizada.getNombre());
            mascota.setEspecie(mascotaActualizada.getEspecie());
            mascota.setRaza(mascotaActualizada.getRaza());
            mascota.setFechaNacimiento(mascotaActualizada.getFechaNacimiento());
            mascota.setSexo(mascotaActualizada.getSexo());

            return mascotaRepository.save(mascota);

        }).orElseThrow(() ->
                new IllegalArgumentException("Mascota no encontrada"));
    }

    @Override
    public void eliminarMascota(Long id) {

        // buscamos la mascota antes de eliminarla
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Mascota no encontrada"));

        // recordar verif. los permisos antes de eliminar ojo
        autorizacionPropietarioService.validarAccesoAPropietario(
                mascota.getPropietario().getId()
        );

        mascotaRepository.delete(mascota);
    }

    @Override
    public List<Mascota> listarMascotasPorPropietario(Long propietarioId) {

        //validamo el acceso antes de consultar la lista
        autorizacionPropietarioService.validarAccesoAPropietario(propietarioId);

        return mascotaRepository.findByPropietarioId(propietarioId);
    }
}

