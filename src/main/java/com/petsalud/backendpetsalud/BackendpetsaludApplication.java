package com.petsalud.backendpetsalud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot.
 * @SpringBootApplication activa la configuración automática, el escaneo de componentes
 * (controladores, servicios, repositorios, seguridad) y la configuración de Spring.
 */
@SpringBootApplication
public class BackendpetsaludApplication {

	// Punto de entrada: levanta el servidor embebido (Tomcat) y el contexto de Spring.
	public static void main(String[] args) {
		SpringApplication.run(BackendpetsaludApplication.class, args);
	}

}
