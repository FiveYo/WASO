package fr.insalyon.waso.som.personne;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;
import java.util.List;

/**
 *
 * @author WASO Team
 */
public class ServiceObjetMetier {

    protected DBConnection dBConnection;
    protected JsonObject container;

    public ServiceObjetMetier(DBConnection dBConnection, JsonObject container) {
        this.dBConnection = dBConnection;
        this.container = container;
    }

    public void getListePersonne() throws ServiceException {
        try {
            List<Object[]> listePersonne = this.dBConnection.launchQuery("SELECT PersonneID, Nom, Prenom, Mail FROM PERSONNE ORDER BY PersonneID");

            JsonArray jsonListe = new JsonArray();

            for (Object[] row : listePersonne) {
                JsonObject jsonItem = new JsonObject();

                jsonItem.addProperty("id", (Integer) row[0]);
                jsonItem.addProperty("nom", (String) row[1]);
                jsonItem.addProperty("prenom", (String) row[2]);
                jsonItem.addProperty("mail", (String) row[3]);

                jsonListe.add(jsonItem);
            }

            this.container.add("personnes", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM getListePersonne", ex);
        }
    }
    
    public void getPersonneById(String idPersonne) throws ServiceException {
        try {
            List<Object[]> listePersonne = this.dBConnection.launchQuery("SELECT PersonneID, Civilite, Nom, Prenom, Mail FROM PERSONNE WHERE PERSONNEID = ?",idPersonne);

            JsonArray jsonListe = new JsonArray();

            for (Object[] row : listePersonne) {
                JsonObject jsonItem = new JsonObject();

                jsonItem.addProperty("id", (Integer) row[0]);
                 jsonItem.addProperty("civilite", (String) row[1]);
                jsonItem.addProperty("nom", (String) row[2]);
                jsonItem.addProperty("prenom", (String) row[3]);
                jsonItem.addProperty("mail", (String) row[4]);

                jsonListe.add(jsonItem);
            }

            this.container.add("personnes", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM getListePersonne", ex);
        }
    }
    

}
