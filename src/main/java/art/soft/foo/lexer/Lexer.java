package art.soft.foo.lexer;


import art.soft.foo.token.SourcePosition;
import art.soft.foo.token.Token;
import art.soft.foo.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem8086
 */
public class Lexer {

    public static List<Token> tokenize(String input) {
        return new Lexer(input).tokenize();
    }

    private static final String OPERATOR_CHARS = "@$+-*/%()[]{}=<>!&|.,^~?:;\\";

    private static final Map<String, TokenType> OPERATORS = TokenType.toMapByType(TokenType.Type.OPERATOR);

    private static final Map<String, TokenType> KEYWORDS = TokenType.toMapByType(TokenType.Type.KEYWORD);

    private final List<Token> tokens;

    private final String input;

    private final int length;

    private final StringBuilder buffer;

    private int pos;
    private int startRow, startCol;
    private int row, col, len;

    public Lexer(String input) {
        this.input = input;
        length = input.length();

        buffer = new StringBuilder();
        tokens = new ArrayList<>();
        row = col = 1;
        startRow = startCol = 1;
    }

    public List<Token> tokenize() {
        while (pos < length) {
            startRow = row;
            startCol = col;
            len = 0;
            final char current = peek(0);
            if (Character.isDigit(current)) tokenizeNumber();
            else if (isIdentifierStart(current)) tokenizeIdentifier();
            else if (current == '#') tokenizeAtomIdentifier();
            else if (current == '`') tokenizeExtendedWord();
            else if (current == '"') tokenizeString('"');
            else if (current == '\'') tokenizeString('\'');
            else if (current == '\\' && peek(1) == '\\') tokenizeTextBlock();
            else if (OPERATOR_CHARS.indexOf(current) != -1) {
                tokenizeOperator();
            } else {
                // whitespaces
                if (current == '\t') {
                    throw error("Tab indentation not allowed");
                }
                if (!Character.isWhitespace(current)) {
                    throw error("Unknown character '" + current + "'");
                }
                next();
            }
        }
        return tokens;
    }

    private void tokenizeNumber() {
        clearBuffer();
        char current = peek(0);
        if (current == '0') {
            char nextChar = peek(1);
            if (nextChar == 'x' || (nextChar == 'X')) {
                next();
                next();
                tokenizeHexNumber();
                return;
            } else if (nextChar == 'o') {
                next();
                next();
                tokenizeOctNumber();
                return;
            } else if (nextChar == 'b' || nextChar == 'B') {
                next();
                next();
                tokenizeBinNumber();
                return;
            }
        }
        boolean isFloat = false;
        while (true) {
            if (current == '.') {
                if (!Character.isDigit(peek(1))) {
                    break;
                }
                isFloat = true;
                if (buffer.indexOf(".") != -1) throw error("Invalid float number");
                buffer.append(current);
            } else if (current == 'e' || current == 'E') {
                isFloat = true;
                if (buffer.indexOf("e") != -1) throw error("Invalid float number");
                buffer.append('e');
                current = next();
                if (current != '+' && current != '-' && !Character.isDigit(current)) {
                    throw error("Invalid float number");
                }
                buffer.append(current);
                if (current == '+' || current == '-') {
                    current = next();
                    if (!Character.isDigit(current)) {
                        throw error("Invalid float number");
                    }
                    continue;
                }
            } else if (current == '_') {
                if (isFloat) {
                    throw error("Underscore ('_') not available in float part of number");
                }
            } else if (!Character.isDigit(current)) {
                break;
            } else {
                buffer.append(current);
            }
            current = next();
        }
        addToken(isFloat ? TokenType.NUMBER_FLOAT : TokenType.NUMBER_INTEGER, buffer.toString());
    }

    private void tokenizeHexNumber() {
        clearBuffer();
        char current = peek(0);
        while (isHexNumber(current) || (current == '_')) {
            if (current != '_') {
                // allow _ symbol
                buffer.append(current);
            }
            current = next();
        }
        final int length = buffer.length();
        if (length > 0) {
            addToken(TokenType.NUMBER_INTEGER_HEX, buffer.toString());
        } else {
            throw error("Invalid hex number");
        }
    }

