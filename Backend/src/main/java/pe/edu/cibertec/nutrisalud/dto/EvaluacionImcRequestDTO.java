package pe.edu.cibertec.nutrisalud.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EvaluacionImcRequestDTO(
        @NotNull @Positive Long idTrabajador,
        @NotNull @Positive Long idUsuario,
        @NotNull @DecimalMin("30.00") @DecimalMax("300.00")
        @Digits(integer = 3, fraction = 2) BigDecimal pesoKg,
        @NotNull @DecimalMin("1.00") @DecimalMax("2.50")
        @Digits(integer = 1, fraction = 2) BigDecimal tallaM,
        @Size(max = 1000) String observaciones) {
}
