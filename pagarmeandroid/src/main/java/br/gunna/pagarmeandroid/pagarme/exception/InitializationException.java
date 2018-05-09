package br.gunna.pagarmeandroid.pagarme.exception;

/**
 * Created by Gunna on 04/04/2018.
 */

public class InitializationException extends RuntimeException {

    private InitializationException(String message) {
        super(message);
    }

    public static InitializationException get() {
        return new InitializationException("You must initialize calling" +
                " PagarMeAndroid.initialize(apiKey) !!!");
    }


}
