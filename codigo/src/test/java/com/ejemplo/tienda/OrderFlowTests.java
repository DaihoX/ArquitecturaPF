package com.ejemplo.tienda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end flow through HTTP only. It lives outside both modules, so it can
 * only use what any client can use: the public endpoints. Numbers of fila 2
 * (precio 120000, umbral 4, descuento 8 %, stock 30).
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowTests {

    @Autowired
    MockMvc mvc;

    @Test
    void theSampleSequenceOfHoja1HoldsThroughHttp() throws Exception {
        long productId = createProduct("Teclado", "120000.00", 30);

        order("C-001", productId, 3)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(360000));
        order("C-002", productId, 4)
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(441600));
        order("C-001", productId, 24)
                .andExpect(jsonPath("$.estado").value("RECHAZADO"))
                .andExpect(jsonPath("$.total").value(0));
        order("C-003", productId, 23)
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(2539200));

        mvc.perform(get("/api/products/" + productId))
                .andExpect(jsonPath("$.stock").value(0));
    }

    @Test
    void aValidationErrorDoesNotConsumeAnId() throws Exception {
        long productId = createProduct("Teclado", "120000.00", 30);

        long before = idOf(order("C-010", productId, 1));
        order("C-010", productId, 0).andExpect(status().isBadRequest());
        order("C-010", 999_999L, 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"));
        long after = idOf(order("C-010", productId, 1));

        org.assertj.core.api.Assertions.assertThat(after).isEqualTo(before + 1);
    }

    @Test
    void aClientTotalSumsOnlyTheConfirmedOrders() throws Exception {
        long productId = createProduct("Teclado", "120000.00", 30);

        order("C-TOT", productId, 4).andExpect(status().isCreated()); // 441600
        order("C-TOT", productId, 3).andExpect(status().isCreated()); // 360000
        order("C-TOT", productId, 500)                                 // RECHAZADO
                .andExpect(jsonPath("$.estado").value("RECHAZADO"));

        mvc.perform(get("/api/orders/clientes/C-TOT/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(801600));
    }

    @Test
    void anExistingOrderCanBeReadBack() throws Exception {
        long productId = createProduct("Monitor", "600000.00", 4);
        long orderId = idOf(order("C-020", productId, 2));

        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("C-020"))
                .andExpect(jsonPath("$.total").value(1200000));
    }

    @Test
    void aProductWithOrdersCannotBeDeleted() throws Exception {
        long productId = createProduct("Webcam", "60000.00", 5);
        order("C-030", productId, 1).andExpect(status().isCreated());

        mvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Producto en uso"));

        mvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk());
    }

    @Test
    void aProductWithoutOrdersCanBeDeleted() throws Exception {
        long productId = createProduct("Cable HDMI", "9900.00", 5);

        mvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void anUnknownOrderReturns404() throws Exception {
        mvc.perform(get("/api/orders/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Pedido no encontrado"));
    }

    private org.springframework.test.web.servlet.ResultActions order(
            String clienteId, long productId, int quantity) throws Exception {
        return mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":\"" + clienteId + "\",\"productId\":" + productId
                        + ",\"quantity\":" + quantity + "}"));
    }

    private static long idOf(org.springframework.test.web.servlet.ResultActions result)
            throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createProduct(String name, String price, int stock) throws Exception {
        String body = mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"d\","
                                + "\"price\":" + price + ",\"stock\":" + stock
                                + ",\"category\":\"papeleria\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
