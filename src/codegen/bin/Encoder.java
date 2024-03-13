package codegen.bin;

import codegen.CodeGenException;
import codegen.ListingLine;
import parser.Sentence;
import parser.unit.operand.*;
import util.BinUtils;
import util.UtilTables;

@SuppressWarnings("unused")
public class Encoder {

    private static final byte MODRM_REG = 0B11;
    private static final byte MODRM_4B_DISP = 0B10;
    private static final byte  MODRM_1B_DISP = 0B01;
    private static final byte MODRM_NO_DISP = 0B00;


    private static ListingLine processVariableDeclaration(Sentence sentence, int bits) throws CodeGenException {
        if(sentence.getOperands().isEmpty())
            throw new CodeGenException("Operands missing");

        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Declaring variable out of segment");

        ListingLine line = new ListingLine(sentence);
        line.setSizeAllocated(sentence.getOperands().get(0).getSize());
        line.setHexCode(String.format(("%" + (bits >> 2) + "s"), sentence.getOperands().get(0).toString()));

        if(sentence.getIdentifier() != null) {
            sentence.getIdentifier().setSegment(UtilTables.currentSegment);
            if(UtilTables.identifiersTable.containsKey(sentence.getIdentifier().getName()))
                throw new CodeGenException("Identifier redefinition");
            UtilTables.identifiersTable.put(sentence.getIdentifier().getName(), sentence.getIdentifier());
        }

        return line;
    }

    public static ListingLine encodeDB(Sentence sentence) throws CodeGenException {
        return processVariableDeclaration(sentence, 8);
    }

    public static ListingLine encodeDW(Sentence sentence) throws CodeGenException {
        return processVariableDeclaration(sentence, 16);
    }

    public static ListingLine encodeDD(Sentence sentence) throws CodeGenException {
        return processVariableDeclaration(sentence, 32);
    }

    public static ListingLine encodeSEGMENT(Sentence sentence) throws CodeGenException {
        if(UtilTables.currentSegment != null)
            throw new CodeGenException("Cannot declare segment in already opened one");
        ListingLine line = new ListingLine(sentence);
        line.setSizeAllocated(0);
        if(sentence.getIdentifier() != null &&
                sentence.getIdentifier().getType() == UtilTables.IdentifierType.SEGMENT) {
            UtilTables.currentSegment = sentence.getIdentifier().getName();
            if(UtilTables.segmentsTable.isEmpty()) {
                UtilTables.segmentsTable.put(UtilTables.currentSegment, sentence.getIdentifier());
                UtilTables.segmentPurposesTable.put(UtilTables.currentSegment, "DS");
            }
            else if(UtilTables.segmentsTable.size() < 2) {
                UtilTables.segmentsTable.put(UtilTables.currentSegment, sentence.getIdentifier());
                UtilTables.segmentPurposesTable.put(UtilTables.currentSegment, "CS");
            }
        }
        UtilTables.reset = false;
        return line;
    }

    public static ListingLine encodeENDS(Sentence sentence) throws CodeGenException {
        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Cannot end declaration of null segment");

        if(sentence.getIdentifier() != null &&
                !UtilTables.currentSegment.equals(sentence.getIdentifier().getName())){
            throw new CodeGenException("Opened segment identifier doesn't match the identifier in ENDS statement");
        }

        ListingLine line = new ListingLine(sentence);
        UtilTables.currentSegment = null;
        line.setSizeAllocated(0);
        UtilTables.reset = true;
        return line;
    }

    public static ListingLine encodeEND(Sentence sentence){
        ListingLine line = new ListingLine(sentence);
        line.setSizeAllocated(0);
        return line;
    }

    public static ListingLine encodeSTOSB(Sentence sentence) throws CodeGenException {

        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(!sentence.getOperands().isEmpty())
            throw new CodeGenException("STOSB doesn't take any operands");
        ListingLine line = new ListingLine(sentence);
        line.setHexCode("AA");
        line.setSizeAllocated(1);
        return line;
    }

    public static ListingLine encodeSAR(Sentence sentence) throws CodeGenException {

        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 2)
            throw new CodeGenException("SAR takes exactly 2 operands");
        if(!(sentence.getOperands().get(0) instanceof RegisterOperand firstOperand))
            throw new CodeGenException("First operand must be register");

        if(!(sentence.getOperands().get(1) instanceof ImmOperand secondOperand))
            throw new CodeGenException("Second operand must be imm");
        if(secondOperand.getImmValue() != 1)
            throw new CodeGenException("Second operand must be 1");

        String hex = "";
        if(firstOperand.getSize() == 8)
            hex += "D0 ";
        else if(firstOperand.getSize() == 32)
            hex += "D1 ";

        hex += modRmBuilder(MODRM_REG,7, BinUtils.getRegisterCode(firstOperand.getName()));

