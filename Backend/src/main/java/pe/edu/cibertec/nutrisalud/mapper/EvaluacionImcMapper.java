package pe.edu.cibertec.nutrisalud.mapper;

import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcResponseDTO;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcResponseDTO.IdentificacionTrabajador;
import pe.edu.cibertec.nutrisalud.entity.EvaluacionImc;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;

@Component
public class EvaluacionImcMapper {
    public EvaluacionImcResponseDTO aRespuesta(EvaluacionImc evaluacion) {
        Trabajador trabajador = evaluacion.getTrabajador();
        IdentificacionTrabajador identificacion = new IdentificacionTrabajador(
                trabajador.getNombres(), trabajador.getApellidos());

        return new EvaluacionImcResponseDTO(evaluacion.getId(), trabajador.getId(),
                evaluacion.getUsuario().getId(), evaluacion.getFechaEvaluacion().toInstant(ZoneOffset.UTC),
                evaluacion.getPesoKg(), evaluacion.getTallaM(), evaluacion.getImc(),
                evaluacion.getClasificacion(), evaluacion.getObservaciones(), identificacion);
    }
}
