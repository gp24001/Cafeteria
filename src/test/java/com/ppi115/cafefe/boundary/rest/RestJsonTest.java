package com.ppi115.cafefe.boundary.rest;

import com.ppi115.cafefe.entity.Caja;
import com.ppi115.cafefe.entity.Factura;
import jakarta.json.Json;
import java.io.StringReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RestJsonTest {
    @Test
    void facturaSoloIncluyeElIdentificadorDeLaCaja() {
        UUID idCaja = UUID.randomUUID();
        Caja caja = new Caja(idCaja);
        caja.setNombre("Caja principal");
        caja.setActivo(true);
        Factura factura = new Factura(UUID.randomUUID());
        factura.setIdCaja(caja);
        factura.setCliente("Cliente");
        factura.setFechaFacturacion(Timestamp.from(Instant.parse("2026-11-07T23:36:20.005Z")));
        try (var lector = Json.createReader(new StringReader(RestJson.serializar(List.of(factura))))) {
            var resultado = lector.readArray().getJsonObject(0);
            assertEquals("Cliente", resultado.getString("cliente"));
            assertEquals("2026-11-07T23:36:20.005Z", resultado.getString("fechaFacturacion"));
            var referencia = resultado.getJsonObject("idCaja");
            assertEquals(1, referencia.size());
            assertEquals(idCaja.toString(), referencia.getString("idCaja"));
            assertFalse(referencia.containsKey("nombre"));
        }
    }
}
