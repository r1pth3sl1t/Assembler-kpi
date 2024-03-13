package codegen;

public class CodeGenException extends Exception{

    public CodeGenException(String message) { super(message); }
    public CodeGenException(int line, String message){
        super("[CODEGEN]<" + line + ">: " + message);
    }
}
