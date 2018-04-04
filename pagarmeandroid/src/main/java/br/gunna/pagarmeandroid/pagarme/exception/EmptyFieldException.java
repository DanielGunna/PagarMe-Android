package br.gunna.pagarmeandroid.pagarme.exception;

/**
 * Created by Gunna on 04/04/2018.
 */

public class EmptyFieldException extends RuntimeException {
    private EmptyFieldException(String message) {
        super(message);
    }

    public static EmptyFieldException get(String field) {
        return new EmptyFieldException("Field " + field + "cant be empty!!");
    }
}
