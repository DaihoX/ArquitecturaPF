package com.ejemplo.tienda.facturacion.internal;

/** The invoice could not be issued. No consecutive number is used. */
class FacturaFallidaException extends RuntimeException {

    FacturaFallidaException(String clienteId) {
        super("No se pudo emitir la factura para el cliente " + clienteId);
    }
}
