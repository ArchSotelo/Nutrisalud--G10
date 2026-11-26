package pe.edu.cibertec.nutrisalud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.nutrisalud.entity.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
