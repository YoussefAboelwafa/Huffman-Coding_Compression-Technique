import lombok.Getter;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


public class Compression {
    @Getter
    private Map<String, boolean[]> huffman_codes = new HashMap<>();
    private final Map<String, Integer> frequencies = new HashMap<>();
    private final PriorityQueue<Node> queue = new PriorityQueue<>();
    private final int bytes;
    private long original_size = 0;
    File outputFile;

    public Compression(File file, int bytes, int chunk_size) throws IOException {
        this.bytes = bytes;
        this.original_size = file.length();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] chunk = new byte[chunk_size];
            int bytes_read;
            while ((bytes_read = bis.read(chunk)) != -1) {
                process_chunk(chunk, bytes_read);
            }
        }
        fill_queue();
        build_tree();
        generate_codes(queue.peek(), new boolean[0], 0);
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

    private void generate_codes(Node node, boolean[] code, int length) {
        boolean[] currentCode = Arrays.copyOf(code, length);

        if (node instanceof Leaf leaf) {
            huffman_codes.put(leaf.getString(), currentCode);
        } else {
            boolean[] leftCode = Arrays.copyOf(currentCode, length + 1);
            leftCode[length] = false;
            generate_codes(node.getLeftNode(), leftCode, length + 1);

            boolean[] rightCode = Arrays.copyOf(currentCode, length + 1);
            rightCode[length] = true;
            generate_codes(node.getRightNode(), rightCode, length + 1);
        }
    }


    private void encode(File inputFile) throws IOException {
        String outputFileName = "20012263." + bytes + "." + inputFile.getName() + ".hc";
        outputFile = new File(inputFile.getParent(), outputFileName);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
             FileOutputStream fos = new FileOutputStream(outputFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(huffman_codes);
            byte[] chunk = new byte[bytes];
            int bytesRead;
            while ((bytesRead = bis.read(chunk)) != -1) {
                if (bytesRead == bytes) {
                    String key = new String(chunk);
                    boolean[] huffmanCode = huffman_codes.get(key);
                    if (huffmanCode != null) {
                        byte[] encodedChunk = new byte[(huffmanCode.length + 7) / 8];
                        fos.write(encodedChunk);
                    } else {
                        throw new IOException("No Huffman code for input chunk: " + key);
                    }
                }
            }
        }
    }


    public long get_compressed_size() {
        return outputFile.length();
    }

    public double get_compression_ratio() {
        return (double) get_compressed_size() / original_size;
    }

}