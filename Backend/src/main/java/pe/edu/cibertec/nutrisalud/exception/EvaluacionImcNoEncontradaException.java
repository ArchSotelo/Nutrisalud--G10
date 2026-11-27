package pe.edu.cibertec.nutrisalud.exception;

public class EvaluacionImcNoEncontradaException extends RuntimeException {
    public EvaluacionImcNoEncontradaException(Long id) {
        super("No existe una evaluación de IMC con ID " + id);
    }
}
