package com.ejemplo.tienda.pedidos.internal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Sample of Hoja 2, section Q2 (fila 2). Only runs with the "muestra2" profile.
 * Each CONFIRMADO order is followed by its invoice and its notice; C-999 makes
 * the invoice fail while the order stays CONFIRMADO.
 */
@Component
@Profile("muestra2")
@org.springframework.core.annotation.Order(2)
class MuestraHoja2 implements CommandLineRunner {

    // Fila 2: umbral u = 4.
    private static final int U = 4;

    private final RegistroMuestra registro;

    MuestraHoja2(RegistroMuestra registro) {
        this.registro = registro;
    }

    @Override
    public void run(String... args) {
        registro.registrar("C-001", U - 1);  // factura F-0001
        registro.registrar("C-999", U - 1);  // la factura falla, el pedido sigue CONFIRMADO
        registro.registrar("C-002", U);      // factura F-0002: el consecutivo no se gastó
    }
}
