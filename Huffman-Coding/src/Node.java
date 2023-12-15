import lombok.Getter;

@Getter
public class Node implements Comparable<Node> {
    private final int frequency;
    private Node leftNode;
    private Node rightNode;

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