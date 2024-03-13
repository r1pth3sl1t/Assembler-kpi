package lexer;

import util.UtilTables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Lexer {

    private static final Map<String, TokenType> keywords;

    private String currentLine;
    private int currentCharIndex = 0;
    private int currentLineIndex = 0;

    private boolean foundEqu = false;

    static {
        keywords = new HashMap<>();
        keywords.put("AL", TokenType.T_GENERAL_REG08);
        keywords.put("AH", TokenType.T_GENERAL_REG08);
        keywords.put("BL", TokenType.T_GENERAL_REG08);
        keywords.put("BH", TokenType.T_GENERAL_REG08);
        keywords.put("CL", TokenType.T_GENERAL_REG08);
        keywords.put("CH", TokenType.T_GENERAL_REG08);
        keywords.put("DL", TokenType.T_GENERAL_REG08);
        keywords.put("DH", TokenType.T_GENERAL_REG08);

        keywords.put("AX", TokenType.T_BANNED_REG);
        keywords.put("BX", TokenType.T_BANNED_REG);
        keywords.put("CX", TokenType.T_BANNED_REG);
        keywords.put("DX", TokenType.T_BANNED_REG);
        keywords.put("SI", TokenType.T_BANNED_REG);
        keywords.put("DI", TokenType.T_BANNED_REG);
        keywords.put("SP", TokenType.T_BANNED_REG);
        keywords.put("BP", TokenType.T_BANNED_REG);
        keywords.put("IP", TokenType.T_BANNED_REG);
        keywords.put("EIP", TokenType.T_BANNED_REG);

        keywords.put("EAX", TokenType.T_GENERAL_REG32);
        keywords.put("EBX", TokenType.T_GENERAL_REG32);
        keywords.put("ECX", TokenType.T_GENERAL_REG32);
        keywords.put("EDX", TokenType.T_GENERAL_REG32);
        keywords.put("EBP", TokenType.T_GENERAL_REG32);
        keywords.put("ESP", TokenType.T_GENERAL_REG32);
        keywords.put("EDI", TokenType.T_GENERAL_REG32);
        keywords.put("ESI", TokenType.T_GENERAL_REG32);

        keywords.put("CS", TokenType.T_SEGMENT_REG);
        keywords.put("DS", TokenType.T_SEGMENT_REG);
        keywords.put("SS", TokenType.T_SEGMENT_REG);
        keywords.put("ES", TokenType.T_SEGMENT_REG);
        keywords.put("GS", TokenType.T_SEGMENT_REG);
        keywords.put("FS", TokenType.T_SEGMENT_REG);

        keywords.put("STOSB", TokenType.T_INSTRUCTION);
        keywords.put("SAR", TokenType.T_INSTRUCTION);
        keywords.put("NEG", TokenType.T_INSTRUCTION);
        keywords.put("BT", TokenType.T_INSTRUCTION);
        keywords.put("CMP", TokenType.T_INSTRUCTION);
        keywords.put("AND", TokenType.T_INSTRUCTION);
        keywords.put("XOR", TokenType.T_INSTRUCTION);
        keywords.put("JB", TokenType.T_INSTRUCTION);
        keywords.put("JMP", TokenType.T_INSTRUCTION);

        keywords.put("DB", TokenType.T_DIRECTIVE);
        keywords.put("DW", TokenType.T_DIRECTIVE);
        keywords.put("DD", TokenType.T_DIRECTIVE);
        keywords.put("SEGMENT", TokenType.T_DIRECTIVE);
        keywords.put("ENDS", TokenType.T_DIRECTIVE);
        keywords.put("END", TokenType.T_DIRECTIVE);
        keywords.put("EQU", TokenType.T_DIRECTIVE);

        keywords.put("BYTE", TokenType.T_TYPE);
        keywords.put("WORD", TokenType.T_TYPE);
        keywords.put("DWORD", TokenType.T_TYPE);

        keywords.put("PTR", TokenType.T_PTR);
    }

    public Lexer() {
    }

    private char getNextChar(){
        try {
            return this.currentLine.charAt(currentCharIndex++);
        }  catch(IndexOutOfBoundsException e) {
            this.currentCharIndex = 0;
            return '\0';
        }
    }

    public TokenizedLine tokenizeString(String line, int number) {
        currentLine = line;
        currentLineIndex = number;
        List<Token> tokensInLine = new LinkedList<>();
        char currentChar = this.getNextChar();
        try {
            StringBuilder tokenContent = new StringBuilder();
            readLine:
            while (currentChar != '\0') {
                switch (currentChar) {
                    case ',', '+', '-', '*', '[', ']', '(', ')', '/' -> {
                        tokenContent = processPreviousToken(tokenContent, tokensInLine);
                        if (foundEqu) {
                            foundEqu = false;
                            break;
                        }
                        tokensInLine.add(new Token("" + currentChar, switch (currentChar) {
                            case ',' -> TokenType.T_COMMA;
                            case '+' -> TokenType.T_PLUS;
                            case '-' -> TokenType.T_MINUS;
                            case '*' -> TokenType.T_STAR;
                            case '/' -> TokenType.T_SLASH;
                            case '[' -> TokenType.T_OPEN_BRACKET;
                            case ']' -> TokenType.T_CLOSE_BRACKET;
                            case '(' -> TokenType.T_OPEN_PARENTHESIS;
                            case ')' -> TokenType.T_CLOSE_PARENTHESIS;
                            default -> throw new IllegalArgumentException();
                        }));
                    }
                    case ':' -> {
                        tokenContent = processPreviousToken(tokenContent, tokensInLine);
                        if (foundEqu) {
                            foundEqu = false;
                            break;
                        }
                        tokensInLine.add(new Token(":", TokenType.T_COLON));
                    }
                    case ' ', '\t' -> {
                        tokenContent = processPreviousToken(tokenContent, tokensInLine);
                        if (foundEqu) {
                            foundEqu = false;
                            break;
                        }
                        if (!tokensInLine.isEmpty() && tokensInLine.get(tokensInLine.size() - 1).content().equals("EQU")) {
                            currentChar = this.getNextChar();
                            while (currentChar != '\0' && currentChar != ';') {
                                tokenContent.append(currentChar);
                                currentChar = this.getNextChar();
                            }
                            tokensInLine.add(new Token(tokenContent.toString(), TokenType.T_EQU_SUB));
                            tokenContent = new StringBuilder();
                            continue;
                        }
                    }
                    case '\'', '\"' -> {
                        tokenContent = processPreviousToken(tokenContent, tokensInLine);
                        if (foundEqu) {
                            foundEqu = false;
                            break;
                        }
                        tokensInLine.add(this.tokenizeStringDeclaration(currentChar));
                    }
                    case ';' -> {
                        break readLine;
                    }
                    default -> {
                        tokenContent.append(currentChar);
                    }
                }
                currentChar = this.getNextChar();
                if (currentChar == '\0') {
                    Token token = this.processComplexToken(tokenContent.toString());
                    tokenContent = new StringBuilder();
                    if (foundEqu) {
                        currentChar = this.getNextChar();
                        foundEqu = false;
                        continue;
                    }
                    if (token != null) {
                        tokensInLine.add(token);
                    }
                }
            }
            currentCharIndex = 0;
        } catch (LexerException e) {
            currentCharIndex = 0;
            UtilTables.errors++;
            System.err.println(e.getMessage());
            return new TokenizedLine(tokensInLine, line, number).setException(e);
        }

        return new TokenizedLine(tokensInLine, line, number);
    }

    private StringBuilder processPreviousToken(StringBuilder tokenContent, List<Token> tokensInLine) throws LexerException {
        Token tmp = this.processComplexToken(tokenContent.toString());
        if(foundEqu) {
            return new StringBuilder();
        }
        if(tmp != null) {
            tokensInLine.add(tmp);
        }

        tokenContent = new StringBuilder();
        return tokenContent;
    }

    private Token tokenizeStringDeclaration(char starterQuote) throws LexerException {
        StringBuilder string = new StringBuilder("" + starterQuote);

        char character = this.getNextChar();
        while(character!= '\0' && character != starterQuote) {
             string.append(character);
            character = this.getNextChar();
        }
        if(character == '\0') {

            throw new LexerException(currentLineIndex, "Quotes are not closed");
        }
        else string.append(character);
        return new Token(string.toString(), TokenType.T_STRING);
    }

    private Token processComplexToken(String tokenContent) throws LexerException{
        tokenContent = tokenContent.toUpperCase();
        if(UtilTables.equReplacements.containsKey(tokenContent)) {
            if(currentCharIndex != 0){
                currentLine = currentLine.substring(0, currentCharIndex - tokenContent.length() - 1) + UtilTables.equReplacements.get(tokenContent) + currentLine.substring(currentCharIndex - 1);
                currentCharIndex = currentCharIndex - tokenContent.length() - 1;
            }
            else {
                currentLine = currentLine.substring(0, currentLine.length() - tokenContent.length()) + UtilTables.equReplacements.get(tokenContent);
                currentCharIndex = currentLine.length() - UtilTables.equReplacements.get(tokenContent).length();
            }
            foundEqu = true;

            return new Token(UtilTables.equReplacements.get(tokenContent), TokenType.T_EQU_SUB);
        }
        if(tokenContent.isEmpty()) return null;
        if(keywords.containsKey(tokenContent.toUpperCase())) return new Token(tokenContent, keywords.get(tokenContent.toUpperCase()));
        if(Character.isDigit(tokenContent.charAt(0))) {
            return this.parseImmValue(tokenContent);
        }
        for(char c : tokenContent.toCharArray()) {
            if(!Character.isDigit(c) && !((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')))
                throw new LexerException(currentLineIndex, "Unresolved symbol '" + c + "' in identifier token '" + tokenContent + "'");
        }
        if(tokenContent.length() > 5) throw new LexerException(currentLineIndex, "Identifier '" + tokenContent +"' is too large");
        return new Token(tokenContent, TokenType.T_IDENTIFIER);
    }

    private Token parseImmValue(String tokenContent) throws LexerException{
        int trim = 1;
        TokenType type = TokenType.T_DEC;
        int radix = switch (tokenContent.charAt(tokenContent.length() - 1)) {
            case 'B', 'b' -> {
                type = TokenType.T_BIN;
                yield 2;
            }
            case 'H', 'h' -> {
                type = TokenType.T_HEX;
                yield 16;
            }
            case 'D', 'd' -> 10;
            default -> {
                trim = 0;
                yield 10;
            }
        };
        try {
            int result = Integer.parseUnsignedInt(tokenContent.substring(0, tokenContent.length() - trim), radix);
            UtilTables.immPool.put(tokenContent, result);
            return new Token(tokenContent, type);

        } catch(NumberFormatException e){
            throw new LexerException(currentLineIndex, "unable to parse numeric token '" + tokenContent + "'");
        }
    }

}
