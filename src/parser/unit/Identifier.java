package parser.unit;

import lexer.Token;
import util.UtilTables;

public class Identifier extends SentenceUnit {

    private int offset = -1;

    private String segment;

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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }
}
