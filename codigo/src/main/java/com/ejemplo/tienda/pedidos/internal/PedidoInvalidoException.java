package com.ejemplo.tienda.pedidos.internal;

/** RN-5: a validation error. The order is NOT registered and consumes no id. */
public class PedidoInvalidoException extends RuntimeException {

    public PedidoInvalidoException(String message) {
        super(message);
    }
}
