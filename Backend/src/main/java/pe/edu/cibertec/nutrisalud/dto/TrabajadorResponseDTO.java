package pe.edu.cibertec.nutrisalud.dto;

import java.time.LocalDate;
import pe.edu.cibertec.nutrisalud.entity.TipoDocumento;

public record TrabajadorResponseDTO(
        Long idTrabajador, Long idEmpresa, TipoDocumento tipoDocumento, String nroDocumento,
        String nombres, String apellidos, String correo, LocalDate fechaNacimiento,
        String sexo, String cargo, boolean estado, boolean correoVerificado, boolean envioAutorizado) {
}
