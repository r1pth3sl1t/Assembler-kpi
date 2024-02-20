package parser.unit;

import lexer.Token;

public abstract class SentenceUnit {

    protected String name;
    protected int firstTokenIdx;
    protected int tokensNumber;

    public int getFirstTokenIdx() {
        return firstTokenIdx;
    }

    public SentenceUnit setFirstTokenIdx(int firstTokenIdx) {
        this.firstTokenIdx = firstTokenIdx + 1;
        return this;
    }

    public int getTokensNumber() {
        return tokensNumber;
    }

    public SentenceUnit setTokensNumber(int tokensNumber) {
        this.tokensNumber = tokensNumber;
        return this;
    }

    public String getName() {
        return name;
    }

    public SentenceUnit(){
        this.name = "";
    }
    public SentenceUnit(Token token){
        this.name = token.getContent();
    }

}
