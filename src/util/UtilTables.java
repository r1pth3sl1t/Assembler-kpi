package util;

import parser.unit.Identifier;

import java.util.HashMap;
import java.util.Map;

public class UtilTables {

    public enum IdentifierType {
        BYTE, WORD, DWORD, SEGMENT, LABEL
    }
    public static Map<String, Identifier> identifiersTable = new HashMap<>();

    public static Map<String, String> equReplacements = new HashMap<>();

    public static Map<String, Integer> immPool = new HashMap<>();
}
