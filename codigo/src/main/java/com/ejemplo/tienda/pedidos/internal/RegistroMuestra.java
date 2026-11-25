package com.ejemplo.tienda.pedidos.internal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Used by the sample runners. Registers an order through the use case and
 * prints its PEDIDO line inside the same transaction. The reactions to
 * PedidoConfirmado run after the commit, so they are printed after this line.
 */
@Component
class RegistroMuestra {

    private static final long PRODUCTO_ID = 1L;

    private final OrderService service;
    private final TransactionTemplate transaction;

    RegistroMuestra(OrderService service, PlatformTransactionManager transactionManager) {
        this.service = service;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    void registrar(String clienteId, int cantidad) {
        transaction.executeWithoutResult(status -> {
            Order order = service.create(clienteId, PRODUCTO_ID, cantidad);
            System.out.println("PEDIDO " + order.getId() + " " + order.getEstado()
                    + " total=" + order.getTotal());
        });
    }
}
