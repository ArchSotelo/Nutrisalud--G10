package pe.edu.cibertec.nutrisalud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "usuario")
public class Usuario {
    @Id
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean estado;

    protected Usuario() {
    }

    public Long getId() {
        return id;
    }
}
