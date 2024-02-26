package parser.unit.operand;

import lexer.Token;
import lexer.TokenType;
import parser.ParserException;
import util.UtilTables;

import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class ImmOperand extends Operand{

    private int immValue;

    public int getImmValue() {
        return immValue;
    }

    public void setImmValue(int immValue) {
        this.immValue = immValue;
    }

    public ImmOperand(int number, boolean negative) throws ParserException {
        super();
        this.immValue = number * (negative ? -1 : 1);
        this.size = getIntegerSize(immValue);
    }

    public static int getIntegerSize(int number) {

        for(int i = 8; i < 32; i = i << 1){
            if(number >> i == 0 || number >> i == ~0) return i;
        }
        return 32;
    }

    @Override
    public String toString(){
        return name + " = " + immValue + "(" + size + ")";
    }
}
