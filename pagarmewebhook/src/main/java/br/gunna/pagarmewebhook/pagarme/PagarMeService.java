package br.gunna.pagarmewebhook.pagarme;


/**
 * Created by Daniel on 11/05/17.
 */


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import br.com.ioasys.cuidar_de_todos.androidapp.Constants;
import br.com.ioasys.cuidar_de_todos.androidapp.service.model.response.PagarMeResponse;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PagarMeService {
    private static final String META_DATA_KEY = "me.pagar.EncryptionKey";
    private static final String PAGAR_ME_API_KEY = "";
    private PagarMeApi service;
    private String holderName;
    private String number;
    private String expirationDate;
    private String cvv;
    private Gson gson;
    private OkHttpClient okHttpClient;
    private HttpLoggingInterceptor logInterceptor;
    private Interceptor responseInterceptor;
    private static  PagarMeService sInstance;
    private String telephone;
    private String ddd;

    public PagarMeService setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    private String brand;

    public String getTelephone() {
        return telephone;
    }

    public PagarMeService setTelephone(String telephone) {
        this.telephone = telephone;
        return  this;
    }

    public String getDdd() {
        return ddd;
    }

    public PagarMeService setDdd(String ddd) {
        this.ddd = ddd;
        return this;
    }

    public PagarMeService setHolderName(String holderName) {
        this.holderName = holderName;
        return  this;
    }

    public PagarMeService setNumber(String number) {
        this.number = number;
        return this;
    }

    public PagarMeService setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public PagarMeService setCvv(String cvv) {
        this.cvv = cvv;
        return this;
    }

    private PagarMeService(){
        initAppHostingService();
    }

    public static PagarMeService getInstance(){
        if(sInstance == null){
            sInstance = new PagarMeService();
        }
        return sInstance;
    }

    private void initAppHostingService() {
        if (service == null) {
            initInterceptors();
            initOkHttp();
            initGson();
            initRetrofitService();
        }
    }

    private void initGson() {
        gson = new GsonBuilder()
                .setLenient()
                .create();
    }

    private void initInterceptors() {
        logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        responseInterceptor = new Interceptor() {

            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response response = chain.proceed(chain.request());
                Log.w("Retrofit@Response", response.body().string());
                return response;
            }
        };
    }

    private void initOkHttp() {
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logInterceptor)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    private void initRetrofitService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.PagarMe.API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        service = retrofit.create(PagarMeApi.class);
    }

    public void generate(Context context, final Listener listener) {
        getPublicKey(context, new Callback<PagarMeResponse>() {
            @Override
            public void onResponse(Call<PagarMeResponse> call, Response<PagarMeResponse> response) {
                if (response.code() == 200) {
                    try {
                        final String cardHash = buildCardHash(response.body());
                        listener.onSucess(cardHash);
                    } catch (Exception e) {
                        listener.onError(e);
                    }
                }else{
                    listener.onError(new Exception(new Throwable("PagarMeService:/Error  card hash: " + response.code() + " " + response.message())));
                }
            }

            @Override
            public void onFailure(Call<PagarMeResponse> call, Throwable t) {
                listener.onError(new Exception(t));
            }
        });
    }


    private void getPublicKey(Context context, Callback<PagarMeResponse> callbackApi) {
        final String apiKey = PAGAR_ME_API_KEY;
        service.getKeyHash(apiKey).enqueue(callbackApi);
    }

    private String buildCardHash(PagarMeResponse publicKeyResponse)
            throws Exception {
        final long publicKeyId = publicKeyResponse.getId();
        final String publicKey = publicKeyResponse.getPublicKey();
        final String cardData = String.format("card_number=%s"
                        + "&card_holder_name=%s"
                        + "&card_expiration_date=%s"
                        + "&card_cvv=%s"
                        + "&brand=%s",
                number, holderName, expirationDate, cvv, telephone, ddd, brand);
        final String encryptedData = encrypt(cardData, publicKey);
        return  String.format("%s_%s",publicKeyId,encryptedData);
    }


    private String encrypt(String plain, String publicKey) throws Exception {
        final String pubKeyPEM = publicKey.replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("-----END PUBLIC KEY-----", "");
        final byte[] keyBytes = Base64.decode(pubKeyPEM.getBytes("UTF-8"), Base64.DEFAULT);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        final PublicKey key = keyFactory.generatePublic(spec);
        final Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final byte[] encryptedBytes = cipher.doFinal(plain.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public interface Listener {
        void onSucess(String cardHash);

        void onError(Exception e);
    }
}
