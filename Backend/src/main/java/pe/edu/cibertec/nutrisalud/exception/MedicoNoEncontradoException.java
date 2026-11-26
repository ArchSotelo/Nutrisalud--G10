package pe.edu.cibertec.nutrisalud.exception;

public class MedicoNoEncontradoException extends RuntimeException {
    public MedicoNoEncontradoException(Long id) {
        super("No existe un usuario médico activo con ID " + id);
    }
}
