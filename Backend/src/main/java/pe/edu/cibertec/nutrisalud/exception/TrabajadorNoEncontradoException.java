package pe.edu.cibertec.nutrisalud.exception;

public class TrabajadorNoEncontradoException extends RuntimeException {
    public TrabajadorNoEncontradoException(Long id) {
        super("No existe un trabajador con ID " + id);
    }
}
