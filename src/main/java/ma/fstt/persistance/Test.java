package ma.fstt.persistance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Test {
    public static void main(String[] args) {
        Client client = new Client();
        //client.setNom("Badr");
        //client.setEmail("badr@gmail.com");
        //client.setMot_de_passe("123456789");
        //client.setMot_de_passe("123456789");

        client.setNom("admin");
        client.setEmail("admin@gmail.com");
        client.setMot_de_passe("admin");
        client.setMot_de_passe("admin");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        System.out.println("COMIITING");
        em.persist(client);
        em.getTransaction().commit();

        try {
            em.getTransaction().begin();
            Vitrine vitrine = new Vitrine();
            vitrine.setNom_vitrine("Vitrine Test");
            em.persist(vitrine);
            em.getTransaction().commit();
            System.out.println("Vitrine ajoutée avec succès !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}
