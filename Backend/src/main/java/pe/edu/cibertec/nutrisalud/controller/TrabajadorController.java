package pe.edu.cibertec.nutrisalud.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.nutrisalud.dto.BusquedaTrabajadoresResponseDTO;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorRequestDTO;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorResponseDTO;
import pe.edu.cibertec.nutrisalud.service.TrabajadorService;

@RestController
@RequestMapping("/api/trabajadores")
public class TrabajadorController {
    private final TrabajadorService servicio;

    public TrabajadorController(TrabajadorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<TrabajadorResponseDTO> listar() {
        return servicio.listar();
    }

    @GetMapping("/buscar")
    public BusquedaTrabajadoresResponseDTO buscar(
            @RequestParam @Pattern(regexp = "(?=.*[!-~])[ -~]{1,20}") String nroDocumento) {
        List<TrabajadorResponseDTO> trabajadores = servicio.buscarPorDocumento(nroDocumento);
        String mensaje = trabajadores.isEmpty() ? "No se encontraron trabajadores con ese documento" : null;
        return new BusquedaTrabajadoresResponseDTO(trabajadores, mensaje);
    }

    @GetMapping("/{id}")
    public TrabajadorResponseDTO consultar(@PathVariable Long id) {
        return servicio.consultar(id);
    }

    @PutMapping("/{id}")
    public TrabajadorResponseDTO actualizar(@PathVariable Long id,
            @Valid @RequestBody TrabajadorRequestDTO solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrabajadorResponseDTO registrar(@Valid @RequestBody TrabajadorRequestDTO solicitud) {
        return servicio.registrar(solicitud);
    }
}
