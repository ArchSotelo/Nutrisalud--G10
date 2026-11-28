package pe.edu.cibertec.nutrisalud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record BusquedaTrabajadoresResponseDTO(
        List<TrabajadorResponseDTO> trabajadores,
        @JsonInclude(JsonInclude.Include.NON_NULL) String mensaje) {
}
