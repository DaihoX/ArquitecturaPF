package com.ejemplo.tienda.avisos.internal;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Notice to the client, after the commit and after the invoice attempt. */
@Component
class AvisoListener {

    @Order(2)
    @TransactionalEventListener
    void alConfirmarse(PedidoConfirmado pedido) {
        System.out.println("AVISO cliente=" + pedido.clienteId());
    }
}
