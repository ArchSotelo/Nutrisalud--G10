package pe.edu.cibertec.nutrisalud.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.cibertec.nutrisalud.entity.TipoDocumento;
import pe.edu.cibertec.nutrisalud.entity.Trabajador;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    boolean existsByTipoDocumentoAndNroDocumento(TipoDocumento tipoDocumento, String nroDocumento);

    boolean existsByTipoDocumentoAndNroDocumentoAndIdNot(
            TipoDocumento tipoDocumento, String nroDocumento, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trabajador t where t.id = :id")
    Optional<Trabajador> buscarParaEdicion(@Param("id") Long id);

    @Modifying
    @Query("delete from Trabajador t where t.id = :id")
    int eliminarPorId(@Param("id") Long id);
}
