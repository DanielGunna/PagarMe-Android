package br.gunna.pagarmeandroid.pagarme.exception;

/**
 * Created by Gunna on 04/04/2018.
 */

public class InvalidKeyException extends RuntimeException{

    private InvalidKeyException(String message) {
        super(message);
    }

    public static InvalidKeyException get() {
        return new InvalidKeyException("You must provide a valid" +
                " non-empty PagarMe api key !! ");
    }

}
