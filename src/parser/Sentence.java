package parser;

import lexer.TokenizedLine;
import parser.unit.Mnemonic;
import parser.unit.Identifier;
import parser.unit.operand.Operand;
import util.UtilTables;

import java.util.LinkedList;
import java.util.List;

public class Sentence {
    private final TokenizedLine tokenizedLine;

    private Identifier identifier;

    private Mnemonic mnemonic;

    private List<Operand> operands;

    private ParserException exception = null;

    public Sentence(TokenizedLine line){
        this.operands = new LinkedList<>();
        this.tokenizedLine = line;
    }

    public void pushOperand(Operand operand){
        this.operands.add(operand);
    }

    public TokenizedLine getTokenizedLine() {
        return tokenizedLine;
    }


    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append(this.tokenizedLine.toString());
        if(this.tokenizedLine.getTokens().isEmpty())
            return result.append("\n<empty>\n").toString();


        if(identifier != null) {
            result.append("<").append(identifier.getType() == UtilTables.IdentifierType.NEAR ? "label" : "name").append(" (").append(identifier.getFirstTokenIdx()).append(",").append(identifier.getTokensNumber()).append(")> ");
        }
        if(mnemonic != null) result.append("<mnem (").append(mnemonic.getFirstTokenIdx()).append(",").append(mnemonic.getTokensNumber()).append(")> ");
        if(!operands.isEmpty()) {
            for(int i = 0; i < operands.size() - 1; i++){
                result.append("<op (").append(operands.get(i).getFirstTokenIdx()).append(",").append(operands.get(i).getTokensNumber()).append(")>, ");
            }
            result.append("<op (").append(operands.get(operands.size() - 1).getFirstTokenIdx()).append(",").append(operands.get(operands.size() - 1).getTokensNumber()).append(")>");
        }
        return result.toString() + '\n';
    }

    public void setMnemonic(Mnemonic mnemonic) {
        this.mnemonic = mnemonic;
    }

    public void setOperands(List<Operand> operands) {
        this.operands = operands;
    }

    public Mnemonic getMnemonic() {
        return mnemonic;
    }

    public List<Operand> getOperands() {
        return operands;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public void setException(ParserException exception) {
        this.exception = exception;
    }

    public boolean hasErrors(){
        return this.exception != null || this.tokenizedLine.getException() != null;
    }


    public String getErrorMessages(){
        if(tokenizedLine.getException() != null) {
            return tokenizedLine.getException().getMessage();
        }
        if(exception != null) {
            return exception.getMessage();
        }
        return "";
    }
}
