# Tienda — monolito modular (Q2 / fila 2)

Requisitos: Java 21 y Maven (`mvn`). Las muestras usan H2 en memoria: no necesitan Docker y cada arranque es limpio.
Todos los comandos se corren dentro de `codigo/`.

## Pruebas (incluye la regla de arquitectura)

    mvn test

La regla de arquitectura (`ApplicationModules.verify()`) está en `ModularityTests`:

    mvn -Dtest=ModularityTests test

## Muestra de la Hoja 1 (arranque limpio)

    mvn -q spring-boot:run -Dspring-boot.run.profiles=h2,muestra1

Salida esperada (fila 2). Las líneas PEDIDO son las mismas de la Hoja 1; desde la Hoja 2,
cada pedido CONFIRMADO va seguido de su factura y su aviso:

    PEDIDO 1 CONFIRMADO total=360000
    FACTURA F-0001 pedido=1 cliente=C-001 total=360000
    AVISO cliente=C-001
    PEDIDO 2 CONFIRMADO total=441600
    FACTURA F-0002 pedido=2 cliente=C-002 total=441600
    AVISO cliente=C-002
    PEDIDO 3 RECHAZADO total=0
    PEDIDO 4 CONFIRMADO total=2539200
    FACTURA F-0003 pedido=4 cliente=C-003 total=2539200
    AVISO cliente=C-003

## Muestra de la Hoja 2 · Q2 (arranque limpio, corrida aparte)

    mvn -q spring-boot:run -Dspring-boot.run.profiles=h2,muestra2

Salida esperada (fila 2):

    PEDIDO 1 CONFIRMADO total=360000
    FACTURA F-0001 pedido=1 cliente=C-001 total=360000
    AVISO cliente=C-001
    PEDIDO 2 CONFIRMADO total=360000
    FACTURA FALLO pedido=2 cliente=C-999
    AVISO cliente=C-999
    PEDIDO 3 CONFIRMADO total=441600
    FACTURA F-0002 pedido=3 cliente=C-002 total=441600
    AVISO cliente=C-002
