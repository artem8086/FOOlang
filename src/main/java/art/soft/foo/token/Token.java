package art.soft.foo.token;

import lombok.Value;

/**
 *
 * @author Artem8086
 */
@Value
public class Token {

    public final static String THIS_IDENTIFIER = "this";
    public final static String SUPER_IDENTIFIER = "super";

    TokenType type;
    String text;
    SourcePosition position;

    public Token(TokenType type, String text, int col, int row, int len) {
        this.type = type;
        this.text = text;
        this.position = new SourcePosition(col, row, len);
    }

    public String position() {
        return position.toString();
    }

    @Override
    public String toString() {
        return type + " " + position() + " " + text;
    }
}
