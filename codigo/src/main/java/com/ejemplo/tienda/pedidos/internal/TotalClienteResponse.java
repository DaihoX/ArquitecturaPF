package com.ejemplo.tienda.pedidos.internal;

/** RN-4 answer: what a client has bought (sum of CONFIRMADO orders). */
public record TotalClienteResponse(String clienteId, long total) {
}
