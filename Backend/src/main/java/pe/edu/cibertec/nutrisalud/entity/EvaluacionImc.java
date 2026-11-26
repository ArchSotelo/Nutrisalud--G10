package pe.edu.cibertec.nutrisalud.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluacion_imc")
public class EvaluacionImc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imc")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_trabajador", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDateTime fechaEvaluacion;

    @Column(name = "peso_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "talla_m", nullable = false, precision = 3, scale = 2)
    private BigDecimal tallaM;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal imc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClasificacionImc clasificacion;

    @Column(length = 1000)
    private String observaciones;

    protected EvaluacionImc() {
    }

    public EvaluacionImc(Trabajador trabajador, Usuario usuario, LocalDateTime fechaEvaluacion,
            BigDecimal pesoKg, BigDecimal tallaM, BigDecimal imc, ClasificacionImc clasificacion,
            String observaciones) {
        this.trabajador = trabajador;
        this.usuario = usuario;
        this.fechaEvaluacion = fechaEvaluacion;
        this.pesoKg = pesoKg;
        this.tallaM = tallaM;
        this.imc = imc;
        this.clasificacion = clasificacion;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public Trabajador getTrabajador() {
        return trabajador;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public BigDecimal getTallaM() {
        return tallaM;
    }

    public BigDecimal getImc() {
        return imc;
    }

    public ClasificacionImc getClasificacion() {
        return clasificacion;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
