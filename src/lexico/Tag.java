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
}
