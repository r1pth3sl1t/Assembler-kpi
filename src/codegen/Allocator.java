package codegen;

import codegen.bin.Encoder;
import codegen.bin.JumpEncoder;
import parser.Sentence;
import util.UtilTables;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Allocator {

    private int offset;

    private final List<Sentence> sentences;

    private final List<ListingLine> listingLines;

    public Allocator(List<Sentence> sentences){
        this.sentences = sentences;
        listingLines = new LinkedList<>();
    }

    public void allocate()  {
        for(Sentence sentence : sentences) {
            if(sentence == null) continue;
            ListingLine listingLine = null;
            if(sentence.hasErrors()) {
                listingLine = new ListingLine(sentence);
                listingLine.setSizeAllocated(0);
                listingLine.setOffset(offset);
                listingLines.add(listingLine);
                continue;
            }
            if(sentence.getIdentifier() != null) {
                try {
                    sentence.getIdentifier().setOffset(offset);
                    if (sentence.getIdentifier().getType() == UtilTables.IdentifierType.NEAR) {
                        UtilTables.identifiersTable.put(sentence.getIdentifier().getName(), sentence.getIdentifier());
                        if (UtilTables.currentSegment == null)
                            throw new CodeGenException(sentence.getTokenizedLine().getLineNumber(),"Label out of segment");
                        sentence.getIdentifier().setSegment(UtilTables.currentSegment);
                    }
                } catch (CodeGenException e) {
                    listingLine = new ListingLine(sentence);
                    listingLine.setSizeAllocated(0);
                    UtilTables.errors++;
                    System.err.println(e.getMessage());
                    listingLine.setException(e);
                    continue;
                }
            }
            if(sentence.getMnemonic() != null) {
                try {
                    Method binaryConvertingMethod;
                    if(sentence.getMnemonic().getName().equals("JB") ||
                            sentence.getMnemonic().getName().equals("JMP")) {
                        binaryConvertingMethod = JumpEncoder.class.
                                getMethod("allocate" + sentence.getMnemonic().getName(), Sentence.class, Integer.class);
                        listingLine = (ListingLine) binaryConvertingMethod.invoke(null, sentence, offset);
                    }
                    else {
                        binaryConvertingMethod = Encoder.class.
                                getMethod("encode" + sentence.getMnemonic().getName(), Sentence.class);
                        listingLine = (ListingLine) binaryConvertingMethod.invoke(null, sentence);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    listingLine = new ListingLine(sentence);
                    listingLine.setSizeAllocated(0);
                    UtilTables.errors++;
                    CodeGenException codeGenException = new CodeGenException(sentence.getTokenizedLine().getLineNumber(), e.getCause().getMessage());
                    System.err.println(codeGenException.getMessage());
                    listingLine.setException(codeGenException);
                }
            }
            if(listingLine == null) listingLine = new ListingLine(sentence);
            listingLine.setOffset(offset);
            this.offset += listingLine.getSizeAllocated();
            listingLines.add(listingLine);
            if(UtilTables.reset) offset = 0;
        }
    }

    public void firstRunListing(PrintWriter writer){

        for (ListingLine listingLine : listingLines) {
            writer.println(listingLine.toString());
        }
        writer.println("\n\nIdentifiers table: ");
        writer.println("      Name |    Type | Value");
        for(String identifier : UtilTables.identifiersTable.keySet()) {
            writer.printf("%10s", identifier);
            writer.print(" ");
            if(UtilTables.identifiersTable.get(identifier) != null) writer.printf("%9s ",UtilTables.identifiersTable.get(identifier).getType());
            String hexOffset = String.format("%8s", Integer.toHexString(UtilTables.identifiersTable.get(identifier).getOffset())).replace(' ', '0');
            writer.println("  " + UtilTables.identifiersTable.get(identifier).getSegment() + ":" + hexOffset);
        }
        writer.println("Segments table");
        writer.println("      Name |");
        for(String segment : UtilTables.segmentPurposesTable.keySet()) {
            writer.printf("%10s", segment);
            writer.println();
        }
    }

}
