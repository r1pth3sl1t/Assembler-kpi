package parser.unit.operand;

import lexer.Token;
import lexer.TokenType;
import parser.ParserException;
import util.UtilTables;

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

    public ImmOperand(List<Token> absoluteExpression) throws ParserException {
        super();
        StringBuilder tmp = new StringBuilder();
        for(Token token : absoluteExpression) {
            tmp.append(token.getContent());
        }
        this.name = tmp.toString();
        this.immValue = parseAbsoluteExpression(absoluteExpression);
        this.size = getIntegerSize(immValue);
    }

    public static int parseAbsoluteExpression(List<Token> absoluteExpression) throws ParserException {
        try {
            Stack<TokenType> operators = new Stack<>();
            Stack<Integer> operands = new Stack<>();
            if (!absoluteExpression.isEmpty() && absoluteExpression.get(0).getType() == TokenType.T_MINUS) {
                operands.add(0);
                operators.add(TokenType.T_MINUS);
                absoluteExpression.remove(0);
            }
            for (Token token : absoluteExpression) {
                switch (token.getType()) {
                    case T_HEX, T_DEC, T_BIN -> operands.push(UtilTables.immPool.get(token.getContent()));
                    case T_PLUS, T_MINUS, T_STAR, T_SLASH -> {
                        if (operators.isEmpty() || getPriority(operators.peek()) < getPriority(token.getType())) {
                            operators.push(token.getType());
                        } else {
                            while (!operators.isEmpty() && getPriority(operators.peek()) >= getPriority(token.getType()))
                                popOperators(operands, operators);
                            operators.push(token.getType());
                        }
                    }
                    case T_OPEN_PARENTHESIS -> operators.push(token.getType());
                    case T_CLOSE_PARENTHESIS -> {
                        while (!operators.isEmpty() && operators.peek() != TokenType.T_OPEN_PARENTHESIS) {
                            popOperators(operands, operators);
                        }
                        if(operators.isEmpty() || operators.peek() != TokenType.T_OPEN_PARENTHESIS)
                            throw new ParserException("Missing parenthesis");
                        operators.pop();
                    }
                    default -> throw new ParserException("This token is not allowed in absolute expression: '" + token.getContent() + "'");
                }
            }
            while (!operators.isEmpty()) {
                popOperators(operands, operators);
            }
            int res = operands.pop();
            if(!operands.isEmpty() || !operators.isEmpty()) throw new ParserException("Invalid absolute expression");
            return res;
        } catch(EmptyStackException e) {
            throw new ParserException("Invalid absolute expression");
        }
    }

    private static void popOperators(Stack<Integer> operands, Stack<TokenType> operators)
    throws EmptyStackException {
        TokenType operation = operators.pop();
        int left = operands.pop();
        int right = operands.pop();
        int result = switch (operation) {
            case T_MINUS -> right - left;
            case T_PLUS -> left + right;
            case T_STAR -> left * right;
            case T_SLASH -> right / left;
            default -> 0;
        };
        operands.push(result);
    }

    public static byte getIntegerSize(int number){
        if(number > -127 && number < 128) return 8;
        if(number > -32768 && number < 32767) return 16;
        return 32;
    }

    private static byte getPriority(TokenType tokenType){
        return switch (tokenType) {
            case T_OPEN_PARENTHESIS -> 0;
            case T_PLUS, T_MINUS -> 1;
            case T_STAR, T_SLASH -> 2;
            default -> -1;
        };
    }

    @Override
    public String toString(){
        return name + " = " + immValue + "(" + size + ")";
    }
}
