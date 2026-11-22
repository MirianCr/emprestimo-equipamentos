package com.empresa.emprestimos.exception;

/**
 * Regra de negócio quebrada pelo estado atual (R1, R3, R4, R6, R9, R10).
 * Estende IllegalStateException, como pede a Parte 2; o TratadorDeErros devolve 409.
 */
public class ConflitoException extends IllegalStateException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
