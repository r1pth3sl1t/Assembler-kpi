package codegen;

import parser.Sentence;

public class ListingLine {
    private final Sentence sentence;

    private int sizeAllocated;

    private int offset;

    private String hexCode = "";

    private CodeGenException exception;

    public ListingLine(Sentence sentence){
        this.sentence = sentence;
    }

    public int getSizeAllocated() {
        return sizeAllocated;
    }

    public void setSizeAllocated(int sizeAllocated) {
        this.sizeAllocated = sizeAllocated;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }


    public void setException(CodeGenException exception) {
        this.exception = exception;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        if(sentence.hasErrors()) {
            result.append(sentence.getErrorMessages()).append('\n');
        }
        else if(exception != null)
            result.append(exception.getMessage()).append('\n');
        result.append(String.format("%2s", sentence.getTokenizedLine().getLineNumber())).append(") ");
        if((sentence.getMnemonic() != null && !sentence.getMnemonic().getName().equals("EQU")) ||
                sentence.getIdentifier() != null && sentence.getMnemonic() == null)
        result.append(String.format("%8s", Integer.toHexString(offset).toUpperCase()).replace(' ', '0'));
        else
            result.append("        ");
        result.append("  ");
        result.append(String.format("%26s", hexCode).toUpperCase());
        result.append("  ");
        result.append(sentence.getTokenizedLine().getInitialLine());
        return result.toString();
    }

    public String firstRunString(){
        StringBuilder result = new StringBuilder();
        if(sentence.hasErrors()) {
            result.append(sentence.getErrorMessages()).append('\n');
        }
        else if(exception != null)
            result.append(exception.getMessage()).append('\n');
        result.append(String.format("%2s", sentence.getTokenizedLine().getLineNumber())).append(". ");
        if((sentence.getMnemonic() != null && !sentence.getMnemonic().getName().equals("EQU")) ||
                sentence.getIdentifier() != null && sentence.getMnemonic() == null)
            result.append(String.format("%8s", Integer.toHexString(offset).toUpperCase()).replace(' ', '0'));
        else
            result.append("        ");
        result.append("  ");
        result.append(String.format("%9s", "[" + sizeAllocated + "]"));
        result.append("  ");
        result.append(sentence.getTokenizedLine().getInitialLine());
        return result.toString();
    }
}
