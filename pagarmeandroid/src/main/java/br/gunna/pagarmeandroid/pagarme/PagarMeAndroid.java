package br.gunna.pagarmeandroid.pagarme;

import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import br.gunna.pagarmeandroid.pagarme.exception.EmptyFieldException;
import br.gunna.pagarmeandroid.pagarme.exception.InitializationException;
import br.gunna.pagarmeandroid.pagarme.exception.InvalidCardNumberException;
import br.gunna.pagarmeandroid.pagarme.exception.InvalidKeyException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static br.gunna.pagarmeandroid.pagarme.CardUtils.isValidCardNumber;

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
        if (!isEmpty(key))
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
        if (!isEmpty(mKey)) {
            if (checkFieldsRequest(listener))
                generateKeyHash(listener);
        } else
            listener.onError(InvalidKeyException.get());
    }

    private boolean checkFieldsRequest(PagarMeListener listener) {
        if (isEmpty(mRequest.getNumber())) {
            listener.onError(getEmptyFieldException("Card number"));
            return false;
        }
        if (isEmpty(mRequest.getHolderName())) {
            listener.onError(getEmptyFieldException("Holder name"));
            return false;
        }
        if (isEmpty(mRequest.getExpirationDate())) {
            listener.onError(getEmptyFieldException("Expiration date"));
            return false;
        }
        if (isEmpty(mRequest.getCvv())) {
            listener.onError(getEmptyFieldException("CVV"));
            return false;
        }
        if (isEmpty(mRequest.getBrand())) {
            listener.onError(getEmptyFieldException("Brand"));
            return false;
        }

        if (!isValidCardNumber(mRequest.getNumber())) {
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
                            if (!isEmpty(cardHash))
                                listener.onSuccess(mRequest, response.body(), cardHash);
                            else
                                listener.onError(getCardHashError());
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

    private Exception getCardHashError() {
        return new RuntimeException("Unknown error generating card hash!!");
    }


    private void getPublicKey(Callback<PagarMeResponse> callbackApi) {
        mApiService.getKeyHash(mKey).enqueue(callbackApi);
    }

    private String buildCardHash(PagarMeResponse publicKeyResponse) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1PADDING");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            int cardHashId = (int) publicKeyResponse.getId();
            String rawPublicKey = publicKeyResponse.getPublicKey();

            //PAGARME PUBLIC KEYS COME IN PEM FORMAT, SO WE NEED TO CONVERT'EM
            //TO DER FORMAT SO THAT THEY CAN PLAY NICE WITH X509EncodedKeySpec.
            rawPublicKey = rawPublicKey
                    .replaceAll("\\s*-----BEGIN PUBLIC KEY-----\\s*", "")
                    .replaceAll("\\s*-----END PUBLIC KEY-----\\s*", "")
                    .replaceAll("\\s", "")
                    .replaceAll("[\n]", "")
                    .replaceAll("[\r]", "")
                    .replaceAll("[\t]", "")
                    .replaceAll("[ ]", "");

            byte[] decodedPublicKey = Base64.decode(rawPublicKey, 0);

            //SUCCESSFULLY CONVERTED THE PUBLIC KEY TO DER FORMAT.

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            //NOW FORMATTING CARD INFORMATION IN THE FORMAT EXPECTED BY PAGARME.

            String cardInformation = String.format(Locale.US,
                    "card_number=%s&card_holder_name=%s&card_expiration_date=%s"
                            + "&card_cvv=%s", mRequest.getNumber().replace(" ", "").trim(),
                    URLEncoder.encode(mRequest.getHolderName().trim(), "UTF-8").replace("+", "%20"),
                    mRequest.getExpirationDate().replace("/", "").trim(), mRequest.getCvv().trim());

            //THEN WE USE OUR CIPHER TO ENCRYPT AND THEN BASE64-ENCODE THE CARD INFORMATION,
            //AND FINALLY, APPEND THE ASSIGNED CARD ID AS PREFIX OF THE RESULT TO COMPOSE THE CARD HASH.

            byte[] encryptedCardInformation = cipher.doFinal(cardInformation.getBytes("UTF-8"));
            return String.format(Locale.US, "%d_%s", cardHashId,
                    (Base64.encodeToString(encryptedCardInformation, Base64.DEFAULT)))
                    .replaceAll("\\s", "");

            //NOW YOU MIGHT WANT TO SEND THE CARD HASH TO YOUR BACKEND SERVER.

        } catch (InvalidKeySpecException | java.security.InvalidKeyException invalidKey) {
            //PROBLEMS WITH THE PUBLIC KEY RECEIVED FROM PAGARME.
        } catch (BadPaddingException | IllegalBlockSizeException | IOException encryptionException) {
            //I/O ERRORS HAPPENED WHILE TRYING TO ENCRYPT CARD INFORMATION.
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return "";
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
