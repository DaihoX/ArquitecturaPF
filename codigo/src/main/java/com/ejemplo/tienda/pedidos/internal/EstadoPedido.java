package com.ejemplo.tienda.pedidos.internal;

/** Final state of an order. A rejected order is still stored and has an id. */
public enum EstadoPedido {
    CONFIRMADO,
    RECHAZADO
}
