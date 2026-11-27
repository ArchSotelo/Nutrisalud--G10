package pe.edu.cibertec.nutrisalud.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL}",
        "spring.datasource.username=${TEST_DB_USERNAME}",
        "spring.datasource.password=${TEST_DB_PASSWORD}"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".*/nutrisalud_v4_test(\\?.*)?")
class EvaluacionImcMySqlTest {
    private static final String RUC = "00000000993";
    private static final String CORREO_MEDICO = "medico.hu03@example.test";
    private static final String DOCUMENTO = "00129003";

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    private Long trabajador;
    private Long medico;

    @BeforeEach
    void preparar() {
        jdbc.update("INSERT INTO empresa (razon_social, ruc) VALUES ('Empresa HU03', ?)", RUC);
        Long empresa = jdbc.queryForObject("SELECT id_empresa FROM empresa WHERE ruc = ?", Long.class, RUC);
        jdbc.update("""
                INSERT INTO trabajador (id_empresa, nro_documento, nombres, apellidos, correo)
                VALUES (?, ?, 'Ana', 'Prueba', 'ana.hu03@example.test')
                """, empresa, DOCUMENTO);
        trabajador = jdbc.queryForObject("SELECT id_trabajador FROM trabajador WHERE id_empresa = ?",
                Long.class, empresa);

        jdbc.update("""
                INSERT INTO usuario (nombres, apellidos, correo, contrasena_hash, rol)
                VALUES ('Médico', 'Prueba', ?, 'prueba', 'MEDICO')
                """, CORREO_MEDICO);
        medico = jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE correo = ?", Long.class, CORREO_MEDICO);
    }

    @AfterEach
    void limpiar() {
        if (trabajador != null) {
            jdbc.update("DELETE FROM evaluacion_imc WHERE id_trabajador = ?", trabajador);
            jdbc.update("DELETE FROM trabajador WHERE id_trabajador = ?", trabajador);
        }
        if (medico != null) {
            jdbc.update("DELETE FROM usuario WHERE id_usuario = ?", medico);
        }
        jdbc.update("DELETE FROM empresa WHERE ruc = ?", RUC);
    }

    @ParameterizedTest
    @CsvSource({
            "73, 1.75, 23.84, NORMAL",
            "73.98, 2.00, 18.50, NORMAL",
            "73.97, 2.00, 18.49, BAJO_PESO",
            "100, 2, 25.00, SOBREPESO",
            "120, 2, 30.00, OBESIDAD"
    })
    void calculaClasificaYGuarda(String peso, String talla, String esperado, String clasificacion) throws Exception {
        mvc.perform(post("/api/evaluaciones-imc").contentType(APPLICATION_JSON)
                        .content(solicitud(trabajador, medico, peso, talla)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idImc").isNumber())
                .andExpect(jsonPath("$.idTrabajador").value(trabajador))
                .andExpect(jsonPath("$.idUsuario").value(medico))
                .andExpect(jsonPath("$.trabajador.nombres").value("Ana"))
                .andExpect(jsonPath("$.trabajador.apellidos").value("Prueba"))
                .andExpect(jsonPath("$.fechaEvaluacion").isString())
                .andExpect(jsonPath("$.imc").value(Double.parseDouble(esperado)))
                .andExpect(jsonPath("$.clasificacion").value(clasificacion));

        BigDecimal guardado = jdbc.queryForObject("SELECT imc FROM evaluacion_imc WHERE id_trabajador = ?",
                BigDecimal.class, trabajador);
        assertEquals(new BigDecimal(esperado), guardado);
    }

    @ParameterizedTest
    @CsvSource({
            "null, 1.75", "70, null", "29.99, 1.75", "300.01, 1.75",
            "70, 0", "70, 2.51", "70.123, 1.75", "70, 1.755", "'\"\"', 1.75"
    })
    void rechazaMedidasInvalidas(String peso, String talla) throws Exception {
        mvc.perform(post("/api/evaluaciones-imc").contentType(APPLICATION_JSON)
                        .content(solicitud(trabajador, medico, peso, talla)))
                .andExpect(status().isBadRequest());
        verificarSinEvaluaciones();
    }

    @Test
    void rechazaReferenciasInvalidas() throws Exception {
        mvc.perform(post("/api/evaluaciones-imc").contentType(APPLICATION_JSON)
                        .content(solicitud(Long.MAX_VALUE, medico, "70", "1.75")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/evaluaciones-imc").contentType(APPLICATION_JSON)
                        .content(solicitud(trabajador, Long.MAX_VALUE, "70", "1.75")))
                .andExpect(status().isNotFound());

        jdbc.update("UPDATE usuario SET estado = FALSE WHERE id_usuario = ?", medico);
        mvc.perform(post("/api/evaluaciones-imc").contentType(APPLICATION_JSON)
                        .content(solicitud(trabajador, medico, "70", "1.75")))
                .andExpect(status().isNotFound());

        jdbc.update("UPDATE usuario SET estado = TRUE, rol = 'ADMIN' WHERE id_usuario = ?", medico);
        mvc.perform(post("/api/evaluaciones-imc").contentType(APPLICATION_JSON)
                        .content(solicitud(trabajador, medico, "70", "1.75")))
                .andExpect(status().isNotFound());
        verificarSinEvaluaciones();
    }

    @Test
    void consultaResultadoGuardadoEIdentificaAlTrabajador() throws Exception {
        jdbc.update("""
                INSERT INTO evaluacion_imc
                    (id_trabajador, id_usuario, peso_kg, talla_m, imc, clasificacion)
                VALUES (?, ?, 100, 2, 25.00, 'SOBREPESO')
                """, trabajador, medico);
        Long id = jdbc.queryForObject("SELECT id_imc FROM evaluacion_imc WHERE id_trabajador = ?",
                Long.class, trabajador);

        mvc.perform(get("/api/evaluaciones-imc/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idImc").value(id))
                .andExpect(jsonPath("$.imc").value(25.0))
                .andExpect(jsonPath("$.clasificacion").value("SOBREPESO"))
                .andExpect(jsonPath("$.idTrabajador").value(trabajador))
                .andExpect(jsonPath("$.trabajador.nombres").value("Ana"))
                .andExpect(jsonPath("$.trabajador.apellidos").value("Prueba"));
    }

    @Test
    void devuelve404ParaUnaEvaluacionInexistente() throws Exception {
        mvc.perform(get("/api/evaluaciones-imc/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe una evaluación de IMC con ID " + Long.MAX_VALUE));
    }

    private void verificarSinEvaluaciones() {
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM evaluacion_imc WHERE id_trabajador = ?",
                Integer.class, trabajador));
    }

    private String solicitud(Long idTrabajador, Long idUsuario, String peso, String talla) {
        return """
                {"idTrabajador": %d, "idUsuario": %d, "pesoKg": %s, "tallaM": %s}
                """.formatted(idTrabajador, idUsuario, peso, talla);
    }
}
