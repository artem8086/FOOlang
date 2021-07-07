package art.soft.foo.token;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static art.soft.foo.token.TokenType.Type.*;

/**
 * @author Artem8086
 */
public enum TokenType {
    // Value types
    NUMBER_INTEGER(VALUE),
    NUMBER_INTEGER_BIN(VALUE),
    NUMBER_INTEGER_OCT(VALUE),
    NUMBER_INTEGER_HEX(VALUE),
    NUMBER_FLOAT(VALUE),
    IDENTIFIER(VALUE),
    ATOM(VALUE),
    STRING(VALUE),
    DOCUMENTATION(VALUE),

    // Keywords,
    BOOL_TRUE("true", KEYWORD),
    BOOL_FALSE("false", KEYWORD),
    NONE("none", KEYWORD),

    // Guard
    WHERE("where", KEYWORD),

    // Control flow
    IF("if", KEYWORD),
    ELSE("else", KEYWORD),
    MATCH("match", KEYWORD),

    // Cycle
    WHILE("while", KEYWORD),

    // Exceptions
    THROW("throw", KEYWORD),
    TRY("try", KEYWORD),
    CATCH("catch", KEYWORD),
    FINALLY("finally", KEYWORD),

    // Variable declaration
    LET("let", KEYWORD),
    VAR("var", KEYWORD),

    // Type compare
    IS("is", KEYWORD),

    // Futures
    TYPEOF("typeof", KEYWORD),
    YIELD("yield", KEYWORD),

    // Boolean operations
    NOT("not", KEYWORD),
    AND("and", KEYWORD),
    OR("or", KEYWORD),

    // Operators and punctuation,
    PLUS("+", OPERATOR),
    MINUS("-", OPERATOR),
    MUL("*", OPERATOR),
    DIV("/", OPERATOR),
    MOD("%", OPERATOR),

    SHIFT_R(">>", OPERATOR),
    SHIFT_L("<<", OPERATOR),
    BIT_AND("&", OPERATOR),
    BIT_OR("|", OPERATOR),
    BIT_XOR("^", OPERATOR),

    INCREASE("++", OPERATOR),
    DECREASE("--", OPERATOR),

    ASSIGN("=", OPERATOR),
    ASSIGN_PLUS("+=", OPERATOR),
    ASSIGN_MINUS("-=", OPERATOR),
    ASSIGN_MUL("*=", OPERATOR),
    ASSIGN_DIV("/=", OPERATOR),
    ASSIGN_MOD("%=", OPERATOR),
    ASSIGN_AND("&=", OPERATOR),
    ASSIGN_OR("|=", OPERATOR),
    ASSIGN_XOR("^=", OPERATOR),
    ASSIGN_SHR(">>=", OPERATOR),
    ASSIGN_SHL("<<=", OPERATOR),

    TILDE("~", OPERATOR),
    QUESTION("?", OPERATOR),

    EQ("==", OPERATOR),
    NE("!=", OPERATOR),
    LTE("<=", OPERATOR),
    LT("<", OPERATOR),
    GT(">", OPERATOR),
    GTE(">=", OPERATOR),

    ROCKET("->", OPERATOR),
    SEQUENCE(".>", OPERATOR),
    LAMBDA("\\", OPERATOR),
    RANGE_INCL("..", OPERATOR),
    RANGE_INCL_LEFT("..<", OPERATOR),
    RANGE_INCL_RIGHT("<..", OPERATOR),
    RANGE_NOT_INCL("<..<", OPERATOR),
    SPREAD("...", OPERATOR),
    PARTIAL_APPLY("$", OPERATOR), // partial application

    LPAREN("(", OPERATOR),
    RPAREN(")", OPERATOR),
    LBRACKET("[", OPERATOR),
    RBRACKET("]", OPERATOR),
    LBRACE("{", OPERATOR),
    RBRACE("}", OPERATOR),
    COMMA(",", OPERATOR),
    DOT(".", OPERATOR),
    COLON(":", OPERATOR),
    SEMICOLON(";", OPERATOR),

    DECORATOR("@", OPERATOR),

    EOF("eof", OTHER);

    private final String value;

    private final Type type;

    TokenType(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    TokenType(Type type) {
        this.value = null;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public static List<TokenType> getAllByType(Type type) {
        return Arrays.stream(values())
                .filter(token -> token.getType() == type)
                .collect(Collectors.toList());
    }

    public static Map<String, TokenType> toMapByType(Type type) {
        return Arrays.stream(values())
                .filter(token -> token.getType() == type)
                .collect(Collectors.toMap(TokenType::getValue, token -> token));
    }

    @Override
    public String toString() {
        switch (type) {
            case KEYWORD:
            case OPERATOR:
                return "\"" + value + "\"";
            default:
                return super.toString();
        }
    }

    public enum Type {
        VALUE,
        KEYWORD,
        OPERATOR,
        OTHER
    }
}
