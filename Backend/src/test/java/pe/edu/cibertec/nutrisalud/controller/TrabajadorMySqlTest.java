package pe.edu.cibertec.nutrisalud.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;
import pe.edu.cibertec.nutrisalud.repository.TrabajadorRepository;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL}",
        "spring.datasource.username=${TEST_DB_USERNAME}",
        "spring.datasource.password=${TEST_DB_PASSWORD}"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".*/nutrisalud_v4_test(\\?.*)?")
class TrabajadorMySqlTest {
    private static final String RUC_PRUEBA = "00000000991";

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private TrabajadorRepository trabajadores;
    private Long empresaId;

    @BeforeEach
    void crearEmpresa() {
        jdbc.update("INSERT INTO empresa (razon_social, ruc) VALUES (?, ?)", "Empresa HU01", RUC_PRUEBA);
        empresaId = jdbc.queryForObject("SELECT id_empresa FROM empresa WHERE ruc = ?", Long.class, RUC_PRUEBA);
    }

    @AfterEach
    void eliminarDatosDeLaPrueba() {
        if (empresaId != null) {
            jdbc.update("DELETE FROM trabajador WHERE id_empresa = ?", empresaId);
            jdbc.update("DELETE FROM empresa WHERE id_empresa = ?", empresaId);
        }
    }

