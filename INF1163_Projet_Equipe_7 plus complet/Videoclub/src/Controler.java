import java.util.*;

public class Controler {
	private Hashtable<String,Adherent> listeMembre=null;
	private Hashtable <Integer,Employe> listeEmploye=null;
	private Catalogue catalogue = null;
	private Inventaire inventaire = null;
	private Location loc = null;
	private Vente vente = null;
	private Hashtable<Integer,Location> listeLocation = null;
	private Employe caissier=null;

	//Constructeur
	public Controler(Hashtable<String,Adherent> list,Hashtable<String,DescriptionArticle> listDesc,
			Hashtable<Integer,Employe> listEmploye, Hashtable<String,Article> listArt){
		this.listeMembre=list;
		this.catalogue = new Catalogue(listDesc);
		this.listeEmploye=listEmploye;
		this.inventaire = new Inventaire(listArt);
		inventaire.reconcile(catalogue);
		
	}
	public Hashtable<String,Adherent> getListAdherent(){
		return this.listeMembre;
	}
	public Hashtable<Integer,Location> getListeLocation() {
		return this.listeLocation;
	}
	public Employe getCaissier(){
		return this.caissier;
	}
	public Catalogue getCatalogue(){
		return this.catalogue;
	}

	/*Methodes pour l'authentification */
	public Adherent authentificationMembre(String pseudo,String mdp) {
		try{
			String num=this.listeMembre.get(pseudo).getNumeroTel();
			String test=this.listeMembre.get(pseudo).getCodeSecret();
			if(num.equals(pseudo) && test.equals(mdp)){
				loc.ajouterAdherent(this.listeMembre.get(pseudo));
				return this.listeMembre.get(pseudo);
			}
		}
		catch (NullPointerException e){
			return null;
		}
		return null;
	}
	public Employe authentificationEmploye(String idEmploye,String mdp) {
		try{
			int id=this.listeEmploye.get(Integer.parseInt(idEmploye)).getIdEmploye();
			String password=this.listeEmploye.get(Integer.parseInt(idEmploye)).getMdp();
			if(Integer.parseInt(idEmploye)==id && password.equals(mdp)){
				this.caissier=this.listeEmploye.get(id);
				return this.caissier;
			}
		}
		catch (NullPointerException e){
			return null;
		}
		catch (NumberFormatException e){
			
		}
		return null;
	}

	public Location getLocation(){
		return this.loc;
	}
	
	//Création d'une location avec la date d'aujourd'hui
	public void creerLocation() {
		Calendar aujourdhui = Calendar.getInstance();
		this.loc = new Location(aujourdhui);
	}
	
	//On saisit le code barre du film et sa durée 
	public boolean saisirArticleLoc (String codeBarre,int duree) {
		if(this.loc.isTerminee() == false) {
			//On récupère l'article grace à son code barre
			Article art = inventaire.getArticle(codeBarre);
			if(art != null) {
				//On cherche le code de sa description
				String codeArticle = art.getCodeDescription();
	
				/*On va chercher cette description dans le catalogue
				 * et on l'ajoute à l'article
				 */
				DescriptionArticle desc = catalogue.getDesc(codeArticle);
				art.setDescription(desc);
	
				if(desc != null) {
					if(desc.getPrixJournalier() > 0) { //Ce n'est pas des confiseries
						loc.creerLigneArticles(art);
						loc.setDateDue(duree);
						loc.majMontant();
						return true;
					}
					else {
						return false;
					}
				}
			}
		}
		return false;
	}

	public void terminerLocation () {
		loc.setEstTerminee(true);
		float montantFinal = loc.getMontant();
		//On crée un paiement 
		Paiement p = new Paiement(montantFinal);
		loc.setPaiement(p);
		//On ajoute la location à la base de données
		Videoclub v = Videoclub.instanceVideoclub();
		Database D = v.getDB();
		int id = D.insertLocation(loc);
		loc.setIdLoc(id);
		System.out.println(loc.getListeLigneArticles());
		
		//Puis on l'ajoute à la liste des locations du contrôleur
		if(this.listeLocation == null) {
			this.listeLocation = new Hashtable<Integer,Location>();
		}
		this.listeLocation.put(id,loc);
		this.loc = null;
		
		

	}

