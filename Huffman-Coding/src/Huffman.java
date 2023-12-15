import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;


public class Huffman {
    @Getter
    private Map<Character, String> huffman_codes = new HashMap<>();
    private Map<Character, Integer> char_frequencies = new HashMap<>();
    private PriorityQueue<Node> queue = new PriorityQueue<>();
    private byte[] fileBytes;


    public Huffman(File file, int bytes) throws IOException {

        fileBytes = Files.readAllBytes(file.toPath());
        System.out.println("Number of file bytes: \u001B[33m" + fileBytes.length + " bytes\u001B[0m");

        fill_char_frequencies_map();
        fill_queue();
        build_tree();
        generate_codes(queue.peek(), "");
    }

    private void fill_char_frequencies_map() {
        for (byte fileByte : fileBytes) {
            char character = (char) fileByte;
            if (char_frequencies.containsKey(character)) {
                char_frequencies.put(character, char_frequencies.get(character) + 1);
            } else {
                char_frequencies.put(character, 1);
            }
        }
    }

    private void fill_queue() {
        for (Map.Entry<Character, Integer> entry : char_frequencies.entrySet()) {
            queue.add(new Leaf(entry.getKey(), entry.getValue()));
        }
    }

    public void build_tree() {
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node parent = new Node(left, right);
            queue.add(parent);
        }
    }

    public void generate_codes(Node node, String code) {
        if (node instanceof Leaf leaf) {
            huffman_codes.put(leaf.getCharacter(), code);
        } else {
            generate_codes(node.getLeftNode(), code + "0");
            generate_codes(node.getRightNode(), code + "1");
        }
    }
}