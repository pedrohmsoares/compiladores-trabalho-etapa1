package main;

import lexico.*;
import sintatico.*;
import java.io.*;

public class Main {

    /* Recebe o arquivo fonte, roda a análise sintática e mostra o sucesso ou o primeiro erro encontrado */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar compilador.jar <arquivo-fonte>");
            return;
        }

        try {
            Lexer lexer = new Lexer(args[0]);
            Parser parser = new Parser(lexer);
            parser.programa();
            System.out.println("Análise sintática concluída com sucesso.");
        } catch (FileNotFoundException e) {
            // a mensagem "Arquivo não encontrado" já é mostrada pelo Lexer
        } catch (LexicalError e) {
            System.out.println(e.getMessage());
        } catch (SyntaxError e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}
