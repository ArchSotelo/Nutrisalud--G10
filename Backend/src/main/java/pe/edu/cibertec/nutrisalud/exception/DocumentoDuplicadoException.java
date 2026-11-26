package pe.edu.cibertec.nutrisalud.exception;

public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException() {
        super("Ya existe un trabajador con el mismo tipo y número de documento");
    }
}
