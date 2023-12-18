import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Leaf extends Node {
    private final String string;
    private final int frequency;
    public Leaf(String string, int frequency) {
        super(null, null);
        this.string = string;
        this.frequency = frequency;
    }
}