package com.ejemplo.tienda.pedidos.internal;

import java.time.Instant;

// What the API RETURNS to the client.
public record OrderResponse(
        Long id,
        Instant createdAt,
        String clienteId,
        Long productId,
        int quantity,
        EstadoPedido estado,
        long total
) {

    static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getCreatedAt(),
                order.getClienteId(), order.getProductId(), order.getQuantity(),
                order.getEstado(), order.getTotal());
    }
}
