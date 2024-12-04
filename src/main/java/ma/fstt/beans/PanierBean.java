package ma.fstt.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.*;
import ma.fstt.persistance.Client;
import ma.fstt.persistance.Panier;
import ma.fstt.persistance.Produit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
@SessionScoped
public class PanierBean implements Serializable {

    private EntityManagerFactory emf;
    private EntityManager em;
    private Panier panier; // Panier en session
    private Long idProduit;

    public Long getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Long idProduit) {
        this.idProduit = idProduit;
    }

    public PanierBean() {
        emf = Persistence.createEntityManagerFactory("default");
        em = emf.createEntityManager();
        panier = new Panier(); // Créer un panier vide par défaut
    }

    public void ajouterProduit(Long idProduit) {
        if (idProduit == null) {
            throw new IllegalArgumentException("ID du produit manquant");
        }

        // Récupérer le client depuis la session
        Client client = (Client) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("client");

        if (client == null || client.getIdClient() == null) {
            throw new IllegalArgumentException("Client non connecté ou ID manquant");
        }

        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Récupérer le produit
            Produit produit = em.find(Produit.class, idProduit);
            if (produit == null) {
                throw new IllegalStateException("Produit introuvable");
            }

            // Récupérer ou créer un panier
            panier = em.createQuery(
                            "SELECT p FROM Panier p WHERE p.client = :client", Panier.class)
                    .setParameter("client", client)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (panier == null) {
                panier = new Panier();
                panier.setClient(client);
                panier.setDateCreation(new Date());
                em.persist(panier);
            }

            // Ajouter le produit au panier
            panier.ajouterProduit(produit);
            em.merge(panier);

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<Map.Entry<Produit, Integer>> getProduitsListe() {
        if (panier == null) {
            chargerPanier(); // Assurez-vous que le panier est bien chargé
        }
        if (panier.getProduits() == null || panier.getProduits().isEmpty()) {
            return new ArrayList<>(); // Retourne une liste vide si aucun produit
        }
        return new ArrayList<>(panier.getProduits().entrySet());
    }

    // Méthode pour calculer le total du panier
    public double calculerTotal() {
        if (panier == null || panier.getProduits() == null) {
            return 0.0;
        }
        return panier.getProduits().entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrix() * entry.getValue())
                .sum();
    }

    public void supprimerProduit(Long produitId) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            Produit produit = em.find(Produit.class, produitId);

            if (panier.getProduits().containsKey(produit)) {
                panier.getProduits().remove(produit);
                em.merge(panier);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void creerPanier() {
        if (panier == null) {
            panier = new Panier(); // Crée un panier vide si nécessaire
        }
    }

    public Panier getPanier() {
        return panier;
    }

    // Sauvegarder l'état du panier dans la session
    private void sauvegarderPanier() {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            // Si le panier existe déjà, on le met à jour
            if (panier.getId() != null) {
                em.merge(panier);
            } else {
                em.persist(panier); // Crée un nouveau panier dans la base de données
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // Méthode pour vider le panier
    public void viderPanier() {
        panier.getProduits().clear(); // Vide la liste des produits
        sauvegarderPanier(); // Sauvegarde l'état du panier (panier vide)
    }

    // Méthode pour vérifier si le panier est vide
    public boolean estPanierVide() {
        return panier.getProduits().isEmpty();
    }

    // Fermer l'EntityManager et EntityManagerFactory
    public void close() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public void chargerPanier() {
        Client client = (Client) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("client");

        if (client != null) {
            try {
                panier = em.createQuery("SELECT p FROM Panier p WHERE p.client.idClient = :idClient", Panier.class)
                        .setParameter("idClient", client.getIdClient())
                        .getSingleResult();
                System.out.println("Panier chargé avec succès : " + panier);
            } catch (NoResultException e) {
                System.out.println("Aucun panier trouvé pour le client avec ID : " + client.getIdClient());
                panier = new Panier();
                panier.setClient(client);
                panier.setDateCreation(new Date());
                EntityTransaction transaction = em.getTransaction();
                try {
                    transaction.begin();
                    em.persist(panier);
                    transaction.commit();
                } catch (Exception ex) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                    throw ex;
                }
                System.out.println("Nouveau panier créé pour le client : " + client.getNom());
            }
        } else {
            System.out.println("Aucun client connecté.");
        }
    }

    public List<Produit> listerProduits() {
        if (panier == null) {
            chargerPanier(); // Assurez-vous que le panier est chargé avant de l'utiliser
        }

        if (panier == null || panier.getId() == null) {
            throw new IllegalStateException("Le panier est toujours vide ou non chargé.");
        }

        // Retourner les produits de la Map 'produits'
        return new ArrayList<>(panier.getProduits().keySet());
    }

    public List<Panier> listerPaniers() {
        // Requête pour récupérer tous les paniers
        return em.createQuery("SELECT p FROM Panier p", Panier.class)
                .getResultList();
    }

    public void supprimerPanier(Long panierId) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Panier panier = em.find(Panier.class, panierId);
            if (panier != null) {
                em.remove(panier);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
