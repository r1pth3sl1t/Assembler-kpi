package util;

import java.util.HashMap;
import java.util.Map;

public class BinUtils {
    private static final Map<String, Integer> registerCodes = new HashMap<>();
    private static final Map<String, String> segmentRegisterPrefixes = new HashMap<>();

    static {
        registerCodes.put("EAX", 0B000);
        registerCodes.put("ECX", 0B001);
        registerCodes.put("EDX", 0B010);
        registerCodes.put("EBX", 0B011);
        registerCodes.put("ESP", 0B100);
        registerCodes.put("EBP", 0B101);
        registerCodes.put("ESI", 0B110);
        registerCodes.put("EDI", 0B111);

        registerCodes.put("AL", 0B000);
        registerCodes.put("CL", 0B001);
        registerCodes.put("DL", 0B010);
        registerCodes.put("BL", 0B011);
        registerCodes.put("AH", 0B100);
        registerCodes.put("CH", 0B101);
        registerCodes.put("DH", 0B110);
        registerCodes.put("BH", 0B111);

        segmentRegisterPrefixes.put("ES",  "26");
        segmentRegisterPrefixes.put("CS",  "2E");
        segmentRegisterPrefixes.put("SS",  "36");
        segmentRegisterPrefixes.put("DS",  "3E");
        segmentRegisterPrefixes.put("FS",  "64");
        segmentRegisterPrefixes.put("GS",  "65");

    }

    public static int getRegisterCode(String register){
        return registerCodes.get(register);
    }

    public static String getSegmentPrefix(String segmentRegister){
        return segmentRegisterPrefixes.get(segmentRegister);
    }
}