	/* Methodes pour l'acquisition d'un film */
	/**
	 * Pour l'ajout d'un film dédié en la location à l'inventaire
	 * @param codeArticle
	 * @param titre
	 * @param genre
	 * @param description : texte comprenant une brève description du film
	 * @param prixJournalier
	 * @param prixHebdomadaire
	 * @param prixVente : La valeur dédié à la revente du film ou si perdu
	 * @param estNouveau
	 * @param quantite : Le nombre d'articles identiques ajoutés
	 */
	public void acquerirFilm(String codeArticle, String titre, String genre, String description, float prixJournalier, 
			float prixHebdomadaire, float prixVente, boolean estNouveau, int quantite){

		DescriptionArticle desc;
		desc = new DescriptionArticle();
		desc.setCodeArticle(codeArticle);
		desc.setTitre(titre);
		desc.setGenre(genre);
		desc.setDescription(description);
		desc.setPrixJournalier(prixJournalier);
		desc.setPrixHebdomadaire(prixHebdomadaire);
		desc.setPrixVente(prixVente);
		desc.setEstNouveau(estNouveau);

		Acquisition acq = new Acquisition();

		acq.ajouterArticle(desc, quantite);
		if (catalogue.getDesc(codeArticle) == null){
			acq.ajouterDescriptionArticle(desc);
			catalogue.ajouterDesc(desc); 
		}

		//Ajoute temporairement l'article au catalogue au lieu de le réinitialiser au complet après la mise à jour de la db
		acq.updatedb();
	}
	/**
	 * Pour l'ajout de tout article autre qu'un film en location
	 * @param codeArticle
	 * @param description
	 * @param prixVente
	 * @param quantite : Le nombre d'articles identiques ajoutés
	 */
	public void acquerirAutre(String codeArticle,String description, float prixVente, int quantite){

		DescriptionArticle desc;
		desc = new DescriptionArticle();
		desc.setCodeArticle(codeArticle);
		desc.setDescription(description);
		desc.setTitre(description);
		desc.setPrixVente(prixVente);

		Acquisition acq = new Acquisition();
		/*Ajoutera le nombre d'articles indiqués mais ajoutera seulement une nouvelle description si elle n'est
		 * pas déjà présente dans le catalogue
		 */
		acq.ajouterArticle(desc, quantite);
		
		if (catalogue.getDesc(codeArticle) == null){
			acq.ajouterDescriptionArticle(desc);
			catalogue.ajouterDesc(desc); 
		}

		//Ajoute temporairement l'article au catalogue au lieu de le réinitialiser au complet après la mise à jour de la db
		acq.updatedb();
	}

	/* Methode pour l'inscription */
	public void Inscription(String nom, String prenom, String num, String cb, String adresse, String mdp) {
		Inscription ins = new Inscription(nom,prenom,num,cb,adresse,mdp);
		Adherent ad = ins.ajouterAdherent();
		this.listeMembre.put(num, ad);
	}


	/*Methode pour charger les locations depuis la base de données */
	public void addListeLocation(ArrayList<Location> l) {
		this.listeLocation = new Hashtable<Integer,Location>();
		for(int i=0;i < l.size(); i++) {
			Adherent ad = listeMembre.get(l.get(i).getNumAdherent());
			Article a = inventaire.getArticle(l.get(i).getCodeBarre());
			//Condition si l'article a ete enlever des articles
			if(a != null) {
				DescriptionArticle desc = catalogue.getDesc(a.getCodeDescription());
				a.setDescription(desc);
				ArrayList<LigneArticle> la = new ArrayList<LigneArticle>();
				la.add(new LigneArticle(a,l.get(i).getDateDue(),l.get(i).getDateRetour()));

				//Pour ajouter tous les articles dans la ligne Article de la location par rapport à son id
				for(int j = i+1; j < l.size(); j++) {
					if(l.get(i).getIdLoc() == l.get(j).getIdLoc()) {
						Article a2 = inventaire.getArticle(l.get(j).getCodeBarre());
						DescriptionArticle desc2 = catalogue.getDesc(a2.getCodeDescription());
						a2.setDescription(desc2);
						la.add(new LigneArticle(a2,l.get(j).getDateDue(),l.get(j).getDateRetour()));
					}
				}
				if(listeLocation.containsKey(l.get(i).getIdLoc()) ==  false) {
					Location tmp = new Location(l.get(i).getIdLoc(),ad,l.get(i).getDateHeure(),la,l.get(i).montant);
					listeLocation.put(tmp.getIdLoc(),tmp);	
				}
			}

		}
	}
	
