package art.soft.foo.scanner;

import art.soft.foo.config.Config;
import art.soft.foo.token.Token;
import art.soft.foo.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static art.soft.foo.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Artem8086
 */
public class ScannerTest {

    @Test
    public void testNumbers() {
        String input = "1_000 3.1415 0xCAFEBABE 0Xf7_d6_c5 1e10 1.2e-2  12..10";
        List<Token> expList = list(NUMBER_INTEGER, NUMBER_FLOAT, NUMBER_INTEGER_HEX, NUMBER_INTEGER_HEX, NUMBER_FLOAT,
                NUMBER_FLOAT, NUMBER_INTEGER, RANGE_INCL, NUMBER_INTEGER);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("1000", result.get(0).getText());
        assertEquals("3.1415", result.get(1).getText());
        assertEquals("CAFEBABE", result.get(2).getText());
        assertEquals("f7d6c5", result.get(3).getText());
        assertEquals("1e10", result.get(4).getText());
        assertEquals("1.2e-2", result.get(5).getText());
        assertEquals("12", result.get(6).getText());
        assertEquals("10", result.get(8).getText());
    }

    @Test
    public void testOctBinNumbers() {
        String input = "0o123 0o34_345 0b101 0B1111_1111";
        List<Token> expList = list(NUMBER_INTEGER_OCT, NUMBER_INTEGER_OCT, NUMBER_INTEGER_BIN, NUMBER_INTEGER_BIN);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("123", result.get(0).getText());
        assertEquals("34345", result.get(1).getText());
        assertEquals("101", result.get(2).getText());
        assertEquals("11111111", result.get(3).getText());
    }

    @Test
    public void testNumbersError() {
        String input = "3.14.15 0Xf7_p6_s5";
        assertThrows(ScannerException.class, () -> Scanner.tokenize(input, new Config()));
    }

    @Test
    public void testArithmetic() {
        String input = "x=-1+((2*3)%(4/5))";
        List<Token> expList = list(IDENTIFIER, ASSIGN, MINUS, NUMBER_INTEGER, PLUS, LPAREN, LPAREN, NUMBER_INTEGER, MUL,
                NUMBER_INTEGER, RPAREN, MOD, LPAREN, NUMBER_INTEGER, DIV, NUMBER_INTEGER, RPAREN, RPAREN);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("x", result.get(0).getText());
    }

    @Test
    public void testKeywords() {
        String input = "if else while let var where match";
        List<Token> expList = list(IF, ELSE, WHILE, LET, VAR, WHERE, MATCH);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testIdentifiers() {
        String input = "\"text\n\ntext\" true false none";
        List<Token> expList = list(STRING, BOOL_TRUE, BOOL_FALSE, NONE);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testString() {
        String input = "\"1\\\"2\"";
        List<Token> expList = list(STRING);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("1\"2", result.get(0).getText());
    }

    @Test
    public void testMultilineString() {
        String input = "\"1\\\"2\" \"text \\\n     test\"";
        List<Token> expList = list(STRING, STRING);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("1\"2", result.get(0).getText());
        assertEquals("text test", result.get(1).getText());
    }

    @Test
    public void testTextBlock() {
        String input = "   \\\\text\n   \\\\test \"string\"\n a";
        List<Token> expList = list(STRING, IDENTIFIER);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("text\ntest \"string\"", result.get(0).getText());
    }

    @Test
    public void testEmptyString() {
        String input = "\"\"";
        List<Token> expList = list(STRING);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("", result.get(0).getText());
    }

    @Test
    public void testStringError() {
        String input = "\"1\"\"";
        List<Token> expList = list(STRING);
        assertThrows(ScannerException.class, () -> Scanner.tokenize(input, new Config()));
    }

    @Test
    public void testOperators() {
        String input = "=+-*/%<>&|~$";
        List<Token> expList = list(ASSIGN, PLUS, MINUS, MUL, DIV, MOD, LT, GT, BIT_AND, BIT_OR, TILDE, PARTIAL_APPLY);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testOperators2Char() {
        String input = "== != <= >= ==+ >=- ->";
        List<Token> expList = list(EQ, NE, LTE, GTE, EQ, PLUS, GTE, MINUS, ROCKET);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testComments() {
        String input = "// 1234 \n /* */ 123 /* \n 12345 \n\n\n */";
        List<Token> expList = list(NUMBER_INTEGER);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
        assertEquals("123", result.get(0).getText());
    }

    @Test
    public void testComments2() {
        String input = "// /* 1234 \n */";
        List<Token> expList = list(MUL, DIV);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testCommentsError() {
        String input = "/* 1234 \n";
        assertThrows(ScannerException.class, () -> Scanner.tokenize(input, new Config()));
    }

    @Test
    public void testSingleQuoteStringError() {
        String input = "' 1234";
        assertThrows(ScannerException.class, () -> Scanner.tokenize(input, new Config()));
    }

    @Test
    public void testSingleQuoteStingIdentifier() {
        String input = "'\"test\" string' = 1";
        List<Token> expList = list(STRING, ASSIGN, NUMBER_INTEGER);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testUnicodeCharacterExtendedWordIdentifier() {
        String input = "let t = 1";
        List<Token> expList = list(LET, IDENTIFIER, ASSIGN, NUMBER_INTEGER);
        List<Token> result = Scanner.tokenize(input, new Config());
        assertTokens(expList, result);
    }

    @Test
    public void testUnicodeCharacterEOF() {
        String input = "€";
        assertThrows(ScannerException.class, () -> Scanner.tokenize(input, new Config()));
    }

    private static void assertTokens(List<Token> expList, List<Token> result) {
        final int length = expList.size();
        assertEquals(length, result.size());
        for (int i = 0; i < length; i++) {
            assertEquals(expList.get(i).getType(), result.get(i).getType());
        }
    }

    private static List<Token> list(TokenType... types) {
        final List<Token> list = new ArrayList<Token>();
        for (TokenType t : types) {
            list.add(token(t));
        }
        return list;
    }

    private static Token token(TokenType type) {
        return token(type, "", 0, 0, 0);
    }

    private static Token token(TokenType type, String text, int row, int col, int len) {
        return new Token(type, text, row, col, len);
    }
}