    private void tokenizeOctNumber() {
        clearBuffer();
        char current = peek(0);
        while (isOctNumber(current) || (current == '_')) {
            if (current != '_') {
                // allow _ symbol
                buffer.append(current);
            }
            current = next();
        }
        final int length = buffer.length();
        if (length > 0) {
            addToken(TokenType.NUMBER_INTEGER_OCT, buffer.toString());
        } else {
            throw error("Invalid oct number");
        }
    }

    private void tokenizeBinNumber() {
        clearBuffer();
        char current = peek(0);
        while (isBinNumber(current) || (current == '_')) {
            if (current != '_') {
                // allow _ symbol
                buffer.append(current);
            }
            current = next();
        }
        final int length = buffer.length();
        if (length > 0) {
            addToken(TokenType.NUMBER_INTEGER_BIN, buffer.toString());
        } else {
            throw error("Invalid bin number");
        }
    }

    private static boolean isHexNumber(char current) {
        return Character.isDigit(current)
                || ('a' <= current && current <= 'f')
                || ('A' <= current && current <= 'F');
    }

    private static boolean isOctNumber(char current) {
        return '0' <= current && current <= '7';
    }

    private static boolean isBinNumber(char current) {
        return '0' == current || current == '1';
    }

    private void tokenizeOperator() {
        char current = peek(0);
        if (current == '/') {
            if (peek(1) == '/') {
                next();
                next();
                if (peek(0) == '/' && peek(1) != '/') {
                    next();
                    tokenizeDocumentation();
                } else {
                    tokenizeComment();
                }
                return;
            } else if (peek(1) == '*') {
                next();
                next();
                tokenizeMultilineComment();
                return;
            }
        }
        clearBuffer();
        while (true) {
            final String text = buffer.toString();
            if (!text.isEmpty() && !OPERATORS.containsKey(text + current)) {
                addToken(OPERATORS.get(text));
                return;
            }
            buffer.append(current);
            current = next();
        }
    }

    private void tokenizeIdentifier() {
        clearBuffer();
        buffer.append(peek(0));
        char current = next();
        while (true) {
            if (!isIdentifierPart(current)) {
                break;
            }
            buffer.append(current);
            current = next();
        }

        final String word = buffer.toString();
        if (KEYWORDS.containsKey(word)) {
            addToken(KEYWORDS.get(word));
        } else {
            addToken(TokenType.IDENTIFIER, word);
        }
    }

    private void tokenizeAtomIdentifier() {
        char current = next();
        if (current == '\'' || current == '"') {
            tokenizeRawString(current);
            return;
        }
        if (!isIdentifierStart(current)) {
            throw new LexerException("Atom identifier must start with correct identifier symbol");
        }
        clearBuffer();
        while (true) {
            buffer.append(current);
            current = next();
            if (!isIdentifierPart(current)) {
                break;
            }
        }

        final String word = buffer.toString();
        addToken(TokenType.ATOM, word);
    }

    private void tokenizeExtendedWord() {
        next();// skip `
        clearBuffer();
        char current = peek(0);
        while (true) {
            if (current == '`') break;
            if (current == '\0') throw error("Reached end of file while parsing extended identifier.");
            if (current == '\n' || current == '\r') throw error("Reached end of line while parsing extended identifier.");
            buffer.append(current);
            current = next();
        }
        next(); // skip closing `
        addToken(TokenType.IDENTIFIER, buffer.toString());
    }

    private void tokenizeString(char closingSymbol) {
        next(); // skip start character
        clearBuffer();
        while (true) {
            char current = peek(0);
            if (charEscape(current)) continue;
            if (current == closingSymbol) break;
            if (current == '\0') throw error("Reached end of source code while parsing text string.");
            buffer.append(current);
            next();
        }
        next(); // skip closing character

        addToken(TokenType.STRING, buffer.toString());
    }

    private void tokenizeRawString(char closingSymbol) {
        next(); // skip start character
        clearBuffer();
        while (true) {
            char current = peek(0);
            if (current == closingSymbol) break;
            if (current == '\0') throw error("Reached end of source code while parsing text string.");
            buffer.append(current);
            next();
        }
        next(); // skip closing character

        addToken(TokenType.STRING, buffer.toString());
    }

