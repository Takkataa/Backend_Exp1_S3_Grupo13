package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio de mascotas.
 */
@Service
public class MascotaServiceImpl implements MascotaService {

    // Repositorio de mascotas.
    @Autowired
    private MascotaRepository mascotaRepository;

    // Repositorio de usuarios, necesario para buscar al propietario.
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Mascota registrarMascota(Mascota mascota, Long propietarioId) {
        // Se verifica que el propietario exista antes de asociarlo a la mascota.
        Usuario propietario = usuarioRepository.findById(propietarioId)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado"));

        mascota.setPropietario(propietario);
        return mascotaRepository.save(mascota);
    }

    @Override
    public Optional<Mascota> obtenerMascotaPorId(Long id) {
        return mascotaRepository.findById(id);
    }

    @Override
    public Mascota actualizarMascota(Long id, Mascota mascotaActualizada) {
        // Se busca la mascota y se copian solo los campos editables (el propietario no cambia).
        return mascotaRepository.findById(id).map(mascota -> {
            mascota.setNombre(mascotaActualizada.getNombre());
            mascota.setEspecie(mascotaActualizada.getEspecie());
            mascota.setRaza(mascotaActualizada.getRaza());
            mascota.setFechaNacimiento(mascotaActualizada.getFechaNacimiento());
            mascota.setSexo(mascotaActualizada.getSexo());
            return mascotaRepository.save(mascota);
        }).orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
    }

    @Override
    public void eliminarMascota(Long id) {
        mascotaRepository.deleteById(id);
    }

    @Override
    public List<Mascota> listarMascotasPorPropietario(Long propietarioId) {
        return mascotaRepository.findByPropietarioId(propietarioId);
    }
}
