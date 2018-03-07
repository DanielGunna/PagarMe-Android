package br.gunna.pagarmewebhook.pagarme;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Daniel on 11/05/17.
 */

public interface PagarMeApi {
    @GET(Constants.PagarMe.CARD_HASH)
    Call<PagarMeResponse> getKeyHash(@Query("encryption_key") String key);
}
