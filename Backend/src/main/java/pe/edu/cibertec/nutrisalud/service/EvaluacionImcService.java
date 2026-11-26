package pe.edu.cibertec.nutrisalud.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcRequestDTO;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcResponseDTO;
import pe.edu.cibertec.nutrisalud.entity.ClasificacionImc;
import pe.edu.cibertec.nutrisalud.entity.EvaluacionImc;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;
import pe.edu.cibertec.nutrisalud.entity.Usuario;
import pe.edu.cibertec.nutrisalud.exception.MedicoNoEncontradoException;
import pe.edu.cibertec.nutrisalud.exception.TrabajadorNoEncontradoException;
import pe.edu.cibertec.nutrisalud.mapper.EvaluacionImcMapper;
import pe.edu.cibertec.nutrisalud.repository.EvaluacionImcRepository;
import pe.edu.cibertec.nutrisalud.repository.TrabajadorRepository;
import pe.edu.cibertec.nutrisalud.repository.UsuarioRepository;

@Service
public class EvaluacionImcService {
    private final TrabajadorRepository trabajadores;
    private final UsuarioRepository usuarios;
    private final EvaluacionImcRepository evaluaciones;
    private final EvaluacionImcMapper mapper;

    public EvaluacionImcService(TrabajadorRepository trabajadores, UsuarioRepository usuarios,
            EvaluacionImcRepository evaluaciones, EvaluacionImcMapper mapper) {
        this.trabajadores = trabajadores;
        this.usuarios = usuarios;
        this.evaluaciones = evaluaciones;
        this.mapper = mapper;
    }

    @Transactional
    public EvaluacionImcResponseDTO registrar(EvaluacionImcRequestDTO solicitud) {
        Trabajador trabajador = trabajadores.buscarConBloqueoDeLectura(solicitud.idTrabajador())
                .orElseThrow(() -> new TrabajadorNoEncontradoException(solicitud.idTrabajador()));
        Usuario medico = usuarios.findByIdAndRolAndEstadoTrue(solicitud.idUsuario(), "MEDICO")
                .orElseThrow(() -> new MedicoNoEncontradoException(solicitud.idUsuario()));

        BigDecimal imc = solicitud.pesoKg().divide(solicitud.tallaM().pow(2), 2, RoundingMode.HALF_UP);
        LocalDateTime fechaUtc = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
        EvaluacionImc evaluacion = new EvaluacionImc(trabajador, medico, fechaUtc, solicitud.pesoKg(),
                solicitud.tallaM(), imc, ClasificacionImc.segunImc(imc), solicitud.observaciones());

        return mapper.aRespuesta(evaluaciones.save(evaluacion));
    }
}
