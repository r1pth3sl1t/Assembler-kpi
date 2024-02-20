package parser;

public class ParserException extends Exception{
    public ParserException(String message){
        super(message);
    }

    public ParserException(int line, String message){
        super("[PARSER]<" + line + ">: " + message);
    }
}
