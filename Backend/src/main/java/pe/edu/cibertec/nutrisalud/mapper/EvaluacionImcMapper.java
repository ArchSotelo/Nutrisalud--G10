package pe.edu.cibertec.nutrisalud.mapper;

import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcResponseDTO;
import pe.edu.cibertec.nutrisalud.entity.EvaluacionImc;

@Component
public class EvaluacionImcMapper {
    public EvaluacionImcResponseDTO aRespuesta(EvaluacionImc evaluacion) {
        return new EvaluacionImcResponseDTO(evaluacion.getId(), evaluacion.getTrabajador().getId(),
                evaluacion.getUsuario().getId(), evaluacion.getFechaEvaluacion().toInstant(ZoneOffset.UTC),
                evaluacion.getPesoKg(), evaluacion.getTallaM(), evaluacion.getImc(),
                evaluacion.getClasificacion(), evaluacion.getObservaciones());
    }
}
