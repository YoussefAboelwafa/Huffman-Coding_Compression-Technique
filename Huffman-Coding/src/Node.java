public class Node {
    private long frequency;
    private String code = "";
    BAW value;
    Node left;
    Node right;

    public Node() {
    }

    public Node(BAW value, Node left, Node right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public Node(BAW val, long frequency) {
        this.value = val;
        this.frequency = frequency;
    }

    public Node(long frequency, Node left, Node right) {
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public long getFrequency() {
        return this.frequency;
    }
}
