package com.petsalud.backendpetsalud.support;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.security.JwtUtil;
import com.petsalud.backendpetsalud.security.UsuarioDetails;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base común de las pruebas de seguridad que recorren la API completa
 * (filtro JWT + Spring Security + @PreAuthorize + controlador) usando MockMvc.
 *
 * - @SpringBootTest levanta la aplicación real con la base H2 en memoria (src/test/resources/application.properties).
 * - @Transactional deshace los cambios al terminar cada prueba, así cada una parte con los mismos datos.
 * - Antes de cada prueba se crean 4 usuarios (uno por rol, más un segundo cliente), una mascota del cliente
 *   y un producto, y a cada usuario se le genera un JWT real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class SeguridadTestBase {

    protected static final String PASSWORD = "ClavePrueba123!";

    // BCrypt es lento a propósito: la contraseña se cifra una sola vez para todas las pruebas.
    private static String passwordCifrada;

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected MascotaRepository mascotaRepository;
    @Autowired protected ProductoRepository productoRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JwtUtil jwtUtil;

    // Usuarios de prueba (uno por rol + un segundo cliente para probar "solo ve lo suyo").
    protected Usuario admin;
    protected Usuario veterinario;
    protected Usuario cliente;
    protected Usuario otroCliente;

    // Datos que pertenecen a "cliente".
    protected Mascota mascotaCliente;
    protected Producto producto;

    // Tokens JWT de cada usuario.
    protected String tokenAdmin;
    protected String tokenVeterinario;
    protected String tokenCliente;
    protected String tokenOtroCliente;

    @BeforeEach
    void crearDatosDePrueba() {
        if (passwordCifrada == null) {
            passwordCifrada = passwordEncoder.encode(PASSWORD);
        }

        admin = crearUsuario("admin@test.cl", "Ana", Rol.ADMIN);
        veterinario = crearUsuario("vet@test.cl", "Victor", Rol.VETERINARIO);
        cliente = crearUsuario("cliente@test.cl", "Carla", Rol.CLIENTE);
        otroCliente = crearUsuario("otro@test.cl", "Oscar", Rol.CLIENTE);

        mascotaCliente = mascotaRepository.save(Mascota.builder()
                .nombre("Firulais").especie("Perro").raza("Quiltro").sexo("M")
                .propietario(cliente)
                .build());

        producto = productoRepository.save(Producto.builder()
                .nombre("Alimento Premium").descripcion("Saco 15 kg")
                .precio(new BigDecimal("15990")).stock(10).categoria("Alimentos")
                .build());

        tokenAdmin = tokenDe(admin);
        tokenVeterinario = tokenDe(veterinario);
        tokenCliente = tokenDe(cliente);
        tokenOtroCliente = tokenDe(otroCliente);
    }

    protected Usuario crearUsuario(String email, String nombre, Rol rol) {
        return usuarioRepository.save(Usuario.builder()
                .email(email)
                .password(passwordCifrada)
                .nombre(nombre)
                .apellido("Prueba")
                .rol(rol)
                .build());
    }

    /** Genera un JWT real, igual al que entrega POST /api/login. */
    protected String tokenDe(Usuario usuario) {
        return jwtUtil.generateToken(new UsuarioDetails(usuario));
    }

    // ---------- Atajos para hacer solicitudes (token == null significa "sin autenticación") ----------

    protected ResultActions get(String url, String token) throws Exception {
        return mockMvc.perform(conToken(MockMvcRequestBuilders.get(url), token));
    }

    protected ResultActions delete(String url, String token) throws Exception {
        return mockMvc.perform(conToken(MockMvcRequestBuilders.delete(url), token));
    }

    protected ResultActions post(String url, String token, String json) throws Exception {
        return mockMvc.perform(conToken(MockMvcRequestBuilders.post(url), token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions post(String url, String token) throws Exception {
        return mockMvc.perform(conToken(MockMvcRequestBuilders.post(url), token));
    }

    protected ResultActions put(String url, String token, String json) throws Exception {
        return mockMvc.perform(conToken(MockMvcRequestBuilders.put(url), token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions put(String url, String token) throws Exception {
        return mockMvc.perform(conToken(MockMvcRequestBuilders.put(url), token));
    }

    private MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder request, String token) {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request;
    }

    /**
     * La seguridad DEJÓ PASAR la solicitud: la respuesta no es 401 (no autenticado) ni 403 (sin permiso).
     * Se usa en los casos permitidos para que la prueba evalúe solo la seguridad y no el formato de la respuesta.
     */
    protected static ResultMatcher permitido() {
        return result -> assertThat(result.getResponse().getStatus())
                .as("La seguridad debía permitir el acceso (ni 401 ni 403)")
                .isNotIn(401, 403);
    }
}
