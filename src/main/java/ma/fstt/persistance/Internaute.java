package ma.fstt.persistance;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "internaute")
@Data
public class Internaute {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id_internaute;

    @Column( nullable = true)
    private String nom;

    @Column( nullable = true)
    private String email;

    @Column( nullable = true)
    private String adresse;

}
