import lombok.Getter;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


public class Compression {
    @Getter
    private Map<String, BitSet> huffman_codes = new HashMap<>();
    private final Map<String, Integer> frequencies = new HashMap<>();
    private final PriorityQueue<Node> queue = new PriorityQueue<>();
    private final int bytes;

    public Compression(File file, int bytes, int chunk_size) throws IOException {
        this.bytes = bytes;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] chunk = new byte[chunk_size];
            int bytes_read;
            while ((bytes_read = bis.read(chunk)) != -1) {
                process_chunk(chunk, bytes_read);
            }
        }
        fill_queue();
        build_tree();
        generate_codes(queue.peek(), new BitSet());

        System.out.println("Compression Codes:");
        for (Map.Entry<String, BitSet> entry : huffman_codes.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
        encode(file);
    }

    private void process_chunk(byte[] chunk, int bytesRead) {
        ByteBuffer wrapped = ByteBuffer.wrap(chunk);
        for (int i = 0; i < bytesRead; i += bytes) {
            if (bytesRead - i >= bytes) {
                byte[] byte_arr = new byte[bytes];
                wrapped.get(byte_arr, 0, bytes);
                String key = new String(byte_arr);
                frequencies.put(key, frequencies.getOrDefault(key, 0) + 1);
            }
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

    private void generate_codes(Node node, BitSet code) {
        if (node instanceof Leaf leaf) {
            huffman_codes.put(leaf.getString(), (BitSet) code.clone());
        } else {
            BitSet leftCode = (BitSet) code.clone();
            leftCode.set(code.length(), false);
            generate_codes(node.getLeftNode(), leftCode);

            BitSet rightCode = (BitSet) code.clone();
            rightCode.set(code.length(), true);
            generate_codes(node.getRightNode(), rightCode);
        }
    }

    private void encode(File inputFile) throws IOException {
        String outputFileName = "20012263." + bytes + "." + inputFile.getName() + ".hc";
        File outputFile = new File(inputFile.getParent(), outputFileName);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
             FileOutputStream fos = new FileOutputStream(outputFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            // Write the Huffman codes to the file
            oos.writeObject(huffman_codes);

            byte[] chunk = new byte[bytes];
            int bytesRead;
            while ((bytesRead = bis.read(chunk)) != -1) {
                if (bytesRead == bytes) {
                    String key = new String(chunk);
                    BitSet huffmanCode = huffman_codes.get(key);
                    if (huffmanCode != null) {
                        byte[] encodedChunk = huffmanCode.toByteArray();
                        fos.write(encodedChunk);
                    } else {
                        throw new IOException("No Huffman code for input chunk: " + key);
                    }
                }
            }
        }
    }


    public long get_compressed_size() {
        return 0;
    }

    public double get_compression_ratio() {
        return 0;
    }

}