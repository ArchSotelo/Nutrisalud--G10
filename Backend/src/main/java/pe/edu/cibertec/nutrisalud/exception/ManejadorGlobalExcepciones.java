package pe.edu.cibertec.nutrisalud.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException excepcion) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Revisa los campos de la solicitud");
        List<ErrorCampo> errores = excepcion.getBindingResult().getFieldErrors().stream()
                .map(this::aErrorCampo)
                .toList();
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoInvalido() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo JSON está ausente, tiene un formato inválido o contiene un valor incompatible");
    }

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    public ProblemDetail empresaNoEncontrada(EmpresaNoEncontradaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ProblemDetail documentoDuplicado(DocumentoDuplicadoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(TrabajadorNoEncontradoException.class)
    public ProblemDetail trabajadorNoEncontrado(TrabajadorNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(TrabajadorConReferenciasException.class)
    public ProblemDetail trabajadorConReferencias(TrabajadorConReferenciasException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(MedicoNoEncontradoException.class)
    public ProblemDetail medicoNoEncontrado(MedicoNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(EvaluacionImcNoEncontradaException.class)
    public ProblemDetail evaluacionImcNoEncontrada(EvaluacionImcNoEncontradaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    private ErrorCampo aErrorCampo(FieldError error) {
        String campo = "dniValido".equals(error.getField()) ? "nroDocumento" : error.getField();
        return new ErrorCampo(campo, error.getDefaultMessage());
    }

    private record ErrorCampo(String campo, String mensaje) {
    }
}
