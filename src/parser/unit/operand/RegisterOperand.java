package parser.unit.operand;

import lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class RegisterOperand extends Operand{

    private static final Map<String, Integer> registerSizeMap;

    static {
        registerSizeMap = new HashMap<>();
        registerSizeMap.put("AL", 8);
        registerSizeMap.put("BL", 8);
        registerSizeMap.put("CL", 8);
        registerSizeMap.put("DL", 8);
        registerSizeMap.put("AH", 8);
        registerSizeMap.put("BH", 8);
        registerSizeMap.put("CH", 8);
        registerSizeMap.put("DH", 8);

        registerSizeMap.put("EAX", 32);
        registerSizeMap.put("EBX", 32);
        registerSizeMap.put("ECX", 32);
        registerSizeMap.put("EDX", 32);
        registerSizeMap.put("EBP", 32);
        registerSizeMap.put("ESP", 32);
        registerSizeMap.put("EDI", 32);
        registerSizeMap.put("ESI", 32);
    }

    public RegisterOperand(Token token) {
        super(token);
        this.size = getRegisterSize(token.content());
    }

    public String toString(){
        return name;
    }

    public static int getRegisterSize(String register){
        if(register.endsWith("S")) return 16;
        return registerSizeMap.get(register);
    }
}
