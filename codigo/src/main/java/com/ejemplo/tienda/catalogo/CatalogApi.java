package com.ejemplo.tienda.catalogo;

/**
 * Public API of the catalog module: the only door other modules may use.
 * It speaks in {@link ProductSummary} values, never in JPA entities.
 */
public interface CatalogApi {

    /** Returns the public data of a product or throws ProductNotFoundException. */
    ProductSummary getProduct(Long productId);

    /**
     * Takes {@code quantity} units out of stock and returns the product data
     * as it was at that moment. Throws ProductNotFoundException or
     * InsufficientStockException.
     */
    ProductSummary reserveStock(Long productId, int quantity);

    /**
     * Takes {@code quantity} units out of stock ONLY if there are enough and
     * returns true; otherwise changes nothing and returns false. It does not
     * throw for lack of stock, so the caller can register a rejected order in
     * the same transaction. Throws ProductNotFoundException.
     */
    boolean tryReserveStock(Long productId, int quantity);
}
