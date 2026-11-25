package com.ejemplo.tienda.facturacion.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import org.junit.jupiter.api.Test;

class FacturacionServiceTest {

    private final FacturacionService facturacion = new FacturacionService("C-999");

    @Test
    void invoicesAreNumberedConsecutively() {
        assertThat(facturacion.emitir(new PedidoConfirmado(1L, "C-001", 360_000))).isEqualTo("F-0001");
        assertThat(facturacion.emitir(new PedidoConfirmado(2L, "C-002", 441_600))).isEqualTo("F-0002");
    }

    @Test
    void aFailedInvoiceDoesNotUseANumber() {
        assertThat(facturacion.emitir(new PedidoConfirmado(1L, "C-001", 360_000))).isEqualTo("F-0001");

        assertThatThrownBy(() -> facturacion.emitir(new PedidoConfirmado(2L, "C-999", 360_000)))
                .isInstanceOf(FacturaFallidaException.class);

        assertThat(facturacion.emitir(new PedidoConfirmado(3L, "C-002", 441_600))).isEqualTo("F-0002");
    }
}
