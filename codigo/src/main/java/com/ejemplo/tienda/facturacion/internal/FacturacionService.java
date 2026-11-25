package com.ejemplo.tienda.facturacion.internal;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Issues invoices F-0001, F-0002... The consecutive only moves forward when the
 * invoice is really issued. It lives in memory (stated in the ADR as a loss).
 */
@Service
class FacturacionService {

    private final String clienteQueFalla;
    private int ultimoConsecutivo = 0;

    FacturacionService(@Value("${tienda.facturacion.cliente-que-falla:C-999}") String clienteQueFalla) {
        this.clienteQueFalla = clienteQueFalla;
    }

    /** Returns the invoice number, or throws FacturaFallidaException without using a number. */
    synchronized String emitir(PedidoConfirmado pedido) {
        if (clienteQueFalla.equals(pedido.clienteId())) {
            throw new FacturaFallidaException(pedido.clienteId());
        }
        ultimoConsecutivo++;
        return String.format("F-%04d", ultimoConsecutivo);
    }
}
