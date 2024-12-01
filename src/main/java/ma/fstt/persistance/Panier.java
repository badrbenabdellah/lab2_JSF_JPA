package ma.fstt.persistance;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "panier")
@Data
public class Panier {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id_panier;

    private

}
