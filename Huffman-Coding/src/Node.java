import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node {
    private long frequency;
    private String code = "";
    Wrapper value;
    Node left;
    Node right;

    public Node(Wrapper value, Node left, Node right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public Node(Wrapper value, long frequency) {
        this.value = value;
        this.frequency = frequency;
    }

    public Node(long frequency, Node left, Node right) {
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }
}
