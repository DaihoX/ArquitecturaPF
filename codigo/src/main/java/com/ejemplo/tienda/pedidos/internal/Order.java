package com.ejemplo.tienda.pedidos.internal;

import com.ejemplo.tienda.pedidos.PedidoConfirmado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

// "orders" because ORDER is a reserved word in SQL.
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, length = 60)
    private String clienteId;

    // A plain number, never a JPA relation to the catalog's entity.
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    // Whole pesos. A rejected order has total 0.
    @Column(nullable = false)
    private long total;

    // Not stored: avoids publishing the event twice if the order is saved again.
    @Transient
    private boolean eventosPublicados;

    protected Order() {
        // empty constructor: JPA needs it to instantiate objects
    }

    public Order(String clienteId, Long productId, int quantity,
                 EstadoPedido estado, long total) {
        this.clienteId = clienteId;
        this.productId = productId;
        this.quantity = quantity;
        this.estado = estado;
        this.total = total;
    }

    /**
     * Spring Data publishes these events right after repository.save(), when
     * the id already exists. Only a CONFIRMADO order announces itself, so
     * OrderService (the registration flow) does not change to add reactions.
     */
    @DomainEvents
    Collection<Object> eventos() {
        if (eventosPublicados || estado != EstadoPedido.CONFIRMADO) {
            return List.of();
        }
        return List.of(new PedidoConfirmado(id, clienteId, total));
    }

    @AfterDomainEventPublication
    void marcarEventosPublicados() {
        this.eventosPublicados = true;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getClienteId() {
        return clienteId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public long getTotal() {
        return total;
    }
}
