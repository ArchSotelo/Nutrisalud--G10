package pe.edu.cibertec.nutrisalud.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcRequestDTO;
import pe.edu.cibertec.nutrisalud.dto.EvaluacionImcResponseDTO;
import pe.edu.cibertec.nutrisalud.service.EvaluacionImcService;

@RestController
@RequestMapping("/api/evaluaciones-imc")
public class EvaluacionImcController {
    private final EvaluacionImcService servicio;

    public EvaluacionImcController(EvaluacionImcService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluacionImcResponseDTO registrar(@Valid @RequestBody EvaluacionImcRequestDTO solicitud) {
        return servicio.registrar(solicitud);
    }
}
