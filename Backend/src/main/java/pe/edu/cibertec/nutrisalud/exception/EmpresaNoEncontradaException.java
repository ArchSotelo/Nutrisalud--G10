package pe.edu.cibertec.nutrisalud.exception;

public class EmpresaNoEncontradaException extends RuntimeException {
    public EmpresaNoEncontradaException(Long idEmpresa) {
        super("No existe una empresa con ID " + idEmpresa);
    }
}