	//Effectuer un retour
	public void effectuerUnRetour(String codeBarre) {
		Retour r;
		Videoclub v = Videoclub.instanceVideoclub();
		
		//On cherche dans les locations celle qui possède l'article avec le code barre entrée en paramètres
		Set<Integer> keys = this.listeLocation.keySet();
		for(Integer k: keys) {
			int taille = this.listeLocation.get(k).getListeLigneArticles().size();
			for(int j=0; j < taille; j++) {
				LigneArticle la = this.listeLocation.get(k).getListeLigneArticles().get(j);
				//On trouve cet article
				if(la.getCodeBarreArticle().equals(codeBarre)) {
					//On le retourne
					r = new Retour(this.listeLocation.get(k).getListeLigneArticles().get(j));
					la = r.getLigneArticles();
					
					//On le retourne aussi dans la base de données
					v.getDB().retour(this.listeLocation.get(k).getIdLoc(), la);
				}
			}
		}
	}
	//On crée une vente
	public void initierVente(){
		if (vente==null){
			this.vente = new Vente();
		}
	}
	//On termine la vente
	public void terminerVente(){
		this.vente = null;
	}
	public Vente instanceVente(){
		return this.vente;
	}
	//On creer un ligne de vente
	//Equivalent de saisirArticleLoc mais pour la vente
	public void creerligneVente(String codeBarre, int qte){
		if (vente==null){
			this.vente = new Vente();
		}
	this.vente.ajouterLigneArticles(inventaire.getArticle(codeBarre), qte);
	}
	
	public Inventaire instanceInventaire(){
		return inventaire;
	}
	
	/*
	 * Gestion des retards
	 * On gère les amendes
	 */
	
	public void gererRetard() {

		//On parcourt toutes les locations
		Set<Integer> keys = this.listeLocation.keySet();
		for(Integer k: keys) {
			int taille = this.listeLocation.get(k).getListeLigneArticles().size();
			
			//On analyse les lignes d'articles de chaque location pour voir si elles contiennent des retards
			for(int j=0; j < taille; j++) {
				LigneArticle la = this.listeLocation.get(k).getListeLigneArticles().get(j);
				Date dateDue = la.getDateDue();
				Date aujourdhui = new Date();
				Date dateRetour = la.getDateRetour();

				//
				if(dateDue.before(aujourdhui) && (dateRetour == null || dateDue.before(dateRetour))){
					//Il y a un retard, on calcule le nombre de jour
					long diff;
					if(dateRetour != null) {
						diff = Math.abs(dateRetour.getTime() - dateDue.getTime());
					}
					else {
						diff = Math.abs(aujourdhui.getTime()-dateDue.getTime());
					}
					long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
					long jRet = (long)diff/MILLISECONDS_PER_DAY;
					
					if(jRet > 0) {
						//On crée une nouvelle amende
						Amende am = new Amende(this.listeLocation.get(k),j);
						//On ajoute l'amende à la location
						listeLocation.get(k).ajouterAmende(am);
					}
					
				}
			}
		}
	}
	//Utiliser pour payer une amende
	public void finAmende(int idLoc, String numAd, String codeBarre) {
		Location loc = this.listeLocation.get(idLoc);
		Adherent ad = this.listeMembre.get(numAd);
		
		ad.payerAmende(loc, codeBarre);
	}
}

	
