import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;


public class Huffman {
    @Getter
    private Map<String, String> huffman_codes = new HashMap<>();
    private final Map<String, Integer> frequencies = new HashMap<>();
    private final PriorityQueue<Node> queue = new PriorityQueue<>();
    private final int bytes;

    public Huffman(File file, int bytes, int chunk_size) throws IOException {
        this.bytes = bytes;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] chunk = new byte[chunk_size];
            int bytes_read;
            while ((bytes_read = bis.read(chunk)) != -1) {
                process_chunk(chunk, bytes_read);
            }
        }
    }

    private void process_chunk(byte[] chunk, int bytesRead) {
        ByteBuffer wrapped = ByteBuffer.wrap(chunk);
        for (int i = 0; i < bytesRead; i += bytes) {
            byte[] byte_arr = new byte[bytes];
            wrapped.get(byte_arr, 0, bytes);
            String key = new String(byte_arr);
            frequencies.put(key, frequencies.getOrDefault(key, 0) + 1);
        }
        fill_queue();
        build_tree();
        generate_codes(queue.peek(), "");

        System.out.println("Huffman Codes:");
        for (Map.Entry<String, String> entry : huffman_codes.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }

    }

    private void fill_queue() {
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            queue.add(new Leaf(entry.getKey(), entry.getValue()));
        }
    }

    private void build_tree() {
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node parent = new Node(left, right);
            queue.add(parent);
        }
    }

    private void generate_codes(Node node, String code) {
        if (node instanceof Leaf leaf) {
            huffman_codes.put(leaf.getString(), code);
        } else {
            generate_codes(node.getLeftNode(), code + "0");
            generate_codes(node.getRightNode(), code + "1");
        }
    }

    public long get_compressed_size() {
        return 0;
    }

    public double get_compression_ratio() {
        return 0;
    }

    public String encode() {
        return null;
    }

}