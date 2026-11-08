package com.petsalud.backendpetsalud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba básica: verifica que el contexto de Spring arranca correctamente.
 * Requiere una base de datos MySQL accesible con los datos de application.properties.
 */
@SpringBootTest
class BackendpetsaludApplicationTests {

	// Si el contexto (incluida la configuración de seguridad) no puede cargar, la prueba falla.
	@Test
	void contextLoads() {
	}

}
