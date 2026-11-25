package com.ejemplo.tienda.pedidos.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByProductId(Long productId);

    /** Sum of the totals of one client's orders in the given state; null if none. */
    @Query("select sum(o.total) from Order o "
            + "where o.clienteId = :clienteId and o.estado = :estado")
    Long sumaTotal(@Param("clienteId") String clienteId,
                   @Param("estado") EstadoPedido estado);
}
