package br.gunna.pagarmewebhook.pagarme;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;



/**
 * Created by Daniel on 12/05/17.
 */

public class PagarMeResponse {

    @Expose
    @SerializedName("id")
    private long id;
    @Expose
    @SerializedName("date_created")
    private String date;
    @Expose
    @SerializedName("ip")
    private String ipAddress;
    @Expose
    @SerializedName("public_key")
    private String publicKey;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
