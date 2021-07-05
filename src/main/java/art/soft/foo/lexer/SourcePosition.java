package art.soft.foo.lexer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Artem8086
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SourcePosition {

    int row, col;

    public void setSourcePosition(SourcePosition position) {
        this.row = position.getRow();
        this.col = position.getCol();
    }

    @Override
    public String toString() {
        return "[" + row + ":" + col + "]";
    }


    public String getErrorMessage(String message, String[] lines) {
        StringBuilder error = new StringBuilder();
        error.append("at line [").append(row).append(',').append(col).append("]:\n");
        try {
            error.append(lines[row - 1].replace('\t', ' '));
            error.append('\n');
            for (int i = col - 1; i > 0; i --) {
                error.append(' ');
            }
            error.append("^\n");
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        error.append("    ").append(message);
        return error.toString();
    }
}
