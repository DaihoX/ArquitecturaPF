package com.ejemplo.tienda.pedidos.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** RN-1 and RN-2 with the numbers of fila 2: umbral 4, descuento 8 %, precio 120000. */
class CalculadoraTotalTest {

    private final CalculadoraTotal calculadora = new CalculadoraTotal(4, 8);

    @Test
    void belowTheThresholdThereIsNoDiscount() {
        assertThat(calculadora.descuento(3)).isZero();
        assertThat(calculadora.total(120_000, 3)).isEqualTo(360_000);
    }

    @Test
    void theThresholdItselfAlreadyGetsTheDiscount() {
        assertThat(calculadora.descuento(4)).isEqualTo(8);
        assertThat(calculadora.total(120_000, 4)).isEqualTo(441_600);
    }

    @Test
    void aboveTheThresholdKeepsTheDiscount() {
        assertThat(calculadora.total(120_000, 23)).isEqualTo(2_539_200);
    }
}
