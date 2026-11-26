package pe.edu.cibertec.nutrisalud.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import pe.edu.cibertec.nutrisalud.entity.TipoDocumento;

public record TrabajadorRequestDTO(
        @NotNull @Positive Long idEmpresa,
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank @Size(max = 20)
        @Pattern(regexp = "[ -~]+", message = "debe contener caracteres ASCII imprimibles")
        String nroDocumento,
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Email @Size(max = 254) String correo,
        LocalDate fechaNacimiento,
        @Size(max = 30) String sexo,
        @Size(max = 100) String cargo) {

    @JsonIgnore
    @AssertTrue(message = "El DNI debe contener exactamente 8 dígitos numéricos")
    public boolean isDniValido() {
        return tipoDocumento != TipoDocumento.DNI
                || nroDocumento == null
                || nroDocumento.matches("[0-9]{8}");
    }
}
