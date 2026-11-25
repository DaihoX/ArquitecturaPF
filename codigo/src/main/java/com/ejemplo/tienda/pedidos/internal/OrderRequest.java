package com.ejemplo.tienda.pedidos.internal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// What the client SENDS to create an order (one line per order).
public record OrderRequest(

        @NotBlank(message = "El cliente es obligatorio")
        String clienteId,

        @NotNull(message = "El producto es obligatorio")
        Long productId,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int quantity
) {
}
