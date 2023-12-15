import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {
    @Getter
    private Map<Character, String> huffmanCodes = new HashMap<>();
    private Map<Character, Integer> charFrequencies = new HashMap<>();
    private PriorityQueue<Node> queue = new PriorityQueue<>();
    private String text;

    public Huffman(File file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        this.text = new String(fileBytes, StandardCharsets.UTF_8);
        System.out.println("Huffman Text Size: \u001B[33m" + text.length() + " bytes\u001B[0m");
        fillCharFrequenciesMap();

        for (char character : charFrequencies.keySet()) {
            System.out.println(character + " => " + charFrequencies.get(character));
        }
        fillQueue();
        buildTreeAndGenerateCodes();
        for (char character : huffmanCodes.keySet()) {
            System.out.println(character + " => " + huffmanCodes.get(character));
        }
    }

    private void fillCharFrequenciesMap() {
        for (char character : text.toCharArray()) {
            charFrequencies.put(character, charFrequencies.getOrDefault(character, 0) + 1);
        }
    }

    private void fillQueue() {
        for (Map.Entry<Character, Integer> entry : charFrequencies.entrySet()) {
            queue.add(new Leaf(entry.getKey(), entry.getValue()));
        }
    }

    public void buildTreeAndGenerateCodes() {
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node parent = new Node(left, right);
            queue.add(parent);
        }

        generateCodes(queue.peek(), "");
    }

    private void generateCodes(Node node, String code) {
        if (node instanceof Leaf leaf) {
            huffmanCodes.put(leaf.getCharacter(), code);
        } else {
            generateCodes(node.getLeftNode(), code + "0");
            generateCodes(node.getRightNode(), code + "1");
        }
    }
}