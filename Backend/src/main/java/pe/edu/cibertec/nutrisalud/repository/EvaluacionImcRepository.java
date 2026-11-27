package pe.edu.cibertec.nutrisalud.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.nutrisalud.entity.EvaluacionImc;

public interface EvaluacionImcRepository extends JpaRepository<EvaluacionImc, Long> {
    List<EvaluacionImc> findAllByTrabajador_IdOrderByFechaEvaluacionDescIdDesc(Long idTrabajador);
}
