package com.ejemplo.tienda.catalogo.internal;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds the single product of the exercise (fila 2): Teclado, $120.000, stock 30.
 * It runs first, so the sample runners can rely on productoId 1.
 */
@Component
@Order(1)
class DataSeeder implements CommandLineRunner {

    private final ProductRepository repository;

    DataSeeder(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return; // data already exists: do not duplicate
        }
        repository.saveAll(List.of(
                new Product("Teclado", "Teclado de la tienda",
                        new BigDecimal("120000"), 30, "perifericos")
        ));
    }
}
