# ADR-0001 · Elección: monolito modular con Spring Modulith

Variante: Q2 / fila 2

- Fecha: 2026-11-24 10:00 PM
- Estado: aceptada

## Contexto

La tienda vende un solo producto: Teclado, $120.000, stock inicial 30, umbral de volumen u = 4 y descuento d = 8 %. El atributo prioritario es la **extensibilidad (Q2)**: cada vez que un pedido se confirma tiene que poder ocurrir algo más (facturar, avisar al cliente) **sin reabrir el flujo que registra el pedido**. La Hoja 2 ya anuncia las dos reacciones: una factura con consecutivo F-0001, F-0002… que solo avanza cuando la factura se emite, y un aviso al cliente; la factura falla siempre para el cliente C-999 y aun así el pedido tiene que quedar CONFIRMADO.

## Alternativas consideradas

- **Capas** — descartada porque, para Q2, cada reacción nueva sería una llamada más dentro del servicio de pedidos: reabrir el flujo de registro cada vez, justo lo que Q2 pide evitar. Además la carpeta de capas no trae pedidos: habría que escribir controlador, servicio, repositorio y entidad de pedidos desde cero para un producto de 30 unidades con una sola regla de descuento (8 % desde 4 unidades).
- **Hexagonal** — descartada porque sus puertos protegen la regla de negocio de la infraestructura (eso es Q3), no de reacciones nuevas: con un puerto por reacción el caso de uso tiene que llamarlas una por una, así que la factura y el aviso modificarían el caso de uso de registrar el pedido. Con una única regla de descuento sobre 30 teclados, el costo de separar `domain`/`application`/`adapter` y de escribir los pedidos desde cero no se recupera.
- **Eventos (bus RabbitMQ)** — es la que mejor encaja con Q2 (sumar consumidores sin tocar al productor) y por eso fue la alternativa más seria. Se descartó porque: (1) el consecutivo F-0001, F-0002 depende del orden de emisión y, con un bus asíncrono, ese o
rden y la salida de la muestra son más difíciles de garantizar; (2) con 30 unidades y pedidos de 3 o 4 teclados no hay volumen que justifique un broker y un contenedor más; (3) habría que adaptar el identificador de texto (PED-001) a numérico, el cliente por correo a `clienteId`, el stock en memoria y la validación síncrona de RN-5. Spring Modulith ya trae eventos de aplicación dentro del mismo proceso, que cubren las dos reacciones de la Hoja 2.

## Decisión

Monolito modular (carpeta `tienda`) con los módulos `catalogo` y `pedidos`. Las reacciones de la Hoja 2 serán módulos nuevos (`facturacion` y `avisos`) que escuchan un evento `PedidoConfirmado` publicado por `pedidos`, sin que `pedidos` los conozca. Persistencia: PostgreSQL del `compose.yaml` para correr la API, y H2 en memoria (perfil `h2`) para las muestras y las pruebas, de modo que cada arranque es limpio. La regla de arquitectura es `ModularityTests` (`ApplicationModules.verify()`).

## Consecuencias

**Se gana:** el pedido y el descuento de stock ocurren en una sola transacción (RN-3 sin eventos de compensación). La regla de dependencias entre módulos se verifica con una prueba que corre en cada `mvn test`. Una reacción nueva es un módulo nuevo y no toca el cálculo del total.

**Se pierde:**
1. Los eventos viajan dentro del mismo proceso y no se guardan en una tabla de publicación: si el proceso se cae justo después de confirmar un pedido, su factura y su aviso se pierden. Además, el consecutivo de factura vivirá en memoria, así que al reiniciar la aplicación vuelve a F-0001.
2. `ModularityTests` solo vigila las fronteras entre módulos de primer nivel: un listener de facturación metido por error dentro de `pedidos.internal` no lo detectaría como violación.

**Predicción del costo de la Hoja 2:**
- Se crean en `main`: `pedidos/PedidoConfirmado.java`, `facturacion/package-info.java`, `facturacion/internal/FacturacionService.java`, `facturacion/internal/FacturaFallidaException.java`, `facturacion/internal/FacturacionListener.java`, `avisos/package-info.java`, `avisos/internal/AvisoListener.java`, `pedidos/internal/RegistroMuestra.java` (registra e imprime un pedido de las muestras), `pedidos/internal/MuestraHoja2.java` y `application-muestra2.properties`. Se crean pruebas nuevas: `FacturacionServiceTest.java` y `ReaccionesFlowTests.java`. Esperamos unos 12 archivos nuevos.
- Se modifican: `pedidos/internal/Order.java`, que publicará el evento cuando el pedido se guarde CONFIRMADO; `pedidos/internal/MuestraHoja1.java`, para que cada línea PEDIDO salga antes que su factura y su aviso; y `LEEME.md`, con el comando de la segunda muestra. Esperamos 3 archivos modificados.
- ¿Se modifica la clase que calcula el total (`CalculadoraTotal`)? **No.**
- ¿Se modifica la clase que registra el pedido (`OrderService`)? **No**: el evento sale de la entidad al guardarse, no del servicio.
- ¿Se toca una prueba de la Hoja 1 (`OrderServiceTest`, `CalculadoraTotalTest`, `OrderFlowTests`, `OrderModuleTests`)? **No.**
