package main;

import lexico.*;
import java.io.*;
import java.util.*;

public class Main {

    /* Nome legível da tag, usado só para imprimir */
    private static String nomeDaTag(int tag) {
        switch (tag) {
            case Tag.PROGRAM:     return "PROGRAM";
            case Tag.BEGIN:       return "BEGIN";
            case Tag.END:         return "END";
            case Tag.INT:         return "INT";
            case Tag.FLOAT:       return "FLOAT";
            case Tag.CHAR:        return "CHAR";
            case Tag.IF:          return "IF";
            case Tag.THEN:        return "THEN";
            case Tag.ELSE:        return "ELSE";
            case Tag.REPEAT:      return "REPEAT";
            case Tag.UNTIL:       return "UNTIL";
            case Tag.WHILE:       return "WHILE";
            case Tag.DO:          return "DO";
            case Tag.READ:        return "READ";
            case Tag.WRITE:       return "WRITE";
            case Tag.EQ:          return "EQ";
            case Tag.NE:          return "NE";
            case Tag.GE:          return "GE";
            case Tag.LE:          return "LE";
            case Tag.AND:         return "AND";
            case Tag.OR:          return "OR";
            case Tag.ID:          return "ID";
            case Tag.INT_CONST:   return "INT_CONST";
            case Tag.FLOAT_CONST: return "FLOAT_CONST";
            case Tag.CHAR_CONST:  return "CHAR_CONST";
            case Tag.LITERAL:     return "LITERAL";
            case Tag.EOF:         return "EOF";
            default:              return "'" + (char) tag + "'";
        }
    }

    private static void imprimirToken(Token t) {
        if (t instanceof Word || t instanceof Num || t instanceof Real)
            System.out.println("<" + nomeDaTag(t.tag) + ", " + t + ">");
        else
            System.out.println("<" + nomeDaTag(t.tag) + ">");
    }

    /* Imprime a tabela na ordem de inserção: palavras reservadas e depois os identificadores, na ordem em que apareceram */
    private static void imprimirTabela(Map<String, Word> words) {
        System.out.println();
        System.out.println("=== Tabela de símbolos ===");
        for (Word w : words.values())
            System.out.println(String.format("%-12s %s", w.getLexeme(), nomeDaTag(w.tag)));
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -cp bin main.Main <arquivo-fonte>");
            return;
        }

        Lexer lexer;
        try {
            lexer = new Lexer(args[0]);
        } catch (FileNotFoundException e) {
            return;
        }

        boolean erro = false;
        try {
            Token t;
            do {
                t = lexer.scan();
                imprimirToken(t);
            } while (t.tag != Tag.EOF);
        } catch (LexicalError e) {
            System.out.println(e.getMessage());
            erro = true;
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            erro = true;
        }

        imprimirTabela(lexer.getWords());
        if (!erro) System.out.println("\nAnálise léxica concluída com sucesso.");
    }
}
