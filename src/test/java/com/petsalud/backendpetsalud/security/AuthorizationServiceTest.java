package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Notificacion;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de AuthorizationService (las reglas "un cliente solo accede a lo suyo").
 * Los repositorios se reemplazan por mocks de Mockito: no se usa base de datos.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private MascotaRepository mascotaRepository;
    @Mock private NotificacionRepository notificacionRepository;

    @InjectMocks private AuthorizationService authz;

    private Usuario cliente;
    private Usuario otroCliente;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder().id(1L).email("cliente@test.cl").rol(Rol.CLIENTE).build();
        otroCliente = Usuario.builder().id(2L).email("otro@test.cl").rol(Rol.CLIENTE).build();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    // ---------- esUsuario ----------

    @Test
    @DisplayName("esUsuario: true cuando el id es el del usuario autenticado")
    void esUsuario_mismoId_true() {
        autenticarComo("cliente@test.cl");
        when(usuarioRepository.findByEmail("cliente@test.cl")).thenReturn(Optional.of(cliente));

        assertTrue(authz.esUsuario(1L));
    }

    @Test
    @DisplayName("esUsuario: false cuando el id es de otra persona")
    void esUsuario_otroId_false() {
        autenticarComo("cliente@test.cl");
        when(usuarioRepository.findByEmail("cliente@test.cl")).thenReturn(Optional.of(cliente));

        assertFalse(authz.esUsuario(2L));
    }

    @Test
    @DisplayName("esUsuario: false si nadie está autenticado (y no consulta la base de datos)")
    void esUsuario_sinAutenticacion_false() {
        assertFalse(authz.esUsuario(1L));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("esUsuario: false si el id es nulo")
    void esUsuario_idNulo_false() {
        autenticarComo("cliente@test.cl");

        assertFalse(authz.esUsuario(null));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("esUsuario: false si el email del token ya no existe en la base de datos")
    void esUsuario_usuarioEliminado_false() {
        autenticarComo("eliminado@test.cl");
        when(usuarioRepository.findByEmail("eliminado@test.cl")).thenReturn(Optional.empty());

        assertFalse(authz.esUsuario(1L));
    }

    // ---------- esDuenoMascota ----------

    @Test
    @DisplayName("esDuenoMascota: true para el propietario")
    void esDuenoMascota_propietario_true() {
        autenticarComo("cliente@test.cl");
        when(mascotaRepository.findById(10L))
                .thenReturn(Optional.of(Mascota.builder().id(10L).propietario(cliente).build()));

        assertTrue(authz.esDuenoMascota(10L));
    }

    @Test
    @DisplayName("esDuenoMascota: false para una mascota de otro cliente")
    void esDuenoMascota_mascotaAjena_false() {
        autenticarComo("cliente@test.cl");
        when(mascotaRepository.findById(20L))
                .thenReturn(Optional.of(Mascota.builder().id(20L).propietario(otroCliente).build()));

        assertFalse(authz.esDuenoMascota(20L));
    }

    @Test
    @DisplayName("esDuenoMascota: false si la mascota no tiene propietario")
    void esDuenoMascota_sinPropietario_false() {
        autenticarComo("cliente@test.cl");
        when(mascotaRepository.findById(30L)).thenReturn(Optional.of(Mascota.builder().id(30L).build()));

        assertFalse(authz.esDuenoMascota(30L));
    }

    @Test
    @DisplayName("esDuenoMascota: false si la mascota no existe")
    void esDuenoMascota_noExiste_false() {
        autenticarComo("cliente@test.cl");
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        assertFalse(authz.esDuenoMascota(99L));
    }

    @Test
    @DisplayName("esDuenoMascota: false sin autenticación")
    void esDuenoMascota_sinAutenticacion_false() {
        assertFalse(authz.esDuenoMascota(10L));
        verifyNoInteractions(mascotaRepository);
    }

    // ---------- esDuenoNotificacion ----------

    @Test
    @DisplayName("esDuenoNotificacion: true para el destinatario")
    void esDuenoNotificacion_destinatario_true() {
        autenticarComo("cliente@test.cl");
        when(notificacionRepository.findById(5L))
                .thenReturn(Optional.of(Notificacion.builder().id(5L).usuario(cliente).build()));

        assertTrue(authz.esDuenoNotificacion(5L));
    }

    @Test
    @DisplayName("esDuenoNotificacion: false para una notificación de otro usuario")
    void esDuenoNotificacion_ajena_false() {
        autenticarComo("cliente@test.cl");
        when(notificacionRepository.findById(6L))
                .thenReturn(Optional.of(Notificacion.builder().id(6L).usuario(otroCliente).build()));

        assertFalse(authz.esDuenoNotificacion(6L));
    }

    @Test
    @DisplayName("esDuenoNotificacion: false si el id es nulo")
    void esDuenoNotificacion_idNulo_false() {
        autenticarComo("cliente@test.cl");

        assertFalse(authz.esDuenoNotificacion(null));
        verifyNoInteractions(notificacionRepository);
    }
}
