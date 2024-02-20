package parser.unit.operand;

import lexer.Token;

public class DirectMemReferenceOperand extends Operand{

    public String getSegmentOverride() {
        return segmentOverride;
    }

    public DirectMemReferenceOperand setSegmentOverride(String segmentOverride) {
        this.segmentOverride = segmentOverride;
        return this;
    }

    private String segmentOverride;

    public DirectMemReferenceOperand(Token token) {
        super(token);
    }

    @Override
    public String toString(){
        return segmentOverride + ": "+ size +" ptr " +name;
    }
}
