package pe.edu.cibertec.nutrisalud.dto;

import java.math.BigDecimal;
import java.time.Instant;
import pe.edu.cibertec.nutrisalud.entity.ClasificacionImc;

public record EvaluacionImcResponseDTO(
        Long idImc, Long idTrabajador, Long idUsuario, Instant fechaEvaluacion,
        BigDecimal pesoKg, BigDecimal tallaM, BigDecimal imc, ClasificacionImc clasificacion,
        String observaciones, IdentificacionTrabajador trabajador) {

    public record IdentificacionTrabajador(String nombres, String apellidos) {
    }
}
