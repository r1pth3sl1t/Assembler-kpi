package parser.unit.operand;

import lexer.Token;

public class EquDirectiveOperand extends Operand {

    public EquDirectiveOperand(Token token){
        super(token);
    }

    public String toString(){
        return name;
    }
}
