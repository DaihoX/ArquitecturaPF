# Informe · Q2 / fila 2 · Monolito modular

## Medición (`git diff --name-status hoja-1 final`)

| Medida | Resultado |
|---|---|
| Archivos creados | 13 (A): `Muestra_Hoja_2.run.xml`, `avisos/package-info.java`, `AvisoListener.java`, `facturacion/package-info.java`, `FacturaFallidaException.java`, `FacturacionListener.java`, `FacturacionService.java`, `PedidoConfirmado.java`, `MuestraHoja2.java`, `RegistroMuestra.java`, `application-muestra2.properties`, `ReaccionesFlowTests.java`, `FacturacionServiceTest.java` |
| Archivos modificados | 3 (M): `LEEME.md`, `MuestraHoja1.java`, `Order.java` |
| ¿Se modificó el archivo o la clase que calcula el total? | No: `CalculadoraTotal.java` no aparece en el diff |
| ¿Se modificó el archivo o la clase que registra el pedido? | No: `OrderService.java` no aparece en el diff (sí cambió la entidad `Order.java`, que ahora publica el evento) |
| ¿Se tocó una prueba de la Hoja 1? | No: ninguna de `OrderServiceTest`, `CalculadoraTotalTest`, `OrderFlowTests` ni `OrderModuleTests` aparece en el diff |

## 1. Predicción contra medición

El ADR-0001 predijo 12 archivos creados y 3 modificados (`Order.java`, `MuestraHoja1.java` y `LEEME.md`), y anticipó que no se tocarían `CalculadoraTotal`, `OrderService` ni las pruebas de la Hoja 1. La tabla confirma los 3 modificados exactos y las tres respuestas en "No", así que la predicción acertó en lo que mide Q2: el flujo de registro no se reabrió. Falló en los creados, 13 contra 12, porque no contamos la configuración de IntelliJ `Muestra_Hoja_2.run.xml`. Es un error pequeño, pero muestra que el diff mide todo el repositorio y no solo las clases de la arquitectura.

## 2. El límite

Si la tienda pasara a emitir la factura en un sistema externo que a veces no responde, y la regla fuera que ninguna factura se puede perder, la arquitectura de eventos con RabbitMQ sería mejor que nuestro monolito modular. En nuestra solución el evento vive en memoria: si el proceso se cae después de guardar el pedido, la factura desaparece, y el consecutivo vuelve a F-0001 al reiniciar, como reconoce el ADR-0002. La guía de eventos, en «El problema que resuelve» y «Ventajas», plantea justamente productores que no esperan a sus consumidores y mensajes que esperan en una cola hasta que alguien los procese. Para ese cambio pagaríamos el broker y la consistencia eventual, pero ganaríamos una factura que sobrevive a una caída.

## 3. Lo que la regla no ve

Para la evidencia agregamos en `OrderService`, del módulo `pedidos`, un método que recibe `com.ejemplo.tienda.catalogo.internal.Product`, una clase interna de otro módulo. `ModularityTests` falló y nombró la dependencia ilegal hacia el `internal` de `catalogo`, así que la regla sí la detectó; después se revirtió con `git checkout`. Una violación que esa regla no detectaría es escribir la lógica de facturación dentro de `pedidos.internal`, por ejemplo un listener que emita la factura desde el propio módulo de pedidos. `ApplicationModules.verify()` solo revisa las dependencias entre módulos, y como todo quedaría dentro de `pedidos` no habría ninguna flecha ilegal. Aun así, se habría perdido la extensibilidad que buscaba Q2.
