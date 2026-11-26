package pe.edu.cibertec.nutrisalud.exception;

public class TrabajadorConReferenciasException extends RuntimeException {
    public TrabajadorConReferenciasException(Long id) {
        super("El trabajador con ID " + id + " tiene registros asociados que impiden eliminarlo");
    }
}
