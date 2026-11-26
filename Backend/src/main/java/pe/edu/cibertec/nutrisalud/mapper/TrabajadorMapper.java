package pe.edu.cibertec.nutrisalud.mapper;

import org.springframework.stereotype.Component;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorRequestDTO;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorResponseDTO;
import pe.edu.cibertec.nutrisalud.entity.Empresa;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;

@Component
public class TrabajadorMapper {
    public Trabajador aEntidad(TrabajadorRequestDTO solicitud, Empresa empresa) {
        return new Trabajador(empresa, solicitud.tipoDocumento(), solicitud.nroDocumento(),
                solicitud.nombres(), solicitud.apellidos(), solicitud.correo(),
                solicitud.fechaNacimiento(), solicitud.sexo(), solicitud.cargo());
    }

    public void actualizar(Trabajador trabajador, TrabajadorRequestDTO solicitud, Empresa empresa) {
        trabajador.actualizarDatos(empresa, solicitud.tipoDocumento(), solicitud.nroDocumento(),
                solicitud.nombres(), solicitud.apellidos(), solicitud.correo(),
                solicitud.fechaNacimiento(), solicitud.sexo(), solicitud.cargo());
    }

    public TrabajadorResponseDTO aRespuesta(Trabajador trabajador) {
        return new TrabajadorResponseDTO(trabajador.getId(), trabajador.getEmpresa().getId(),
                trabajador.getTipoDocumento(), trabajador.getNroDocumento(), trabajador.getNombres(),
                trabajador.getApellidos(), trabajador.getCorreo(), trabajador.getFechaNacimiento(),
                trabajador.getSexo(), trabajador.getCargo(), trabajador.isEstado(),
                trabajador.isCorreoVerificado(), trabajador.isEnvioAutorizado());
    }
}
