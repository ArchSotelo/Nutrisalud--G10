package pe.edu.cibertec.nutrisalud.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trabajador")
public class Trabajador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajador")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 15)
    private TipoDocumento tipoDocumento;

    @Column(name = "nro_documento", nullable = false, length = 20)
    private String nroDocumento;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 254)
    private String correo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 30)
    private String sexo;

    @Column(length = 100)
    private String cargo;

    @Column(nullable = false)
    private boolean estado = true;

    @Column(name = "correo_verificado", nullable = false)
    private boolean correoVerificado;

    @Column(name = "envio_autorizado", nullable = false)
    private boolean envioAutorizado;

    protected Trabajador() {
    }

    public Trabajador(Empresa empresa, TipoDocumento tipoDocumento, String nroDocumento,
            String nombres, String apellidos, String correo, LocalDate fechaNacimiento,
            String sexo, String cargo) {
        actualizarDatos(empresa, tipoDocumento, nroDocumento, nombres, apellidos, correo,
                fechaNacimiento, sexo, cargo);
    }

    public void actualizarDatos(Empresa empresa, TipoDocumento tipoDocumento, String nroDocumento,
            String nombres, String apellidos, String correo, LocalDate fechaNacimiento,
            String sexo, String cargo) {
        if (this.correo != null && !this.correo.equals(correo)) {
            correoVerificado = false;
            envioAutorizado = false;
        }
        this.empresa = empresa;
        this.tipoDocumento = tipoDocumento;
        this.nroDocumento = nroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.cargo = cargo;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public String getCargo() {
        return cargo;
    }

    public boolean isEstado() {
        return estado;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public boolean isEnvioAutorizado() {
        return envioAutorizado;
    }
}
