package pe.edu.cibertec.nutrisalud.service;

import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorRequestDTO;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorResponseDTO;
import pe.edu.cibertec.nutrisalud.entity.Empresa;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;
import pe.edu.cibertec.nutrisalud.exception.DocumentoDuplicadoException;
import pe.edu.cibertec.nutrisalud.exception.EmpresaNoEncontradaException;
import pe.edu.cibertec.nutrisalud.exception.TrabajadorConReferenciasException;
import pe.edu.cibertec.nutrisalud.exception.TrabajadorNoEncontradoException;
import pe.edu.cibertec.nutrisalud.mapper.TrabajadorMapper;
import pe.edu.cibertec.nutrisalud.repository.EmpresaRepository;
import pe.edu.cibertec.nutrisalud.repository.TrabajadorRepository;

@Service
public class TrabajadorService {
    private final EmpresaRepository empresas;
    private final TrabajadorRepository trabajadores;
    private final TrabajadorMapper mapper;

    public TrabajadorService(EmpresaRepository empresas, TrabajadorRepository trabajadores,
            TrabajadorMapper mapper) {
        this.empresas = empresas;
        this.trabajadores = trabajadores;
        this.mapper = mapper;
    }

    @Transactional
    public TrabajadorResponseDTO registrar(TrabajadorRequestDTO solicitud) {
        Empresa empresa = buscarEmpresa(solicitud.idEmpresa());

        if (trabajadores.existsByTipoDocumentoAndNroDocumento(
                solicitud.tipoDocumento(), solicitud.nroDocumento())) {
            throw new DocumentoDuplicadoException();
        }

        return guardar(mapper.aEntidad(solicitud, empresa), solicitud.idEmpresa());
    }

    @Transactional(readOnly = true)
    public List<TrabajadorResponseDTO> listar() {
        return trabajadores.findAll(Sort.by("id")).stream()
                .map(mapper::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrabajadorResponseDTO> buscarPorDocumento(String nroDocumento) {
        return trabajadores.findAllByNroDocumentoOrderByIdAsc(nroDocumento).stream()
                .map(mapper::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrabajadorResponseDTO consultar(Long id) {
        return trabajadores.findById(id)
                .map(mapper::aRespuesta)
                .orElseThrow(() -> new TrabajadorNoEncontradoException(id));
    }

    @Transactional
    public TrabajadorResponseDTO actualizar(Long id, TrabajadorRequestDTO solicitud) {
        Trabajador trabajador = trabajadores.buscarConBloqueoDeEscritura(id)
                .orElseThrow(() -> new TrabajadorNoEncontradoException(id));
        Empresa empresa = buscarEmpresa(solicitud.idEmpresa());

        if (trabajadores.existsByTipoDocumentoAndNroDocumentoAndIdNot(
                solicitud.tipoDocumento(), solicitud.nroDocumento(), id)) {
            throw new DocumentoDuplicadoException();
        }

        mapper.actualizar(trabajador, solicitud, empresa);
        return guardar(trabajador, solicitud.idEmpresa());
    }

    @Transactional
    public void eliminar(Long id) {
        try {
            if (trabajadores.eliminarPorId(id) == 0) {
                throw new TrabajadorNoEncontradoException(id);
            }
        } catch (DataIntegrityViolationException excepcion) {
            if (excepcion.getCause() instanceof ConstraintViolationException restriccion
                    && restriccion.getErrorCode() == 1451) {
                throw new TrabajadorConReferenciasException(id);
            }
            throw excepcion;
        }
    }

    private Empresa buscarEmpresa(Long idEmpresa) {
        return empresas.findById(idEmpresa)
                .orElseThrow(() -> new EmpresaNoEncontradaException(idEmpresa));
    }

    private TrabajadorResponseDTO guardar(Trabajador trabajador, Long idEmpresa) {
        try {
            return mapper.aRespuesta(trabajadores.saveAndFlush(trabajador));
        } catch (DataIntegrityViolationException excepcion) {
            if (excepcion.getCause() instanceof ConstraintViolationException restriccion) {
                String nombre = restriccion.getConstraintName();
                if (restriccion.getErrorCode() == 1062 && coincide(nombre, "uq_trabajador_documento")) {
                    throw new DocumentoDuplicadoException();
                }
                // La empresa es la única clave foránea de trabajador en el esquema V4.
                if (restriccion.getErrorCode() == 1452) {
                    throw new EmpresaNoEncontradaException(idEmpresa);
                }
            }
            throw excepcion;
        }
    }

    private boolean coincide(String nombre, String restriccion) {
        return nombre != null && (nombre.equals(restriccion) || nombre.endsWith("." + restriccion));
    }
}
