package com.ejemplo.tienda.pedidos.internal;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.ProductSummary;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of the orders module. It talks to the catalog ONLY through the
 * public {@link CatalogApi}.
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository repository;
    private final CatalogApi catalog;
    private final CalculadoraTotal calculadora;

    public OrderService(OrderRepository repository, CatalogApi catalog,
                        CalculadoraTotal calculadora) {
        this.repository = repository;
        this.catalog = catalog;
        this.calculadora = calculadora;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public Order findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /** RN-4: sum of the totals of the client's CONFIRMADO orders. */
    public long totalComprado(String clienteId) {
        Long suma = repository.sumaTotal(clienteId, EstadoPedido.CONFIRMADO);
        return suma == null ? 0L : suma;
    }

    /**
     * Registers an order. RN-5 errors (bad quantity, unknown product) throw and
     * save nothing, so they take no id. Lack of stock is NOT an error: the
     * order is saved as RECHAZADO with total 0 and takes no stock (RN-3).
     */
    @Transactional
    public Order create(String clienteId, Long productId, int quantity) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new PedidoInvalidoException("El cliente es obligatorio");
        }
        if (productId == null) {
            throw new PedidoInvalidoException("El producto es obligatorio");
        }
        if (quantity <= 0) {
            throw new PedidoInvalidoException("La cantidad debe ser mayor que cero");
        }
        ProductSummary product = catalog.getProduct(productId); // 404 if it does not exist
        long total = calculadora.total(product.price().longValueExact(), quantity);

        Order order = catalog.tryReserveStock(productId, quantity)
                ? new Order(clienteId, productId, quantity, EstadoPedido.CONFIRMADO, total)
                : new Order(clienteId, productId, quantity, EstadoPedido.RECHAZADO, 0L);
        return repository.save(order);
    }
}
