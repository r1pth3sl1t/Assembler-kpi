package parser.unit;

import lexer.Token;

public abstract class SentenceUnit {

    protected String name;
    protected int firstTokenIdx;
    protected int tokensNumber;

    public int getFirstTokenIdx() {
        return firstTokenIdx;
    }

    public void setFirstTokenIdx(int firstTokenIdx) {
        this.firstTokenIdx = firstTokenIdx + 1;
    }

    public int getTokensNumber() {
        return tokensNumber;
    }

    public void setTokensNumber(int tokensNumber) {
        this.tokensNumber = tokensNumber;
    }

    public String getName() {
        return name;
    }

    public SentenceUnit(){
        this.name = "";
    }
    public SentenceUnit(Token token){
        this.name = token.content();
    }

}
