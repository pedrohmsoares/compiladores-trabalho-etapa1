package sintatico;

import lexico.*;
import java.io.IOException;

/*
    Analisador sintático descendente recursivo (preditivo).
    Cada símbolo não terminal da gramática tem um método com o mesmo nome.
    O token atual (lookahead) fica em "tok" e é usado para escolher a produção.
    Repetições { } da gramática viram "while" e partes opcionais [ ] viram "if".
    Ao encontrar o primeiro erro, lança SyntaxError e a análise termina.

    Os pontos marcados com "MUDANÇA NA GRAMÁTICA" são os lugares em que a
    gramática do enunciado foi alterada para ficar LL(1).
*/
public class Parser {
    private Lexer lexer;
    private Token tok;

    public Parser(Lexer lexer) throws IOException, LexicalError {
        this.lexer = lexer;
        avancar(); // lê o primeiro token
    }

    /* Funções auxiliares */

    /* Lê o próximo token do analisador léxico */
    private void avancar() throws IOException, LexicalError {
        tok = lexer.scan();
    }

    /* Se o token atual é o esperado, avança; senão é erro sintático */
    private void consumir(int tag) throws IOException, LexicalError, SyntaxError {
        if (tok.tag == tag) avancar();
        else erro(Tag.nome(tag));
    }

    /* Lança o erro sintático dizendo o que era esperado e o que foi encontrado */
    private void erro(String esperado) throws SyntaxError {
        throw new SyntaxError("esperado " + esperado + ", encontrado " + textoDoToken(tok), Lexer.line);
    }

    /* Texto do token para a mensagem de erro */
    private String textoDoToken(Token t) {
        if (t.tag == Tag.EOF) return "fim de arquivo";
        if (t.tag < 256) return "'" + (char) t.tag + "'";
        return "'" + t + "'";
    }

    private boolean ehTipo() {
        return tok.tag == Tag.INT || tok.tag == Tag.FLOAT || tok.tag == Tag.CHAR;
    }

    /* relop ::= "==" | ">" | ">=" | "<" | "<=" | "!=" */
    private boolean ehRelop() {
        return tok.tag == Tag.EQ || tok.tag == '>' || tok.tag == Tag.GE
            || tok.tag == '<' || tok.tag == Tag.LE || tok.tag == Tag.NE;
    }

    /* addop ::= "+" | "-" | "||" */
    private boolean ehAddop() {
        return tok.tag == '+' || tok.tag == '-' || tok.tag == Tag.OR;
    }

    /* mulop ::= "*" | "/" | "%" | "&&" */
    private boolean ehMulop() {
        return tok.tag == '*' || tok.tag == '/' || tok.tag == '%' || tok.tag == Tag.AND;
    }

    /* Programa e declarações */

