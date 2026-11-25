/**
 * Invoicing module: reacts to every confirmed order with a numbered invoice.
 *
 * <p>It only knows the public event of the orders module; the orders module
 * does not know this module exists.
 */
@ApplicationModule(displayName = "Facturación", allowedDependencies = "pedidos")
package com.ejemplo.tienda.facturacion;

import org.springframework.modulith.ApplicationModule;
