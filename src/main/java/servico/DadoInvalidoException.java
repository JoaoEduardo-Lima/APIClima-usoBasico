package servico;

public class DadoInvalidoException extends Exception {
    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}