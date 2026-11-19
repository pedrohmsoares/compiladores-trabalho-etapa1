# Etapa 2 – material para o relatório

## Estrutura
```
trabalhoFinal/
  src/lexico/      analisador léxico (Etapa 1) + Tag.nome() para as mensagens de erro
  src/sintatico/   Parser.java (analisador sintático) e SyntaxError.java
  src/main/        Main.java
  testes/          testes originais do enunciado
  compilador.jar
  relatorio/etapa2/
    fontes/        cada versão dos testes (testeN_vK.txt), uma por erro corrigido
    saidas/        saída do compilador para cada versão (testeN_vK.out)
    gerar_saidas.sh  compila, gera o jar e refaz todas as saídas
```

## Forma de uso
```
java -jar compilador.jar <arquivo-fonte>
```
ou, compilando: `javac -encoding UTF-8 -d bin src/*/*.java` e `java -cp bin main.Main <arquivo-fonte>`.

A saída é `Análise sintática concluída com sucesso.` ou `Erro sintático na linha N: esperado X, encontrado Y` (erros léxicos continuam aparecendo como `Erro léxico na linha N: ...`). A análise para no primeiro erro (sem recuperação de erro, como permite o enunciado).

## Abordagem e classes
Parser descendente recursivo preditivo (LL(1)), escrito à mão, como nos slides de Análise Sintática Partes 1 e 2: um método para cada não terminal, um token de lookahead (`tok`) e as funções `avancar()` (advance) e `consumir()` (eat).

- `Tag`: ganhou `nome()`, que transforma uma tag em texto para a mensagem de erro.
- `SyntaxError`: exceção com a mensagem e a linha do erro sintático.
- `Parser`: o analisador sintático. Pede os tokens ao `Lexer` um por vez.
- `Main`: cria o `Lexer` e o `Parser`, chama `programa()` e mostra o resultado. Não mostra mais tokens nem tabela de símbolos (pedido do enunciado para as etapas 2 e 3).
- `Lexer`, `Token`, `Word`, `Num`, `Real`, `LexicalError`: sem mudanças desde a Etapa 1.

A linha informada é a do token inesperado.

## Modificações na gramática
No `Parser.java`, cada uma está marcada com o comentário `MUDANÇA NA GRAMÁTICA`.

1. **Recursão à esquerda em simple-expr**
   - original: `simple-expr ::= term | simple-expr addop term`
   - nova: `simple-expr ::= term {addop term}`
2. **Recursão à esquerda em term**
   - original: `term ::= factor-a | term mulop factor-a`
   - nova: `term ::= factor-a {mulop factor-a}`
3. **Fatoração à esquerda em expression** (as duas produções começam com simple-expr)
   - original: `expression ::= simple-expr | simple-expr relop simple-expr`
   - nova: `expression ::= simple-expr [relop simple-expr]`
4. **Fatoração à esquerda em if-stmt** (as duas produções começam com `if condition then stmt-list`)
   - original: `if-stmt ::= if condition then stmt-list end | if condition then stmt-list else stmt-list end`
   - nova: `if-stmt ::= if condition then stmt-list [else stmt-list] end`
5. **Conflito entre decl e assign-stmt.** Depois do `begin`, uma declaração (`a = int`) e uma atribuição (`a = 1`) começam com `identifier "="`. Os FIRST se sobrepõem, então não é LL(1). A decisão foi adiada (fatoração) até o token depois do `=`:
   - original: `program ::= program identifier begin [decl-list] stmt-list end "."`
   - nova:
     ```
     program     ::= program identifier begin corpo end "."
     corpo       ::= identifier corpoId
                   | if-stmt {";" stmt} | while-stmt {";" stmt} | repeat-stmt {";" stmt}
                   | read-stmt {";" stmt} | write-stmt {";" stmt}
     corpoId     ::= "," ident-list "=" type ";" corpo
                   | "=" corpoIgual
     corpoIgual  ::= type ";" corpo
                   | simple-expr {";" stmt}
     ```
   A linguagem continua a mesma: zero ou mais declarações seguidas de uma lista de comandos.
6. **Nome:** a gramática usa `fator-a` e `factor-a` para o mesmo símbolo; foi usado só `factor-a`.

As outras produções ficaram como no enunciado.

