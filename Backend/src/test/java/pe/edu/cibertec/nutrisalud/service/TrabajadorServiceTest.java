package pe.edu.cibertec.nutrisalud.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorRequestDTO;
import pe.edu.cibertec.nutrisalud.dto.TrabajadorResponseDTO;
import pe.edu.cibertec.nutrisalud.entity.Empresa;
import pe.edu.cibertec.nutrisalud.entity.TipoDocumento;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;
import pe.edu.cibertec.nutrisalud.exception.DocumentoDuplicadoException;
import pe.edu.cibertec.nutrisalud.exception.EmpresaNoEncontradaException;
import pe.edu.cibertec.nutrisalud.mapper.TrabajadorMapper;
import pe.edu.cibertec.nutrisalud.repository.EmpresaRepository;
import pe.edu.cibertec.nutrisalud.repository.TrabajadorRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrabajadorServiceTest {
    private final TrabajadorRequestDTO solicitud = new TrabajadorRequestDTO(
            7L, TipoDocumento.DNI, "01234567", "Ana", "Torres", "ana@example.test",
            null, null, null);

    @Mock private EmpresaRepository empresas;
    @Mock private TrabajadorRepository trabajadores;
    @Mock private Empresa empresa;
    private TrabajadorService servicio;

    @BeforeEach
    void preparar() {
        servicio = new TrabajadorService(empresas, trabajadores, new TrabajadorMapper());
    }

    @Test
    void guardaYDevuelveLosDatosConLosValoresIniciales() {
        when(empresas.findById(7L)).thenReturn(Optional.of(empresa));
        when(empresa.getId()).thenReturn(7L);
        when(trabajadores.saveAndFlush(any(Trabajador.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        TrabajadorResponseDTO respuesta = servicio.registrar(solicitud);

        assertEquals(7L, respuesta.idEmpresa());
        assertEquals("01234567", respuesta.nroDocumento());
        assertEquals("ana@example.test", respuesta.correo());
        assertTrue(respuesta.estado());
        assertFalse(respuesta.correoVerificado());
        assertFalse(respuesta.envioAutorizado());
        verify(trabajadores).saveAndFlush(any(Trabajador.class));
    }

    @Test
    void rechazaEmpresaInexistente() {
        when(empresas.findById(7L)).thenReturn(Optional.empty());

        assertThrows(EmpresaNoEncontradaException.class, () -> servicio.registrar(solicitud));
        verifyNoInteractions(trabajadores);
    }

    @Test
    void traduceLaClaveForaneaSiLaEmpresaDesapareceAntesDelGuardado() {
        when(empresas.findById(7L)).thenReturn(Optional.of(empresa));
        var errorSql = new SQLException("Violación de clave foránea", "23000", 1452);
        var restriccion = new ConstraintViolationException("Inserción rechazada", errorSql, null);
        when(trabajadores.saveAndFlush(any(Trabajador.class)))
                .thenThrow(new DataIntegrityViolationException("Inserción rechazada", restriccion));

        assertThrows(EmpresaNoEncontradaException.class, () -> servicio.registrar(solicitud));
    }

    @Test
    void rechazaDocumentoDuplicadoSinGuardar() {
        when(empresas.findById(7L)).thenReturn(Optional.of(empresa));
        when(trabajadores.existsByTipoDocumentoAndNroDocumento(TipoDocumento.DNI, "01234567"))
                .thenReturn(true);

        assertThrows(DocumentoDuplicadoException.class, () -> servicio.registrar(solicitud));
        verify(trabajadores, never()).saveAndFlush(any());
    }
}
