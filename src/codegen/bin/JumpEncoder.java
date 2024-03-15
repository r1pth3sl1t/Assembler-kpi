package codegen.bin;

import codegen.CodeGenException;
import codegen.ListingLine;
import parser.Sentence;
import parser.unit.operand.DirectMemReferenceOperand;
import util.UtilTables;

@SuppressWarnings("unused")
public class JumpEncoder {
    public static ListingLine allocateJMP(Sentence sentence, Integer offset) throws CodeGenException {
        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 1)
            throw new CodeGenException("JMP takes exactly 1 operand");
        if(!(sentence.getOperands().get(0) instanceof DirectMemReferenceOperand operand))
            throw new CodeGenException("JMP takes relative memory reference(label)");

        int allocated = 1;
        if(!UtilTables.identifiersTable.containsKey(operand.getName()))
            allocated += 4;
        else {
            if(UtilTables.identifiersTable.get(operand.getName()).getType() != UtilTables.IdentifierType.NEAR)
                throw new CodeGenException("label expected");
            int labelOffset = UtilTables.identifiersTable.get(operand.getName()).getOffset();
            if(labelOffset - (offset + 2) > 127 || labelOffset - (offset + 2) < -128){
                allocated += 4;
            }
            else allocated++;
        };

        ListingLine line = new ListingLine(sentence);
        line.setSizeAllocated(allocated);

        return line;
    }

    public static ListingLine allocateJB(Sentence sentence, Integer offset) throws CodeGenException {
        if(UtilTables.currentSegment == null)
            throw new CodeGenException("Instruction in null segment");
        if(sentence.getOperands().size() != 1)
            throw new CodeGenException("JMP takes exactly 1 operand");
        if(!(sentence.getOperands().get(0) instanceof DirectMemReferenceOperand operand))
            throw new CodeGenException("JMP takes relative memory reference(label)");

        int allocated = 2;
        if(!UtilTables.identifiersTable.containsKey(operand.getName()))
            allocated = 6;
        else {
            if(UtilTables.identifiersTable.get(operand.getName()).getType() != UtilTables.IdentifierType.NEAR)
                throw new CodeGenException("label expected");
            int labelOffset = UtilTables.identifiersTable.get(operand.getName()).getOffset();
            if(labelOffset - (offset + 2) > 127 || labelOffset - (offset + 2) < -128){
                allocated = 6;
            }
        };

        ListingLine line = new ListingLine(sentence);
        line.setSizeAllocated(allocated);

        return line;
    }

}
