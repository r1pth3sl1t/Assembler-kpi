package util;

import parser.unit.Identifier;

import java.util.HashMap;
import java.util.Map;

public class UtilTables {

    public static int errors = 0;

    public static String currentSegment;

    public enum IdentifierType {
        BYTE, WORD, DWORD, SEGMENT, NEAR, EQU
    }
    public static Map<String, Identifier> identifiersTable = new HashMap<>();

    public static Map<String, Identifier> segmentsTable = new HashMap<>();

    public static Map<String, String> equReplacements = new HashMap<>();

    public static Map<String, Integer> immPool = new HashMap<>();

    public static Map<String, String> segmentPurposesTable = new HashMap<>();

    public static boolean reset = false;
}
