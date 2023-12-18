import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node implements Comparable<Node> {
    private final int frequency;
    private Node leftNode;
    private Node rightNode;
    private Leaf leaf;

    public Node(Node leftNode, Node rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.frequency = (leftNode != null ? leftNode.getFrequency() : 0)
                + (rightNode != null ? rightNode.getFrequency() : 0);
    }

    @Override
    public int compareTo(Node node) {
        return Integer.compare(frequency, node.getFrequency());
    }
}