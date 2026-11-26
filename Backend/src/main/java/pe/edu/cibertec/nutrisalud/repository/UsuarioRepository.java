package pe.edu.cibertec.nutrisalud.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;
import pe.edu.cibertec.nutrisalud.entity.Usuario;

public interface UsuarioRepository extends Repository<Usuario, Long> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Usuario> findByIdAndRolAndEstadoTrue(Long id, String rol);
}
