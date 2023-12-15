import lombok.Getter;

@Getter
public class Leaf extends Node {
    private final char character;
    private final int frequency;

    public Leaf(char character, int frequency) {
        super(null, null);
        this.character = character;
        this.frequency = frequency;
    }
}