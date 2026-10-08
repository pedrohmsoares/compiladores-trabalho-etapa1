package lexico;

public class Real extends Token {
    public final float value;

    public Real(float value) {
        super(Tag.FLOAT_CONST);
        this.value = value;
    }

    public String toString() {
        return "" + value;
    }
}
