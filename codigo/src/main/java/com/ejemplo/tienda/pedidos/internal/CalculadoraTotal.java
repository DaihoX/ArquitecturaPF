package com.ejemplo.tienda.pedidos.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RN-1 and RN-2 in one place: volume discount and order total, in whole pesos.
 * The defaults are the numbers of fila 2 (umbral 4, descuento 8 %).
 */
@Component
public class CalculadoraTotal {

    private final int umbral;
    private final int porcentaje;

    public CalculadoraTotal(@Value("${tienda.descuento.umbral:4}") int umbral,
                            @Value("${tienda.descuento.porcentaje:8}") int porcentaje) {
        this.umbral = umbral;
        this.porcentaje = porcentaje;
    }

    /** RN-1: cantidad >= umbral gets the discount; below it, nothing. */
    public int descuento(int cantidad) {
        return cantidad >= umbral ? porcentaje : 0;
    }

    /** RN-2: total = cantidad x precio x (100 - descuento) / 100. */
    public long total(long precio, int cantidad) {
        return cantidad * precio * (100 - descuento(cantidad)) / 100;
    }
}
