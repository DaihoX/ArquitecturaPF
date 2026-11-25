package com.ejemplo.tienda.pedidos;

/**
 * Event of the orders module's public API: an order was registered CONFIRMADO.
 * Other modules react to it (invoice, notice) without the orders module
 * knowing they exist.
 */
public record PedidoConfirmado(Long pedidoId, String clienteId, long total) {
}
