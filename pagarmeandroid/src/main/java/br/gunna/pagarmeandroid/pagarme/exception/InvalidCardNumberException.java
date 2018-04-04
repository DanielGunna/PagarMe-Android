package br.gunna.pagarmeandroid.pagarme.exception;

/**
 * Created by Gunna on 04/04/2018.
 */

public class InvalidCardNumberException extends RuntimeException {

    private InvalidCardNumberException(String message) {
        super(message);
    }

    public static InvalidCardNumberException get() {
        return new InvalidCardNumberException("Invalid card number !!");
    }


}
