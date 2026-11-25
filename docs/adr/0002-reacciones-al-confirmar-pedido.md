# ADR-0002 · Reacciones al confirmar un pedido: módulos que escuchan después del commit

Variante: Q2 / fila 2

- Fecha: 2026-11-24 10:56 PM
- Estado: aceptada (el ADR-0001 se mantiene)

## Contexto

La Hoja 2 pidió que cada pedido CONFIRMADO provoque dos reacciones: una factura con consecutivo F-0001, F-0002… que solo avanza cuando la factura se emite, y un aviso al cliente. La factura falla siempre para el cliente C-999, y aun así su pedido tiene que quedar CONFIRMADO. Con la fila 2, la muestra registra tres pedidos (3 teclados para C-001, 3 para C-999 y 4 para C-002), y el pedido 3 tiene que recibir F-0002 y no F-0003.

## Alternativas consideradas

- **Llamar a facturación y avisos desde `OrderService`** — descartada porque reabre el flujo de registro por cada reacción, justo lo que Q2 pide evitar, y porque la excepción de la factura de C-999 ocurriría dentro de la transacción del pedido: sin un manejo cuidadoso, desharía el pedido 2 y el stock de sus 3 teclados.
- **`@EventListener` síncrono, dentro de la misma transacción** — descartada porque cualquier excepción no atrapada en facturación marcaría la transacción para revertir y el pedido no quedaría CONFIRMADO; la regla "la factura no puede tumbar el pedido" dependería de que nadie olvide un `try`.
- **Listener asíncrono con registro de publicación de eventos (Spring Modulith JPA)** — descartada porque exige otra dependencia y una tabla más para tres pedidos de muestra, y porque el consecutivo F-0001/F-0002 y el orden de las líneas de la muestra se vuelven más difíciles de garantizar con hilos.

## Decisión

El pedido anuncia un evento público `PedidoConfirmado` desde la entidad `Order` (con `@DomainEvents` de Spring Data, que se publica al guardarla), y dos módulos nuevos, `facturacion` y `avisos`, lo escuchan con `@TransactionalEventListener`, que corre **después del commit**. `OrderService` y `CalculadoraTotal` no cambiaron.

## Consecuencias

**Se gana:** sumar una tercera reacción es crear otro módulo que escuche `PedidoConfirmado`, sin tocar `pedidos`. Como las reacciones corren cuando el pedido ya está guardado, una factura que falla (C-999) no puede deshacerlo, y el consecutivo no se consume. `ModularityTests` vigila que `facturacion` y `avisos` solo dependan de la API pública de `pedidos`.

**Se pierde:** el evento viaja en memoria y no queda registrado: si el proceso se cae entre el commit y la reacción, la factura y el aviso de ese pedido se pierden sin rastro, y el consecutivo vive en memoria, así que al reiniciar vuelve a F-0001. La entidad `Order` quedó acoplada a una anotación de Spring Data para publicar su evento. Y las muestras necesitan una transacción externa (`RegistroMuestra`) para que la línea PEDIDO salga antes que sus reacciones; por eso la muestra de la Hoja 1 conserva sus líneas PEDIDO pero ahora imprime también la factura y el aviso de cada pedido confirmado.

**Veredicto sobre lo que el ADR-0001 dijo que se perdería:**
1. *Eventos en memoria y consecutivo en memoria:* **ocurrió tal como se dijo.** No hay tabla de publicación, y el consecutivo se reinicia con la aplicación: dos corridas seguidas de la muestra 2 emiten F-0001 las dos veces.
2. *La regla solo vigila fronteras entre módulos:* **sigue siendo cierto.** Los listeners se pusieron en módulos propios, pero si se hubieran escrito dentro de `pedidos.internal`, `ModularityTests` habría pasado igual.

**Veredicto sobre la predicción:** acertó en lo que importa para Q2. Se modificaron exactamente los 3 archivos predichos (`Order.java`, `MuestraHoja1.java` y `LEEME.md`), y no se tocaron `CalculadoraTotal`, `OrderService` ni ninguna prueba de la Hoja 1. Falló por uno en los creados: se predijeron 12 y el diff midió 13, porque no contamos `codigo/.run/Muestra_Hoja_2.run.xml`, la configuración de IntelliJ para correr la muestra 2. Ese archivo no es parte de la arquitectura, pero el diff lo cuenta.