    /*
        program ::= program identifier begin corpo end "."
        Depois do "." deve vir o fim de arquivo.
    */
    public void programa() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.PROGRAM);
        consumir(Tag.ID);
        consumir(Tag.BEGIN);
        corpo();
        consumir(Tag.END);
        consumir('.');
        consumir(Tag.EOF);
    }

    /*
        MUDANÇA NA GRAMÁTICA: o trecho "[decl-list] stmt-list" virou "corpo".
        Uma declaração (a = int) e uma atribuição (a = 1) começam igual
        (identifier "="), então a decisão foi adiada até ler o token depois do "=".

        corpo ::= identifier corpoId
                | comando restoListaComandos     (comando que não começa com identifier)
    */
    private void corpo() throws IOException, LexicalError, SyntaxError {
        if (tok.tag == Tag.ID) {
            consumir(Tag.ID);
            corpoId();
        } else {
            comando();
            restoListaComandos();
        }
    }

    /*
        corpoId ::= "," listaIdentificadores "=" tipo ";" corpo    (é uma declaração)
                  | "=" corpoIgual
    */
    private void corpoId() throws IOException, LexicalError, SyntaxError {
        if (tok.tag == ',') {
            consumir(',');
            listaIdentificadores();
            consumir('=');
            tipo();
            consumir(';');
            corpo();
        } else {
            consumir('=');
            corpoIgual();
        }
    }

    /*
        corpoIgual ::= tipo ";" corpo                            (é uma declaração)
                     | expressaoSimples restoListaComandos       (é uma atribuição)
    */
    private void corpoIgual() throws IOException, LexicalError, SyntaxError {
        if (ehTipo()) {
            tipo();
            consumir(';');
            corpo();
        } else {
            expressaoSimples();
            restoListaComandos();
        }
    }

    /* ident-list ::= identifier {"," identifier} */
    private void listaIdentificadores() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.ID);
        while (tok.tag == ',') {
            consumir(',');
            consumir(Tag.ID);
        }
    }

    /* type ::= int | float | char */
    private void tipo() throws IOException, LexicalError, SyntaxError {
        if (ehTipo()) avancar();
        else erro("tipo (int, float ou char)");
    }

    /* Comandos */

    /* stmt-list ::= stmt {";" stmt} */
    private void listaComandos() throws IOException, LexicalError, SyntaxError {
        comando();
        restoListaComandos();
    }

    /* Parte {";" stmt} da stmt-list. O ";" separa comandos, então depois dele sempre vem um comando */
    private void restoListaComandos() throws IOException, LexicalError, SyntaxError {
        while (tok.tag == ';') {
            consumir(';');
            comando();
        }
    }

    /* stmt ::= assign-stmt | if-stmt | while-stmt | repeat-stmt | read-stmt | write-stmt */
    private void comando() throws IOException, LexicalError, SyntaxError {
        switch (tok.tag) {
            case Tag.ID:     atribuicao(); break;
            case Tag.IF:     comandoIf(); break;
            case Tag.WHILE:  comandoWhile(); break;
            case Tag.REPEAT: comandoRepeat(); break;
            case Tag.READ:   comandoRead(); break;
            case Tag.WRITE:  comandoWrite(); break;
            default:         erro("comando");
        }
    }

    /* assign-stmt ::= identifier "=" simple-expr */
    private void atribuicao() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.ID);
        consumir('=');
        expressaoSimples();
    }

    /*
        MUDANÇA NA GRAMÁTICA: as duas produções do if começavam iguais, então foram fatoradas.
        if-stmt ::= if condition then stmt-list [else stmt-list] end
    */
    private void comandoIf() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.IF);
        condicao();
        consumir(Tag.THEN);
        listaComandos();
        if (tok.tag == Tag.ELSE) {
            consumir(Tag.ELSE);
            listaComandos();
        }
        consumir(Tag.END);
    }

    /* while-stmt ::= stmt-prefix stmt-list end */
    private void comandoWhile() throws IOException, LexicalError, SyntaxError {
        prefixoWhile();
        listaComandos();
        consumir(Tag.END);
    }

    /* stmt-prefix ::= while condition do */
    private void prefixoWhile() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.WHILE);
        condicao();
        consumir(Tag.DO);
    }

    /* repeat-stmt ::= repeat stmt-list stmt-suffix */
    private void comandoRepeat() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.REPEAT);
        listaComandos();
        sufixoRepeat();
    }

    /* stmt-suffix ::= until condition */
    private void sufixoRepeat() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.UNTIL);
        condicao();
    }

    /* read-stmt ::= read "(" identifier ")" */
    private void comandoRead() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.READ);
        consumir('(');
        consumir(Tag.ID);
        consumir(')');
    }

    /* write-stmt ::= write "(" writable ")" */
    private void comandoWrite() throws IOException, LexicalError, SyntaxError {
        consumir(Tag.WRITE);
        consumir('(');
        conteudoWrite();
        consumir(')');
    }

    /* writable ::= simple-expr | literal */
    private void conteudoWrite() throws IOException, LexicalError, SyntaxError {
        if (tok.tag == Tag.LITERAL) consumir(Tag.LITERAL);
        else expressaoSimples();
    }

    /* Expressões */

    /* condition ::= expression */
    private void condicao() throws IOException, LexicalError, SyntaxError {
        expressao();
    }

    /*
        MUDANÇA NA GRAMÁTICA: as duas produções começavam com simple-expr, então foram fatoradas.
        expression ::= simple-expr [relop simple-expr]
    */
    private void expressao() throws IOException, LexicalError, SyntaxError {
        expressaoSimples();
        if (ehRelop()) {
            avancar();
            expressaoSimples();
        }
    }

    /*
        MUDANÇA NA GRAMÁTICA: retirada a recursão à esquerda.
        simple-expr ::= term {addop term}
    */
    private void expressaoSimples() throws IOException, LexicalError, SyntaxError {
        termo();
        while (ehAddop()) {
            avancar();
            termo();
        }
    }

    /*
        MUDANÇA NA GRAMÁTICA: retirada a recursão à esquerda.
        term ::= factor-a {mulop factor-a}
    */
    private void termo() throws IOException, LexicalError, SyntaxError {
        fatorA();
        while (ehMulop()) {
            avancar();
            fatorA();
        }
    }

    /* factor-a ::= factor | "!" factor | "-" factor */
    private void fatorA() throws IOException, LexicalError, SyntaxError {
        if (tok.tag == '!' || tok.tag == '-') avancar();
        fator();
    }

    /* factor ::= identifier | constant | "(" expression ")" */
    private void fator() throws IOException, LexicalError, SyntaxError {
        switch (tok.tag) {
            case Tag.ID:
                consumir(Tag.ID);
                break;
            case Tag.INT_CONST:
            case Tag.FLOAT_CONST:
            case Tag.CHAR_CONST:
                constante();
                break;
            case '(':
                consumir('(');
                expressao();
                consumir(')');
                break;
            default:
                erro("identificador, constante ou '('");
        }
    }

    /* constant ::= integer_const | float_const | char_const */
    private void constante() throws IOException, LexicalError, SyntaxError {
        if (tok.tag == Tag.INT_CONST || tok.tag == Tag.FLOAT_CONST || tok.tag == Tag.CHAR_CONST) avancar();
        else erro("constante");
    }
}
