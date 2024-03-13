package parser.unit.operand;

import lexer.Token;
import lexer.TokenType;
import parser.ParserException;
import util.UtilTables;

public class ImmOperand extends Operand{

    private final int immValue;

    public int getImmValue() {
        return immValue;
    }

    public ImmOperand(int number, boolean negative) throws ParserException {
        super();
        this.immValue = number * (negative ? -1 : 1);
        this.size = getUnsignedIntegerSize(immValue) >> 3;
    }

    public ImmOperand(Token token, boolean negative) throws ParserException {
        this(token,negative, true);
    }

    public ImmOperand(Token token, boolean negative, boolean considerSigned) throws ParserException {
        this(UtilTables.immPool.get(token.content()),negative);
        if(token.type() == TokenType.T_BIN || token.type() == TokenType.T_HEX) considerSigned = false;
        this.size = (considerSigned ? getSignedIntegerSize(immValue) : getUnsignedIntegerSize(immValue)) >> 3;
    }

    public ImmOperand(String textConstant, boolean negative) throws ParserException {
        int immValue = 0;
        String string = textConstant;
        string = string.replace("" + string.charAt(0), "");
        if(!string.isEmpty()) {
            if(string.length() > 4)
                throw new ParserException("Constant value out of range");
            for(int i = 0; i < string.length(); i++){
                immValue = immValue | string.charAt(i) << (string.length() - i - 1) * 8;
            }
        }
        this.immValue = immValue * (negative ? -1 : 1);
    }

    public static int getUnsignedIntegerSize(int number) {
        for(int i = 8; i < 32; i = i << 1){
            if(number >> i == 0 || number >> i == ~0) return i;
        }
        return 32;
    }

    public static int getSignedIntegerSize(int number) {
        if(number >= -128 && number < 128) return 8;
        if(number >= -32768 && number < 32768) return 16;
        return 32;
    }

    @Override
    public String toString(){
        return String.format("%" + (size << 1) + "s",Integer.toHexString(immValue)).replace(' ', '0');
    }
}
