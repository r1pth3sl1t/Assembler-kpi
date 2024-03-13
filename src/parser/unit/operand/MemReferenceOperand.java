package parser.unit.operand;

public class MemReferenceOperand extends Operand{

    private String base;

    private String index;

    private int displacement;

    private String segmentOverride = "";

    public MemReferenceOperand(){
        super();
    }


    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getDisplacement() {
        return displacement;
    }

    public void setDisplacement(int displacement) {
        this.displacement = displacement;
    }

    public String getSegmentOverride() {
        return segmentOverride;
    }

    public void setSegmentOverride(String segmentOverride) {
        this.segmentOverride = segmentOverride;
    }

    public void setName() {
        this.name = segmentOverride + ": " + size +" ptr [" + base + " + " + index + "+" + displacement + "]";
    }

    @Override
    public String toString(){
        return name;
    }

}
