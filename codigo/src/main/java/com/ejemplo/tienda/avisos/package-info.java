/**
 * Notices module: tells the client that the order was confirmed (simulated
 * with a printed line). Independent of invoicing.
 */
@ApplicationModule(displayName = "Avisos", allowedDependencies = "pedidos")
package com.ejemplo.tienda.avisos;

import org.springframework.modulith.ApplicationModule;
