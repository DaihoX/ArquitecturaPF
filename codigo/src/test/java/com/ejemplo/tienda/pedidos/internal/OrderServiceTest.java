package com.ejemplo.tienda.pedidos.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ejemplo.tienda.catalogo.CatalogApi;
import com.ejemplo.tienda.catalogo.ProductNotFoundException;
import com.ejemplo.tienda.catalogo.ProductSummary;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Business rules RN-1 to RN-5 with a fake catalog (fila 2: Teclado, 120000). */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private CatalogApi catalog;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(repository, catalog, new CalculadoraTotal(4, 8));
    }

    private void tecladoExiste() {
        when(catalog.getProduct(1L))
                .thenReturn(new ProductSummary(1L, "Teclado", new BigDecimal("120000.00")));
    }

    private void saveDevuelveElMismoPedido() {
        when(repository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void justBelowTheThresholdIsConfirmedWithoutDiscount() {
        tecladoExiste();
        saveDevuelveElMismoPedido();
        when(catalog.tryReserveStock(1L, 3)).thenReturn(true);

        Order order = service.create("C-001", 1L, 3);

        assertThat(order.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(order.getTotal()).isEqualTo(360_000);
    }

    @Test
    void theThresholdItselfIsConfirmedWithDiscount() {
        tecladoExiste();
        saveDevuelveElMismoPedido();
        when(catalog.tryReserveStock(1L, 4)).thenReturn(true);

        Order order = service.create("C-002", 1L, 4);

        assertThat(order.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(order.getTotal()).isEqualTo(441_600);
    }

    @Test
    void lackOfStockSavesARejectedOrderWithTotalZero() {
        tecladoExiste();
        saveDevuelveElMismoPedido();
        when(catalog.tryReserveStock(1L, 24)).thenReturn(false);

        Order order = service.create("C-001", 1L, 24);

        assertThat(order.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
        assertThat(order.getTotal()).isZero();
        verify(repository).save(any(Order.class)); // rejected orders are stored too
    }

    @Test
    void aQuantityOfZeroIsAValidationErrorAndSavesNothing() {
        assertThatThrownBy(() -> service.create("C-001", 1L, 0))
                .isInstanceOf(PedidoInvalidoException.class);

        verify(repository, never()).save(any());
        verifyNoInteractions(catalog);
    }

    @Test
    void anUnknownProductIsAValidationErrorAndSavesNothing() {
        when(catalog.getProduct(99L)).thenThrow(new ProductNotFoundException(99L));

        assertThatThrownBy(() -> service.create("C-001", 99L, 1))
                .isInstanceOf(ProductNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void totalBoughtIsZeroForAClientWithoutConfirmedOrders() {
        when(repository.sumaTotal("C-009", EstadoPedido.CONFIRMADO)).thenReturn(null);

        assertThat(service.totalComprado("C-009")).isZero();
    }

    @Test
    void totalBoughtAddsUpTheConfirmedOrders() {
        when(repository.sumaTotal("C-001", EstadoPedido.CONFIRMADO)).thenReturn(801_600L);

        assertThat(service.totalComprado("C-001")).isEqualTo(801_600);
    }

    @Test
    void findByIdThrowsWhenTheOrderDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }
}
