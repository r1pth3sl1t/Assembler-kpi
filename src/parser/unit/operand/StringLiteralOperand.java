package parser.unit.operand;

import lexer.Token;

import java.nio.charset.StandardCharsets;

public class StringLiteralOperand extends Operand{
    private final byte[] stringAsByteArray;

    public StringLiteralOperand(Token token)  {
        super(token);
        String tmp = token.content().replace("" + token.content().charAt(0), "");
        stringAsByteArray = tmp.getBytes(StandardCharsets.US_ASCII);
        this.size = stringAsByteArray.length;
    }

    @Override
    public String toString() {
        StringBuilder hex = new StringBuilder();

        for(byte b : stringAsByteArray) {
            hex.append(Integer.toHexString(b)).append(" ");
        }
        return hex.toString();
    }
}