        ListingLine listingLine = new ListingLine(sentence);
        listingLine.setHexCode(hex);
        listingLine.setSizeAllocated(2);
        return listingLine;

    }
    public static ListingLine encodeNEG(Sentence sentence) throws CodeGenException {

        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 1)
            throw new CodeGenException("NEG takes exactly 1 operand");

        if(!(sentence.getOperands().get(0) instanceof DirectMemReferenceOperand)
        && !(sentence.getOperands().get(0) instanceof MemReferenceOperand))
            throw new CodeGenException("NEG takes only mem operand");

        String hex = "";

        HexMemory mem = resolveMemoryOperand(sentence.getOperands().get(0), 3);
        int allocated = mem.size;
        String segmentPrefix = mem.segmentPrefix;
        String modrm = mem.modRmSib;

        if(!segmentPrefix.isEmpty()) hex += segmentPrefix + ": ";

        if(sentence.getOperands().get(0).getSize() == 8) {
            hex += "F6 ";
        }
        else {
            hex += "F7 ";
        }
        allocated++;
        hex += modrm;

        ListingLine listingLine = new ListingLine(sentence);
        listingLine.setHexCode(hex);
        listingLine.setSizeAllocated(allocated);

        return listingLine;
    }

    public static ListingLine encodeBT(Sentence sentence) throws CodeGenException {
        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 2)
            throw new CodeGenException("BT takes exactly 2 operands");
        if(!(sentence.getOperands().get(0) instanceof RegisterOperand dest))
            throw new CodeGenException("Dest operand must be a register");
        if(!(sentence.getOperands().get(1) instanceof RegisterOperand src))
            throw new CodeGenException("Src operand must be a register");
        if(dest.getSize() != src.getSize() || dest.getSize() != 32 || src.getSize() != 32)
            throw new CodeGenException("Types mismatch");

        String hex = "0F A3 ";
        hex += modRmBuilder(MODRM_REG, BinUtils.getRegisterCode(src.getName()), BinUtils.getRegisterCode(dest.getName()));
        ListingLine listingLine = new ListingLine(sentence);
        listingLine.setHexCode(hex);
        listingLine.setSizeAllocated(3);
        return listingLine;
    }

    public static ListingLine encodeCMP(Sentence sentence) throws CodeGenException {
        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 2)
            throw new CodeGenException("CMP takes exactly 2 operands");
        if(!(sentence.getOperands().get(0) instanceof RegisterOperand dest))
            throw new CodeGenException("Dest operand must be a register");
        if(!((sentence.getOperands().get(1) instanceof DirectMemReferenceOperand) ||
                (sentence.getOperands().get(1) instanceof MemReferenceOperand)))
            throw new CodeGenException("Src operand must be a memory reference");

        String segmentPrefix = "";
        String modrm = "";
        int memSize = 0;
        int allocated = 0;

        HexMemory mem = resolveMemoryOperand(sentence.getOperands().get(1), BinUtils.getRegisterCode(dest.getName()));
        segmentPrefix = mem.segmentPrefix;
        modrm = mem.modRmSib;
        allocated = mem.size;
        memSize = mem.memOpSize;

        String hex = "";
        if(!segmentPrefix.isEmpty()){
            hex += segmentPrefix + ": ";
        }

        if(memSize == 0) memSize = RegisterOperand.getRegisterSize(dest.getName());

        if(memSize != dest.getSize())
            throw new CodeGenException("Types mismatch");

        if(memSize == 8)
            hex += "3A ";
        else
            hex += "3B ";

        allocated++;

        hex += modrm;

        ListingLine listingLine = new ListingLine(sentence);
        listingLine.setHexCode(hex);
        listingLine.setSizeAllocated(allocated);

        return listingLine;
    }

    public static ListingLine encodeAND(Sentence sentence) throws CodeGenException {

        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 2)
            throw new CodeGenException("AND takes exactly 2 operands");
        if(!(sentence.getOperands().get(1) instanceof RegisterOperand src))
            throw new CodeGenException("Src operand must be a register");
        if(!((sentence.getOperands().get(0) instanceof DirectMemReferenceOperand) ||
                (sentence.getOperands().get(0) instanceof MemReferenceOperand)))
            throw new CodeGenException("Dest operand must be a memory reference");

        HexMemory hexMemory = resolveMemoryOperand(sentence.getOperands().get(0), BinUtils.getRegisterCode(src.getName()));

        int allocated = hexMemory.size;
        String segmentPrefix = hexMemory.segmentPrefix;
        String modrm = hexMemory.modRmSib;
        int memSize = hexMemory.memOpSize;

        if(memSize == 0) memSize = src.getSize();
        if(memSize != src.getSize())
            throw new CodeGenException("Types mismatch");

        String hex = "";
        if(!segmentPrefix.isEmpty()) hex += segmentPrefix + ": ";

        if(memSize == 8)
            hex += "20 ";
        else hex += "21 ";
        allocated++;

        hex += modrm;

        ListingLine line = new ListingLine(sentence);
        line.setHexCode(hex);
        line.setSizeAllocated(allocated);

        return line;
    }
    public static ListingLine encodeXOR(Sentence sentence) throws CodeGenException {
        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 2)
            throw new CodeGenException("XOR takes exactly 2 operands");
        if(!(sentence.getOperands().get(1) instanceof ImmOperand src))
            throw new CodeGenException("Src operand must be imm");
        if(!((sentence.getOperands().get(0) instanceof DirectMemReferenceOperand) ||
                (sentence.getOperands().get(0) instanceof MemReferenceOperand)))
            throw new CodeGenException("Dest operand must be a memory reference");

        HexMemory memory = resolveMemoryOperand(sentence.getOperands().get(0), 6);
        String hex = "";
        if(!memory.segmentPrefix.isEmpty())
            hex += memory.segmentPrefix + ": ";

        if(memory.memOpSize == 0) memory.memOpSize = 32;

        if(memory.memOpSize < src.getSize())
            throw new CodeGenException("Types mismatch");

        if(memory.memOpSize == 32 && src.getSize() << 3 == 8)
            hex += "83 ";
        else if(memory.memOpSize == 32 && src.getSize() << 3 == 32)
            hex += "81 ";
        else
            hex += "80 ";

        memory.size++;

        hex += memory.modRmSib + " ";
        hex += src.toString();
        memory.size += src.getSize();
        ListingLine line = new ListingLine(sentence);
        line.setHexCode(hex);
        line.setSizeAllocated(memory.size);
        return line;
    }

    public static ListingLine encodeEQU(Sentence sentence){
        ListingLine line = new ListingLine(sentence);
        line.setSizeAllocated(0);
        if(!sentence.getOperands().isEmpty()) line.setHexCode(sentence.getOperands().get(0).getName());
        return line;
    }

    private static String modRmBuilder(int mod, int reg, int rm){
        byte modrm = 0;
        modrm |= (byte) ((mod & 0b11) << 6);
        modrm |= (byte) ((reg & 0b111) << 3);
        modrm |= (byte) (rm & 0b111);
        return String.format("%2s", Integer.toHexString(modrm & 0xff).toUpperCase()).replace(' ', '0');
    }

    private static String sibBuilder(int mul, int index, int base){
      return modRmBuilder(mul, index, base);
    }

    private static HexMemory resolveMemoryOperand(Operand operand, int reg) throws CodeGenException {
        String segmentPrefix = "";
        String modrm = "";
        int memSize = 0;
        int allocated = 0;

        if(operand instanceof DirectMemReferenceOperand direct) {
            if(!UtilTables.identifiersTable.containsKey(direct.getName()))
                throw new CodeGenException("Identifier not declared");
            if(direct.getSegmentOverride().isEmpty()) {
                String idSegment = UtilTables.identifiersTable.get(direct.getName()).getSegment();
                segmentPrefix = UtilTables.segmentPurposesTable.get(idSegment);

                if(segmentPrefix.equals("DS")) segmentPrefix = "";
                else segmentPrefix = BinUtils.getSegmentPrefix(segmentPrefix);
            }
            else if (direct.getSegmentOverride().equals("DS")) segmentPrefix = "";
            else segmentPrefix = BinUtils.getSegmentPrefix(direct.getSegmentOverride());
            modrm = modRmBuilder(MODRM_NO_DISP, reg, 0b101);
            allocated++;
            if(direct.getSize() != 0) memSize = direct.getSize();
            else memSize = switch(UtilTables.identifiersTable.get(direct.getName()).getType()) {
                case BYTE -> 8;
                case DWORD -> 32;
                default -> throw new CodeGenException("Invalid operand size: only 8- and 32-bit operands allowed");
            };
            modrm += " " + String.format("%8s", Integer.toHexString(UtilTables.identifiersTable.get(direct.getName()).getOffset())).replace(' ', '0');
            allocated += 4;
        }
        else if(operand instanceof MemReferenceOperand indirect){
            if(!indirect.getSegmentOverride().isEmpty())
                segmentPrefix = BinUtils.getSegmentPrefix(indirect.getSegmentOverride());

            if((indirect.getBase().equals("EBP") || indirect.getBase().equals("ESP")) &&
                indirect.getSegmentOverride().equals("SS")) {
                segmentPrefix = "";
            }
            int mod = ImmOperand.getSignedIntegerSize(indirect.getDisplacement()) == 8 ? MODRM_1B_DISP : MODRM_4B_DISP;
            String format = mod == MODRM_1B_DISP ? "%2s" : "%8s";
            modrm = modRmBuilder(mod, reg, 0b100);
            allocated++;

            modrm += " " + sibBuilder(0, BinUtils.getRegisterCode(indirect.getIndex()), BinUtils.getRegisterCode(indirect.getBase()));
            allocated++;
            modrm += " " + String.format(format, Integer.toHexString(indirect.getDisplacement() & (mod == MODRM_1B_DISP ? 0xff : 0xffffffff))).toUpperCase().replace(' ', '0');
            allocated += mod == MODRM_1B_DISP ? 1 : 4;
            if(indirect.getSize() != 0) memSize = indirect.getSize();
        }
        if(!segmentPrefix.isEmpty()) allocated++;
        HexMemory tmp = new HexMemory();
        tmp.modRmSib = modrm;
        tmp.segmentPrefix = segmentPrefix;
        tmp.size = allocated;
        tmp.memOpSize = memSize;
        return tmp;
    }

    private static class HexMemory {
        String segmentPrefix;
        String modRmSib;
        int size;

        int memOpSize;
    }

}
