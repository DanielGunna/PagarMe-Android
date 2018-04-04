package br.gunna.pagarmeandroid.pagarme;

import android.text.TextUtils;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import br.gunna.pagarmeandroid.pagarme.exception.EmptyFieldException;
import br.gunna.pagarmeandroid.pagarme.exception.InitializationException;
import br.gunna.pagarmeandroid.pagarme.exception.InvalidCardNumberException;
import br.gunna.pagarmeandroid.pagarme.exception.InvalidKeyException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Gunna on 08/03/2018.
 */

public class PagarMeAndroid {
    private PagarMeRequest mRequest;
    private PagarMeApi mApiService;
    private static PagarMeAndroid sInstance;
    private String mKey;

    private PagarMeAndroid(String key) {
        mRequest = new PagarMeRequest();
        mApiService = PagarMeService.getServiceInstance();
        mKey = key;
    }


    public static void initialize(String key) {
        if (!TextUtils.isEmpty(key))
            sInstance = new PagarMeAndroid(key);
        else
            throw InvalidKeyException.get();
    }


    public static PagarMeAndroid getsInstance() {
        if (sInstance == null)
            throw InitializationException.get();
        return sInstance;
    }

    public PagarMeAndroid holderName(String value) {
        mRequest.setHolderName(value);
        return this;
    }

    public PagarMeAndroid number(String value) {
        mRequest.setNumber(value);
        mRequest.setBrand(CardUtils.getCreditCardBrand(value));
        return this;
    }


    public PagarMeAndroid expirationDate(String value) {
        mRequest.setExpirationDate(value);
        return this;
    }


    public PagarMeAndroid cvv(String value) {
        mRequest.setCvv(value);
        return this;
    }

    public PagarMeAndroid telephone(String value) {
        mRequest.setTelephone(value);
        return this;
    }


    public PagarMeAndroid ddd(String value) {
        mRequest.setDdd(value);
        return this;
    }


    public void generateCardHash(PagarMeListener listener) {
        if (!TextUtils.isEmpty(mKey)) {
            if (checkFieldsRequest(listener))
                generateKeyHash(listener);
        } else
            listener.onError(InvalidKeyException.get());
    }

    private boolean checkFieldsRequest(PagarMeListener listener) {
        if (TextUtils.isEmpty(mRequest.getNumber())) {
            listener.onError(getEmptyFieldException("Card number"));
            return false;
        }
        if (TextUtils.isEmpty(mRequest.getHolderName())) {
            listener.onError(getEmptyFieldException("Holder name"));
            return false;
        }
        if (TextUtils.isEmpty(mRequest.getExpirationDate())) {
            listener.onError(getEmptyFieldException("Expiration date"));
            return false;
        }
        if (TextUtils.isEmpty(mRequest.getCvv())) {
            listener.onError(getEmptyFieldException("CVV"));
            return false;
        }
        if (TextUtils.isEmpty(mRequest.getBrand())) {
            listener.onError(getEmptyFieldException("Brand"));
            return false;
        }

        if (CardUtils.checkCreditCard(mRequest.getNumber())) {
            listener.onError(InvalidCardNumberException.get());
            return false;
        }

        return true;
    }

    private Exception getEmptyFieldException(String field) {
        return EmptyFieldException.get(field);
    }

    private Exception getUnexpectedErrorException() {
        return new Exception(new Throwable("Unexpected error pagarme response =  null "));
    }


    private void generateKeyHash(final PagarMeListener listener) {
        getPublicKey(new Callback<PagarMeResponse>() {
            @Override
            public void onResponse(Call<PagarMeResponse> call, Response<PagarMeResponse> response) {
                if (response.code() == 200) {
                    try {
                        if (response.body() != null) {
                            final String cardHash = buildCardHash(response.body());
                            listener.onSuccess(mRequest, response.body(), cardHash);
                        } else {
                            listener.onError(getUnexpectedErrorException());
                        }
                    } catch (Exception e) {
                        listener.onError(e);
                    }
                } else {
                    listener.onError(new Exception(
                                    new Throwable(
                                            "PagarMeService:/Error generating card hash: "
                                                    + response.code() + " " + response.message())
                            )
                    );
                }
            }

            @Override
            public void onFailure(Call<PagarMeResponse> call, Throwable t) {
                listener.onError(new Exception(t));
            }
        });
    }


    private void getPublicKey(Callback<PagarMeResponse> callbackApi) {
        mApiService.getKeyHash(mKey).enqueue(callbackApi);
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
                mRequest.getNumber(), mRequest.getHolderName(), mRequest.getExpirationDate(),
                mRequest.getCvv(), mRequest.getBrand());
        final String encryptedData = encrypt(cardData, publicKey);
        return String.format("%s_%s", publicKeyId, encryptedData);
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

    public interface PagarMeListener {
        void onSuccess(PagarMeRequest cardRequest, PagarMeResponse response, String cardHash);

        void onError(Exception e);
    }
}
