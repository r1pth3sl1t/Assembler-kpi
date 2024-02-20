package lexer;

public enum TokenType {

    T_INSTRUCTION,
    T_DIRECTIVE,

    T_EQU_ALIAS,
    T_EQU_SUB,

    T_GENERAL_REG08,
    T_GENERAL_REG32,

    T_SEGMENT_REG,

    T_TYPE,
    T_PTR,

    T_OPEN_BRACKET,
    T_CLOSE_BRACKET,
    T_COMMA,
    T_COLON,
    T_PLUS,
    T_MINUS,
    T_STAR,
    T_OPEN_PARENTHESIS,
    T_CLOSE_PARENTHESIS,
    T_SLASH,
    T_STRING,
    T_BIN,
    T_HEX,
    T_DEC,
    T_IDENTIFIER,
}
