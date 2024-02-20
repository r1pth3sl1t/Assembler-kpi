package parser.unit.operand;

import lexer.Token;
import parser.unit.SentenceUnit;

public abstract class Operand extends SentenceUnit {

    protected int size = 32;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Operand(Token token) {
        super(token);
    }

    public Operand(){ super(); }
}
