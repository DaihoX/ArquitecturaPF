package com.ejemplo.tienda.pedidos.internal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Sample of Hoja 1 (fila 2). Only runs with the "muestra1" profile. It goes in
 * through the use case (OrderService), after the DataSeeder has created the
 * product, and prints one line per order.
 */
@Component
@Profile("muestra1")
@org.springframework.core.annotation.Order(2)
class MuestraHoja1 implements CommandLineRunner {

    // Fila 2: Teclado, stock inicial 30, umbral u = 4.
    private static final int STOCK = 30;
    private static final int U = 4;

    private final RegistroMuestra registro;

    MuestraHoja1(RegistroMuestra registro) {
        this.registro = registro;
    }

    @Override
    public void run(String... args) {
        registro.registrar("C-001", U - 1);              // justo debajo del umbral
        registro.registrar("C-002", U);                  // el umbral ya cuenta
        registro.registrar("C-001", STOCK - 2 * U + 2);  // se pasa por 1: RECHAZADO
        registro.registrar("C-003", STOCK - 2 * U + 1);  // deja el stock en 0
    }
}
