package pe.edu.cibertec.nutrisalud.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "empresa")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, columnDefinition = "CHAR(11)")
    private String ruc;

    @Column(length = 100)
    private String sector;

    @Column(length = 250)
    private String direccion;

    @Column(length = 25)
    private String telefono;

    @Column(nullable = false)
    private boolean estado;

    protected Empresa() {
    }

    public Long getId() {
        return id;
    }
}
