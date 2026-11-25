package com.ejemplo.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TiendaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TiendaApplication.class, args);
        // Sample runs (profiles muestra*) print their lines and then end the process.
        if (context.getEnvironment().getProperty("tienda.muestra.salir", Boolean.class, false)) {
            System.exit(SpringApplication.exit(context));
        }
    }
}
