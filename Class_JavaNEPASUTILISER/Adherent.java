import java.util.ArrayList;

public class Adherent extends Client{
	
	private String numeroTel;
	private String codeSecret;
	private String nom;
	private String prenom;
	private String adresse;
	private String numeroCB;
	private ArrayList<Location> listeLocation;
	
	public String getNumeroTel() {
		return numeroTel;
	}
	public String getCodeSecret() {
		return codeSecret;
	}
	public String getAdresse() {
		return adresse;
	}
	public String getNumeroCB() {
		return numeroCB;
	}
	public String getNom() {
		return nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public ArrayList<Location> getListeLocation() {
		return listeLocation;
	}
	public Adherent(String numeroTel, String codeSecret, String nom, String prenom, String adresse, String numeroCB) {
		super();
		this.numeroTel = numeroTel;
		this.codeSecret = codeSecret;
		this.nom = nom;
		this.prenom = prenom;
		this.adresse = adresse;
		this.numeroCB = numeroCB;
	}
	public void setListeLocation(ArrayList<Location> listeLocation) {
		this.listeLocation = listeLocation;
	}
	@Override
	public String toString() {
		return "Adherent [numeroTel=" + numeroTel + ", codeSecret=" + codeSecret + ", nom=" + nom + ", prenom=" + prenom
				+ ", adresse=" + adresse + ", numeroCB=" + numeroCB + ", listeLocation=" + listeLocation + "]";
	}
	
	
}