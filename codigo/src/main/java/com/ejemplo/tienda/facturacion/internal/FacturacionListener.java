package com.ejemplo.tienda.facturacion.internal;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Runs AFTER the order's transaction commits: when it runs, the order is
 * already stored as CONFIRMADO, so a failing invoice can never undo it.
 */
@Component
class FacturacionListener {

    private final FacturacionService facturacion;

    FacturacionListener(FacturacionService facturacion) {
        this.facturacion = facturacion;
    }

    @Order(1)
    @TransactionalEventListener
    void alConfirmarse(PedidoConfirmado pedido) {
        try {
            String numero = facturacion.emitir(pedido);
            System.out.println("FACTURA " + numero + " pedido=" + pedido.pedidoId()
                    + " cliente=" + pedido.clienteId() + " total=" + pedido.total());
        } catch (FacturaFallidaException ex) {
            System.out.println("FACTURA FALLO pedido=" + pedido.pedidoId()
                    + " cliente=" + pedido.clienteId());
        }
    }
}
