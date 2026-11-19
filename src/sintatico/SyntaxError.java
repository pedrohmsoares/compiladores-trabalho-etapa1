package sintatico;

/* Erro sintático: guarda a mensagem e a linha em que o erro foi encontrado */
public class SyntaxError extends Exception {
    private final int linha;

    public SyntaxError(String mensagem, int linha) {
        super("Erro sintático na linha " + linha + ": " + mensagem);
        this.linha = linha;
    }

    public int getLinha() {
        return linha;
    }
}
