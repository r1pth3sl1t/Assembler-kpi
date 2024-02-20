package parser.unit;

import lexer.Token;
import util.UtilTables;

public class Identifier extends SentenceUnit {

    private long offset = -1L;

    private UtilTables.IdentifierType type;

    public Identifier(Token token) {
        super(token);
    }

    public UtilTables.IdentifierType getType() {
        return type;
    }

    public void setType(UtilTables.IdentifierType type) {
        this.type = type;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "type=" + type +
                ", offset=" + offset +
                ", name='" + name + '\'' +
                '}';
    }
}
