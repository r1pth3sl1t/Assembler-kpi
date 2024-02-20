package lexer;

public class LexerException extends Exception {

    public LexerException(String message) {
        super(message);
    }
    public LexerException(int line, String message) {
        super("[LEXER]<" + line + ">: " + message);
    }
}
