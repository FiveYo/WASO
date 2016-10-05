package fr.insalyon.waso.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.JsonHttpClient;
import fr.insalyon.waso.util.exception.ServiceException;
import java.io.IOException;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author WASO Team
 */
public class AjaxAction {

    protected String smaUrl;
    protected JsonObject container;

    protected JsonHttpClient jsonHttpClient;

    public AjaxAction(String smaUrl, JsonObject container) {
        this.smaUrl = smaUrl;
        this.container = container;

        this.jsonHttpClient = new JsonHttpClient();
    }

    public void release() {
        try {
            this.jsonHttpClient.close();
        } catch (IOException ex) {
            // Ignorer
        }
    }

    protected static JsonObject transformClient(JsonObject client) {

        
        // Transformation de l'entité Client pour l'affichage (id,denomination,adresse,ville)
        
        JsonObject jsonItem = new JsonObject();

        jsonItem.addProperty("id", client.get("id").getAsString());
        jsonItem.addProperty("denomination", client.get("denomination").getAsString());

        
        // Pour afficher le code postal avant le nom de la ville
        
        String ville = client.get("ville").getAsString();
        int indexCodePostal = ville.lastIndexOf(" ");
        if (indexCodePostal > 0) {
            ville = ville.substring(indexCodePostal + 1) + " " + ville.substring(0, indexCodePostal);

        }

        jsonItem.addProperty("adresse", client.get("adresse").getAsString());
        jsonItem.addProperty("ville", ville);

        
        // Simplification des entités Personnes (id,nom,prenom)
        
        JsonArray persons = new JsonArray();

        for (JsonElement p : client.get("personnes").getAsJsonArray()) {

            JsonObject person = p.getAsJsonObject();

            JsonObject jsonSubItem = new JsonObject();
            jsonSubItem.add("id", person.get("id"));
            jsonSubItem.add("nom", person.get("nom"));
            jsonSubItem.add("prenom", person.get("prenom"));

            persons.add(jsonSubItem);
        }

        jsonItem.add("personnes", persons);

        return jsonItem;
    }

    protected static JsonArray transformListeClient(JsonArray liste) {

        JsonArray jsonListe = new JsonArray();

        for (JsonElement i : liste) {

            jsonListe.add(transformClient(i.getAsJsonObject()));
        }

        return jsonListe;
    }

    public void getListeClient() throws ServiceException {
        try {
            JsonElement smaResultContainerElement = this.jsonHttpClient.post(
                    this.smaUrl,
                    new BasicNameValuePair("SMA", "getListeClient")
                );

            if (smaResultContainerElement == null) {
                throw new ServiceException("Appel impossible au SMA getListeClient [" + this.smaUrl + "]");
            }

            JsonObject smaResultContainerObject = smaResultContainerElement.getAsJsonObject();

            JsonArray jsonListe = transformListeClient(smaResultContainerObject.getAsJsonArray("clients"));

            this.container.add("clients", jsonListe);

        } catch (IOException ex) {
            throw new ServiceException("Exception in Ajax getListeClient", ex);
        }
    }

    public void rechercherClientParNumero(Integer numero) throws ServiceException {
        
        try {
            JsonElement smaResultContainerElement = this.jsonHttpClient.post(
                    this.smaUrl,
                    new BasicNameValuePair("SMA", "getClientParNumero"),
                    new BasicNameValuePair("idClient", Integer.toString(numero))
                );
            

            if (smaResultContainerElement == null) {
                throw new ServiceException("Appel impossible au SMA getClientParNumero [" + this.smaUrl + "]");
            }

            JsonObject smaResultContainerObject = smaResultContainerElement.getAsJsonObject();

            JsonArray jsonListe = transformListeClient(smaResultContainerObject.getAsJsonArray("clients"));

            this.container.add("clients", jsonListe);

        } catch (IOException ex) {
            throw new ServiceException("Exception in Ajax rechercherClientParNumero", ex);
        }
    }

    void rechercherClientParDenomination(String denomination, String ville) throws ServiceException {

        try {
            JsonElement smaResultContainerElement = this.jsonHttpClient.post(
                    this.smaUrl,
                    new BasicNameValuePair("SMA", "rechercherClientParDenomination"),
                    new BasicNameValuePair("DENOMINATION",denomination),
                    new BasicNameValuePair("VILLE",ville)
                );
            

            if (smaResultContainerElement == null) {
                throw new ServiceException("Appel impossible au SMA rechercherClientParDenomination [" + this.smaUrl + "]");
            }

            JsonObject smaResultContainerObject = smaResultContainerElement.getAsJsonObject();

            JsonArray jsonListe = transformListeClient(smaResultContainerObject.getAsJsonArray("clients"));

            this.container.add("clients", jsonListe);

        } catch (IOException ex) {
            throw new ServiceException("Exception in Ajax rechercherClientParDenomination", ex);
        }
        
    }

    void rechercherClientParNomPersonne(String nomPersonne, String ville) throws ServiceException {
        
        // ...
    }

}
