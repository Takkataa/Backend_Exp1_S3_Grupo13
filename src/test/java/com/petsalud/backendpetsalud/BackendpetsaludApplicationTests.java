package com.petsalud.backendpetsalud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba básica: verifica que el contexto de Spring arranca correctamente.
 * Usa la base H2 en memoria definida en src/test/resources/application.properties (no necesita MySQL).
 */
@SpringBootTest
class BackendpetsaludApplicationTests {

	// Si el contexto (incluida la configuración de seguridad) no puede cargar, la prueba falla.
	@Test
	void contextLoads() {
	}

}
