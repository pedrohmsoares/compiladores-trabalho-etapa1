package lexico;

public class LexicalError extends Exception {
    private final int linha;

    public LexicalError(String mensagem, int linha) {
        super("Erro léxico na linha " + linha + ": " + mensagem);
        this.linha = linha;
    }

    public int getLinha() {
        return linha;
    }
}
