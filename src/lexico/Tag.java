package lexico;

public class Tag {
    public final static int
            // Palavras reservadas
            PROGRAM = 256,
            BEGIN   = 257,
            END     = 258,
            INT     = 259,
            FLOAT   = 260,
            CHAR    = 261,
            IF      = 262,
            THEN    = 263,
            ELSE    = 264,
            REPEAT  = 265,
            UNTIL   = 266,
            WHILE   = 267,
            DO      = 268,
            READ    = 269,
            WRITE   = 270,

            // Operadores de 2 caracteres
            EQ  = 271, // ==
            NE  = 272, // !=
            GE  = 273, // >=
            LE  = 274, // <=
            AND = 275, // &&
            OR  = 276, // ||

            // Outros tokens
            ID          = 277,
            INT_CONST   = 278,
            FLOAT_CONST = 279,
            CHAR_CONST  = 280,
            LITERAL     = 281,
            EOF         = 282;

    /* Devolve o nome de uma tag em texto, usado nas mensagens de erro do analisador sintático */
    public static String nome(int tag) {
        switch (tag) {
            case PROGRAM:     return "'program'";
            case BEGIN:       return "'begin'";
            case END:         return "'end'";
            case INT:         return "'int'";
            case FLOAT:       return "'float'";
            case CHAR:        return "'char'";
            case IF:          return "'if'";
            case THEN:        return "'then'";
            case ELSE:        return "'else'";
            case REPEAT:      return "'repeat'";
            case UNTIL:       return "'until'";
            case WHILE:       return "'while'";
            case DO:          return "'do'";
            case READ:        return "'read'";
            case WRITE:       return "'write'";
            case EQ:          return "'=='";
            case NE:          return "'!='";
            case GE:          return "'>='";
            case LE:          return "'<='";
            case AND:         return "'&&'";
            case OR:          return "'||'";
            case ID:          return "identificador";
            case INT_CONST:   return "constante inteira";
            case FLOAT_CONST: return "constante float";
            case CHAR_CONST:  return "constante char";
            case LITERAL:     return "literal";
            case EOF:         return "fim de arquivo";
            default:          return "'" + (char) tag + "'";
        }
    }
}
