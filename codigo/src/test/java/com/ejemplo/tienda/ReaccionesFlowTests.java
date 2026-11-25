package com.ejemplo.tienda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Hoja 2 (Q2): reactions to a confirmed order, seen from the outside. */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class ReaccionesFlowTests {

    @Autowired
    MockMvc mvc;

    @Test
    void aConfirmedOrderGetsAnInvoiceAndANotice(CapturedOutput output) throws Exception {
        long productId = createProduct(5);

        long orderId = idOf(order("C-R01", productId, 1));

        assertThat(output.getOut())
                .containsPattern("FACTURA F-\\d{4} pedido=" + orderId + " cliente=C-R01 total=120000")
                .contains("AVISO cliente=C-R01");
    }

    @Test
    void aFailingInvoiceDoesNotRejectTheOrder(CapturedOutput output) throws Exception {
        long productId = createProduct(5);

        long orderId = idOf(order("C-999", productId, 1));

        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        assertThat(output.getOut())
                .contains("FACTURA FALLO pedido=" + orderId + " cliente=C-999")
                .contains("AVISO cliente=C-999");
    }

    @Test
    void aRejectedOrderTriggersNothing(CapturedOutput output) throws Exception {
        long productId = createProduct(1);

        long orderId = idOf(order("C-R02", productId, 2));

        assertThat(output.getOut())
                .doesNotContain("pedido=" + orderId + " ")
                .doesNotContain("AVISO cliente=C-R02");
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

    private long createProduct(int stock) throws Exception {
        String body = mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Teclado\",\"description\":\"d\","
                                + "\"price\":120000.00,\"stock\":" + stock
                                + ",\"category\":\"perifericos\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
