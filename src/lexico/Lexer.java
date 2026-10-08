package lexico;

import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1;                                             // Contador de linhas
    private static final char FIM = (char) -1;                              // Valor de ch no fim do arquivo
    private char ch = ' ';                                                  // Caractere lido do arquivo
    private FileReader file;                                                // Arquivo que vai ser compilado
    private LinkedHashMap<String, Word> words = new LinkedHashMap<String, Word>();  // Tabela de símbolos (mantém a ordem de inserção)

    /* Método construtor */
    public Lexer(String fileName) throws FileNotFoundException {
        // Testa se o arquivo existe e pode ser lido
        try {
            file = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
            throw e;
        }
        /*
            Carrega a tabela words com as palavras reservadas que devem estar previamente
            presentes dentro da tabela de símbolos
        */
        reserve(new Word("program", Tag.PROGRAM));
        reserve(new Word("begin", Tag.BEGIN));
        reserve(new Word("end", Tag.END));
        reserve(new Word("int", Tag.INT));
        reserve(new Word("float", Tag.FLOAT));
        reserve(new Word("char", Tag.CHAR));
        reserve(new Word("if", Tag.IF));
        reserve(new Word("then", Tag.THEN));
        reserve(new Word("else", Tag.ELSE));
        reserve(new Word("repeat", Tag.REPEAT));
        reserve(new Word("until", Tag.UNTIL));
        reserve(new Word("while", Tag.WHILE));
        reserve(new Word("do", Tag.DO));
        reserve(new Word("read", Tag.READ));
        reserve(new Word("write", Tag.WRITE));
    }

    /* Método para retornar a tabela de palavras reservadas (usado no main()) */
    public LinkedHashMap<String, Word> getWords() {
        return words;
    }

    /* 
        Os métodos abaixo são parte do que foi pedido na especificação do trabalho:
        **reserve()** que adiciona uma nova palavra reservada dentro da tabela de símbolos (words),

        **readch()** que lê o próximo caractere no arquivo,

        **readch(char c)** que lê o próximo caractere e retorna true ou false quando ele 
        identifica que o que foi lido é igual à char c e

        **scan()** que faz o scan() do caractere atual e retorna o Token associado a ele
        sendo o motor do analisador léxico
     */

    /* Método para inserir palavras reservadas na tabela de símbolos (words) */
    private void reserve(Word w) {
        words.put(w.getLexeme(), w); // lexema é a chave para entrada na HashTable
    }

    /* Lê o próximo caractere do arquivo */
    private void readch() throws IOException {
        ch = (char) file.read();
    }

    /* Lê o próximo caractere do arquivo e verifica se é igual a c */
    private boolean readch(char c) throws IOException {
        readch();
        if (ch != c) return false;
        ch = ' ';
        return true;
    }

    public Token scan() throws IOException, LexicalError {
        ignorarDelimitadoresEComentarios();

        // Quando o arquivo chega no final, lança o token EOF
        if (ch == FIM) return new Token(Tag.EOF);

        switch (ch) {
            // Operadores
            case '&':
                if (readch('&')) return Word.and;
                else throw new LexicalError("caractere '&' isolado (esperado '&&')", line);
            case '|':
                if (readch('|')) return Word.or;
                else throw new LexicalError("caractere '|' isolado (esperado '||')", line);
            case '=':
                if (readch('=')) return Word.eq;
                else return new Token('=');
            case '!':
                if (readch('=')) return Word.ne;
                else return new Token('!');
            case '<':
                if (readch('=')) return Word.le;
                else return new Token('<');
            case '>':
                if (readch('=')) return Word.ge;
                else return new Token('>');
            // Operadores e pontuação de um caractere
            case '+': case '-': case '*': case '/': case '%':
            case ';': case ',': case '.': case '(': case ')': {
                Token t = new Token(ch);
                ch = ' ';
                return t;
            }
        }

        if (ehDigito(ch)) return lerNumero();
        if (ehLetra(ch) || ch == '_') return lerIdentificador();
        if (ch == '"') return lerLiteral();
        if (ch == '\'') return lerCaractere();

        // Caracteres que não pertencem à linguagem
        throw new LexicalError("caractere inválido '" + ch + "'", line);
    }

    /* 
        Os métodos abaixo foram criados com o intuito de auxiliarem o método scan(),
        ao invés de colocar todas essas verificações dentro dele, eu decidi por criar
        métodos privados fora dele que fazem essa verificação e então scan() apenas os 
        chama, melhorando a visualização e entendimento do método.
    */

    /* Verifica se o char passado é um dígito --> Retorna true ou false */
    private boolean ehDigito(char c) {
        return c >= '0' && c <= '9';
    }

    /* Verifica se o char passado é uma letra --> Retorna true ou false */
    private boolean ehLetra(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /* 
    Pula espaços, quebras de linha (contando as linhas) e comentários
    --> 
    Lança um erro em caso de Input/Output ou um erro léxico, definido em Lexical Error
    */
    private void ignorarDelimitadoresEComentarios() throws IOException, LexicalError {
        for (;; readch()) {
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\b') continue;
            else if (ch == '\n') line++;    // Uma quebra de linha apenas adiciona mais uma linha lida
            else if (ch == '{') {
                int linhaInicio = line;
                if (!readch('*'))
                    // Como { não está na gramática especificada no trabalho, se não for seguido de *, lança um erro
                    throw new LexicalError("caractere inválido '{' (comentário começa com '{*')", line);
                char anterior = ' ';
                boolean fechou = false;
                readch();
                while (ch != FIM) {
                    if (ch == '\n') line++;
                    if (anterior == '*' && ch == '}') {
                        fechou = true;
                        break;
                    }
                    anterior = ch;
                    readch();
                }
                if (!fechou)
                    throw new LexicalError("comentário não fechado, iniciado na linha " + linhaInicio, linhaInicio);
            } else break;
        }
    }

    /*
    A função está lendo um dígito: 
    integer_const: digit+   |   float_const: digit+ "." digit+ 
    */
    private Token lerNumero() throws IOException, LexicalError {
        int value = 0;
        do {
            value = 10 * value + Character.digit(ch, 10);
            readch();
        } while (ehDigito(ch));
        if (ch != '.') return new Num(value);

        readch();
        if (!ehDigito(ch))
            throw new LexicalError("constante float mal formada: faltam dígitos depois do '.'", line);
        StringBuffer sb = new StringBuffer();
        sb.append(value).append('.');
        do {
            sb.append(ch);
            readch();
        } while (ehDigito(ch));
        return new Real(Float.parseFloat(sb.toString()));
    }

    /* 
    A função está lendo um identificador:
    identifier: (letter | "_") (letter | digit | "_")* 
    */
    private Token lerIdentificador() throws IOException {
        StringBuffer sb = new StringBuffer();
        do {
            sb.append(ch);
            readch();
        } while (ehLetra(ch) || ehDigito(ch) || ch == '_');
        String s = sb.toString();
        Word w = words.get(s);
        if (w != null) return w; // palavra reservada ou identificador já existente
        w = new Word(s, Tag.ID);
        words.put(s, w);
        return w;
    }

    /* 
    A função está lendo um Literal:
    literal: '"' caractere* '"' 
    */
    private Token lerLiteral() throws IOException, LexicalError {
        StringBuffer sb = new StringBuffer();
        sb.append('"');
        readch();
        while (ch != '"') {
            if (ch == '\n' || ch == FIM)
                throw new LexicalError("literal não fechado", line);
            sb.append(ch);
            readch();
        }
        sb.append('"');
        ch = ' ';
        return new Word(sb.toString(), Tag.LITERAL);
    }

    /* 
    A função está lendo um caractere:
    char_const: "'" carac "'" 
    */
    private Token lerCaractere() throws IOException, LexicalError {
        readch();
        if (ch == '\'' || ch == '\n' || ch == FIM)
            throw new LexicalError("constante char mal formada", line);
        char c = ch;
        readch();
        if (ch != '\'')
            throw new LexicalError("constante char não fechada", line);
        ch = ' ';
        return new Word("'" + c + "'", Tag.CHAR_CONST);
    }
    
}
