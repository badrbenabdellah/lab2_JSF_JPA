package ma.fstt.persistance;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "produit")
@Data

public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id_produit;

    @Column( nullable = true)
    private String nom_produit;
    @Column( nullable = true)
    private String description_produit;

    @Column( nullable = true)
    private float prix_produit;

    @Column( nullable = true)
    private int stock_produit;

}