## Regras do enunciado que explicam alguns erros dos testes
- As declarações ficam **depois** do `begin` (`program identifier begin [decl-list] stmt-list end "."`).
- `stmt-list ::= stmt {";" stmt}`: o `;` **separa** comandos. `;` antes de `end`, `else` ou `until` é erro.
- `expression` aceita só um `relop`. Por isso `opc == 'N' || opc == 'n'` precisa de parênteses: `(opc == 'N') || (opc == 'n')`.
- `assign-stmt` usa `simple-expr`, então não pode ter `relop` numa atribuição.
- `1a` não é erro léxico: o léxico lê `1` (constante) e depois `a` (identificador). Por isso vira erro sintático.

## Testes (ponto de partida: as versões sem erro léxico da Etapa 1)
| Teste | Versão | Erro | Correção feita para a próxima versão |
|---|---|---|---|
| 1 | v1 | linha 1: esperado 'program', encontrado 'programa' | `programa` → `program` |
| 1 | v2 | linha 4: esperado 'begin', encontrado 'base' | `begin` movido para antes das declarações |
| 1 | v3 | linha 6: esperado identificador, encontrado '1' | `1a` → `a1` |
| 1 | v4 | linha 6: esperado '=', encontrado 'is' | `is` → `=` |
| 1 | v5 | linha 10: esperado '=', encontrado '(' | `Write` → `write` |
| 1 | v6 | linha 20: esperado '(', encontrado '"Area: "' | `write "Area: "` → `write ("Area: ")` |
| 1 | v7 | linha 26: esperado comando, encontrado 'end' | tirado o `;` do último `write` |
| 1 | v8 | sucesso | |
| 2 | v1 | linha 3: esperado 'begin', encontrado 'a' | `begin` colocado antes das declarações |
| 2 | v2 | linha 3: esperado identificador, encontrado '1' | `1c` → `c` |
| 2 | v3 | linha 7: esperado '=', encontrado '(' | `Read` → `read` |
| 2 | v4 | sucesso | |
| 3 | v1 | linha 2: esperado 'begin', encontrado ',' | colocado o nome do programa (`program teste3`) |
| 3 | v2 | linha 2: esperado 'begin', encontrado 'a' | `begin` movido para antes das declarações |
| 3 | v3 | linha 6: esperado ';', encontrado 'b' | `;` depois de `b = float` |
| 3 | v4 | linha 17: esperado 'then', encontrado 'write' | colocado `then` |
| 3 | v5 | linha 18: esperado comando, encontrado 'else' | tirado o `;` antes do `else` |
| 3 | v6 | linha 19: esperado comando, encontrado fim de arquivo | tirado o `;` do último `write` |
| 3 | v7 | linha 19: esperado 'end', encontrado fim de arquivo | colocado o `end` do `if` |
| 3 | v8 | linha 20: esperado 'end', encontrado fim de arquivo | colocado o `end.` do programa |
| 3 | v9 | sucesso | |
| 4 | v1 | linha 1: esperado 'program', encontrado 'programa' | `programa` → `program` |
| 4 | v2 | linha 5: esperado 'begin', encontrado 'pontuacao' | `begin` movido para antes das declarações |
| 4 | v3 | linha 5: esperado tipo (int, float ou char), encontrado 'inteiro' | `inteiro` → `int` |
| 4 | v4 | linha 17: esperado ')', encontrado 'do' | fechado o `)` da condição do `while` |
| 4 | v5 | linha 24: esperado 'end', encontrado 'write' | `;` depois do `end` do `if` |
| 4 | v6 | linha 29: esperado comando, encontrado 'end' | tirado o `;` do último `read` |
| 4 | v7 | linha 31: esperado '.', encontrado fim de arquivo | `end` → `end.` |
| 4 | v8 | sucesso | |
| 5 | v1 | linha 4: esperado 'begin', encontrado 'a' | `begin` movido para antes das declarações |
| 5 | v2 | linha 18: esperado 'then', encontrado 'end' | `end` → `then` no primeiro `if` |
| 5 | v3 | linha 25: esperado comando, encontrado 'else' | tirado o `;` antes do `else` |
| 5 | v4 | linha 33: esperado comando, encontrado 'until' | tirado o `;` antes do `until` |
| 5 | v5 | linha 33: esperado ')', encontrado '==' | `until ((opc == 'N') \|\| (opc == 'n'))` |
| 5 | v6 | sucesso | |
| 6a | v1 | linha 3: esperado comando, encontrado 'end' | tirado o `;` antes do `end` |
| 6a | v2 | sucesso | |
| 6b | – | sucesso | |

Observação: quando o erro é "encontrado fim de arquivo", a linha informada é a seguinte à última linha do arquivo (ex.: linha 31 num arquivo de 30 linhas). Isso acontece porque o arquivo termina com uma quebra de linha, e o léxico a conta antes de devolver o EOF.