    @Test
    void registraYRecuperaEnOtraTransaccion() throws Exception {
        mvc.perform(post("/api/trabajadores").contentType(APPLICATION_JSON)
                        .content(solicitud(empresaId, "DNI", "\"01234567\"", "ana@example.test")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTrabajador").isNumber())
                .andExpect(jsonPath("$.nroDocumento").value("01234567"))
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.correoVerificado").value(false))
                .andExpect(jsonPath("$.envioAutorizado").value(false));

        Long id = jdbc.queryForObject("SELECT id_trabajador FROM trabajador WHERE id_empresa = ?",
                Long.class, empresaId);
        Trabajador guardado = trabajadores.findById(id).orElseThrow();
        assertEquals("01234567", guardado.getNroDocumento());
        assertEquals("ana@example.test", guardado.getCorreo());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"1234567", "123456789", "0123456a", "１２３４５６７８"})
    void rechazaDniInvalido(String dni) throws Exception {
        String documento = dni == null ? "null" : "\"" + dni + "\"";
        mvc.perform(post("/api/trabajadores").contentType(APPLICATION_JSON)
                        .content(solicitud(empresaId, "DNI", documento, "ana@example.test")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("nroDocumento"));
        assertEquals(0, cantidadRegistrada());
    }

    @Test
    void rechazaSolicitudSinCamposObligatorios() throws Exception {
        mvc.perform(post("/api/trabajadores").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.length()").value(6));
        assertEquals(0, cantidadRegistrada());
    }

    @Test
    void rechazaTipoDeDocumentoNumerico() throws Exception {
        String cuerpo = solicitud(empresaId, "DNI", "\"01234567\"", "ana@example.test")
                .replace("\"DNI\"", "0");
        assertEquals(400, registrar(cuerpo));
        assertEquals(0, cantidadRegistrada());
    }

    @Test
    void rechazaCorreoInvalido() throws Exception {
        mvc.perform(post("/api/trabajadores").contentType(APPLICATION_JSON)
                        .content(solicitud(empresaId, "DNI", "\"01234567\"", "correo-invalido")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("correo"));
        assertEquals(0, cantidadRegistrada());
    }

    @Test
    void rechazaEmpresaInexistente() throws Exception {
        assertEquals(404, registrar(solicitud(Long.MAX_VALUE, "DNI", "\"01234567\"", "ana@example.test")));
        assertEquals(0, cantidadRegistrada());
    }

    @Test
    void permiteElMismoNumeroConDistintoTipo() throws Exception {
        assertEquals(201, registrar(solicitud(empresaId, "DNI", "\"01234567\"", "ana@example.test")));
        assertEquals(409, registrar(solicitud(empresaId, "DNI", "\"01234567\"", "ana@example.test")));
        assertEquals(201, registrar(solicitud(empresaId, "CE", "\"01234567\"", "ana@example.test")));
        assertEquals(2, cantidadRegistrada());
    }

    @Test
    void registraUnaSolaFilaConSolicitudesConcurrentes() throws Exception {
        String cuerpo = solicitud(empresaId, "DNI", "\"01234567\"", "ana@example.test");
        CountDownLatch inicio = new CountDownLatch(1);
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var primera = ejecutor.submit(() -> {
                inicio.await();
                return registrar(cuerpo);
            });
            var segunda = ejecutor.submit(() -> {
                inicio.await();
                return registrar(cuerpo);
            });
            inicio.countDown();
            var estados = List.of(primera.get(10, TimeUnit.SECONDS), segunda.get(10, TimeUnit.SECONDS));
            assertEquals(List.of(201, 409), estados.stream().sorted().toList());
        }
        assertEquals(1, cantidadRegistrada());
    }

    @Test
    void listaConsultaYActualizaElTrabajador() throws Exception {
        Long id = crearTrabajador("01234567");
        jdbc.update("UPDATE trabajador SET correo_verificado = TRUE, envio_autorizado = TRUE "
                + "WHERE id_trabajador = ?", id);
        mvc.perform(get("/api/trabajadores")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idTrabajador == %s)].nroDocumento", id)
                        .value(contains("01234567")));

        String edicion = solicitud(empresaId, "DNI", "\"01234567\"", "nuevo@example.test");
        mvc.perform(put("/api/trabajadores/{id}", id).contentType(APPLICATION_JSON).content(edicion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTrabajador").value(id))
                .andExpect(jsonPath("$.correoVerificado").value(false))
                .andExpect(jsonPath("$.envioAutorizado").value(false));

        mvc.perform(get("/api/trabajadores/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@example.test"));
    }

    @Test
    void editarRespetaValidacionYDocumentoUnico() throws Exception {
        Long id = crearTrabajador("01234567");
        crearTrabajador("01234568");

        mvc.perform(put("/api/trabajadores/{id}", id).contentType(APPLICATION_JSON)
                        .content(solicitud(empresaId, "DNI", "\"01234568\"", "ana@example.test")))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/trabajadores/{id}", id).contentType(APPLICATION_JSON)
                        .content(solicitud(empresaId, "DNI", "\"123\"", "ana@example.test")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/trabajadores/{id}", id).contentType(APPLICATION_JSON)
                        .content(solicitud(Long.MAX_VALUE, "DNI", "\"01234567\"", "ana@example.test")))
                .andExpect(status().isNotFound());
        assertEquals("01234567", trabajadores.findById(id).orElseThrow().getNroDocumento());
    }

    @Test
    void eliminaTrabajadorSinReferenciasYDevuelve404EnOperacionesPosteriores() throws Exception {
        Long id = crearTrabajador("01234567");

        mvc.perform(delete("/api/trabajadores/{id}", id)).andExpect(status().isNoContent());
        assertFalse(trabajadores.existsById(id));
        mvc.perform(delete("/api/trabajadores/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/trabajadores/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/trabajadores/{id}", id).contentType(APPLICATION_JSON)
                        .content(solicitud(empresaId, "DNI", "\"01234567\"", "ana@example.test")))
                .andExpect(status().isNotFound());
    }

    @Test
    void conservaTrabajadorConEvaluacionAsociada() throws Exception {
        Long id = crearTrabajador("01234567");
        String correoMedico = "evaluacion.hu01@example.test";
        jdbc.update("""
                INSERT INTO usuario (nombres, apellidos, correo, contrasena_hash, rol)
                VALUES ('Médico', 'Prueba', ?, 'prueba', 'MEDICO')
                """, correoMedico);
        Long medico = jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE correo = ?", Long.class, correoMedico);
        try {
            jdbc.update("""
                    INSERT INTO evaluacion_imc (id_trabajador, id_usuario, peso_kg, talla_m, imc, clasificacion)
                    VALUES (?, ?, 70, 1.75, 22.86, 'NORMAL')
                    """, id, medico);
            mvc.perform(delete("/api/trabajadores/{id}", id)).andExpect(status().isConflict());
            assertTrue(trabajadores.existsById(id));
            assertEquals(1, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM evaluacion_imc WHERE id_trabajador = ?", Integer.class, id));
        } finally {
            jdbc.update("DELETE FROM evaluacion_imc WHERE id_trabajador = ?", id);
            jdbc.update("DELETE FROM usuario WHERE id_usuario = ?", medico);
        }
    }

    @Test
    void buscaDocumentoExactoConCerosInicialesEnDistintosTipos() throws Exception {
        Long id = crearTrabajador("01234567");
        crearTrabajador("01234568");
        assertEquals(201, registrar(solicitud(empresaId, "CE", "\"01234567\"", "ana@example.test")));

        mvc.perform(get("/api/trabajadores/buscar").param("nroDocumento", "01234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trabajadores.length()").value(2))
                .andExpect(jsonPath("$.trabajadores[0].idTrabajador").value(id))
                .andExpect(jsonPath("$.trabajadores[0].tipoDocumento").value("DNI"))
                .andExpect(jsonPath("$.trabajadores[1].tipoDocumento").value("CE"))
                .andExpect(jsonPath("$.mensaje").doesNotExist());
    }

    @Test
    void devuelveMensajeSiLaBusquedaNoTieneCoincidencias() throws Exception {
        crearTrabajador("01234567");

        mvc.perform(get("/api/trabajadores/buscar").param("nroDocumento", "0123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trabajadores.length()").value(0))
                .andExpect(jsonPath("$.mensaje").value("No se encontraron trabajadores con ese documento"));
    }

    @Test
    void rechazaBusquedaSinDocumentoValido() throws Exception {
        mvc.perform(get("/api/trabajadores/buscar")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/trabajadores/buscar").param("nroDocumento", "  "))
                .andExpect(status().isBadRequest());
    }

    private Long crearTrabajador(String documento) throws Exception {
        assertEquals(201, registrar(solicitud(empresaId, "DNI", "\"" + documento + "\"", "ana@example.test")));
        return jdbc.queryForObject("SELECT id_trabajador FROM trabajador WHERE id_empresa = ? AND nro_documento = ?",
                Long.class, empresaId, documento);
    }

    private int registrar(String cuerpo) throws Exception {
        return mvc.perform(post("/api/trabajadores").contentType(APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse().getStatus();
    }

    private int cantidadRegistrada() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM trabajador WHERE id_empresa = ?", Integer.class, empresaId);
    }

    private String solicitud(Long idEmpresa, String tipo, String documento, String correo) {
        return """
                {
                  "idEmpresa": %s,
                  "tipoDocumento": "%s",
                  "nroDocumento": %s,
                  "nombres": "Ana",
                  "apellidos": "Torres",
                  "correo": "%s"
                }
                """.formatted(idEmpresa, tipo, documento, correo);
    }
}