    private void tokenizeTextBlock() {
        next(); // skip '\'
        next(); // skip '\'
        clearBuffer();
        while (true) {
            char current = peek(0);
            if (charEscape(current)) continue;
            if (current == '\n') {
                while (Character.isWhitespace(next())) {}
                if (peek(0) != '\\') break;
                if (next() != '\\') { // skip '\'
                    throw new LexerException("Incorrect multi line string. '\\' excepted ");
                }
            }
            if (current == '\0') break;
            buffer.append(current);
            next();
        }

        addToken(TokenType.STRING, buffer.toString());
    }

    private boolean charEscape(char current) {
        if (current == '\\') {
            current = next();
            switch (current) {
                case '"': next(); buffer.append('"'); return true;
                case '\'': next(); buffer.append('\''); return true;
                case '0': next(); buffer.append('\0'); return true;
                case 'b': next(); buffer.append('\b'); return true;
                case 'f': next(); buffer.append('\f'); return true;
                case 'n': next(); buffer.append('\n'); return true;
                case 'r': next(); buffer.append('\r'); return true;
                case 't': next(); buffer.append('\t'); return true;
                case '\n':
                    while (Character.isWhitespace(next())) {}
                    return true;
                case 'u': // http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.3
                    int rollbackPosition = pos;
                    int rollbackCol = col;
                    int rollbackRow = row;
                    while (current == 'u') current = next();
                    int escapedValue = 0;
                    for (int i = 12; i >= 0 && escapedValue != -1; i -= 4) {
                        if (isHexNumber(current)) {
                            escapedValue |= (Character.digit(current, 16) << i);
                        } else {
                            escapedValue = -1;
                        }
                        current = next();
                    }
                    if (escapedValue >= 0) {
                        buffer.append((char) escapedValue);
                    } else {
                        // rollback
                        buffer.append("\\u");
                        pos = rollbackPosition;
                        col = rollbackCol;
                        row = rollbackRow;
                    }
                    return true;
            }
            buffer.append('\\');
            return true;
        }
        return false;
    }

    private void tokenizeDocumentation() {
        clearBuffer();
        boolean skipWhitespace = true;
        char current = peek(0);
        while ("\r\n\0".indexOf(current) == -1) {
            if (skipWhitespace && Character.isWhitespace(current)) {
                skipWhitespace = false;
            } else {
                buffer.append(current);
            }
            current = next();
        }
        addToken(TokenType.DOCUMENTATION, buffer.toString());
    }

    private void tokenizeComment() {
        char current = peek(0);
        while ("\r\n\0".indexOf(current) == -1) {
            current = next();
        }
    }

    private void tokenizeMultilineComment() {
        char current = peek(0);
        while (true) {
            if (current == '*' && peek(1) == '/') break;
            if (current == '\0') throw error("Reached end of source code while parsing multiline comment");
            current = next();
        }
        next(); // *
        next(); // /
    }

    private boolean isIdentifierStart(char current) {
        return ('a' <= current && current <= 'z') || ('A' <= current && current <= 'Z');
    }

    private boolean isIdentifierPart(char current) {
        return ('0' <= current && current <= '9') || ('a' <= current && current <= 'z') ||
                ('A' <= current && current <= 'Z') || (current == '_');
    }

    private void clearBuffer() {
        buffer.setLength(0);
    }

    private char next() {
        len++;
        pos++;
        final char result = peek(0);
        if (result == '\n') {
            row++;
            col = 0;
        } else col++;
        return result;
    }

    private char peek(int relativePosition) {
        final int position = pos + relativePosition;
        if (position >= length) return '\0';
        return input.charAt(position);
    }

    private void addToken(TokenType type) {
        addToken(type, "");
    }

    private void addToken(TokenType type, String text) {
        tokens.add(new Token(type, text, startRow, startCol, len));
    }

    private LexerException error(String text) {
        SourcePosition position = new SourcePosition(startCol, startRow, len);
        return new LexerException(position.getErrorMessage("LexerError: " + text, input.split("\n")));
    }
}
