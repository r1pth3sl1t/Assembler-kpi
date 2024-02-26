package parser.unit.operand;

import lexer.Token;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class StringLiteralOperand extends Operand{
    private byte[] stringAsByteArray;

    public StringLiteralOperand(Token token)  {
        super(token);
        String tmp = token.getContent().replace("" + token.getContent().charAt(0), "");
        stringAsByteArray = tmp.getBytes(StandardCharsets.US_ASCII);
        this.size = stringAsByteArray.length;
    }

    public byte[] getStringAsByteArray() {
        return stringAsByteArray;
    }

    public void setStringAsByteArray(byte[] stringAsByteArray) {
        this.stringAsByteArray = stringAsByteArray;
    }

    @Override
    public String toString() {
        return name + "(" + size +")";
    }
}
