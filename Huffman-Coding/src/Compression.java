import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

public class Compression {
    private int index;
    private int size;
    private int number_ones = 0;
    private long original_size = 0;
    private long compressed_size = 0;
    HashMap<Wrapper, Long> frequencies = new HashMap<>();
    HashMap<Wrapper, String> huffman_codes = new HashMap<>();
    PriorityQueue<Node> queue;

    // Main method for compressing a file
    public void compress(File file, int n) throws IOException {

        // Read bytes from file and calculate frequencies
        this.frequencies = build_frequencies(file, n);

        // Build Huffman tree
        this.queue = new PriorityQueue<>(frequencies.size(), Comparator.comparingLong(Node::getFrequency));
        build_tree(frequencies, queue);

        // Build Huffman codes
        Node root = queue.peek();
        assert root != null;
        if (root.left == null && root.right == null) {
            this.huffman_codes.put(root.value, "0");
        } else {
            build_huffman_codes(this.huffman_codes, root, "");
        }

        // Compress file using Huffman codes
        compress_file(file, this.huffman_codes, make_dictionary(root, n), n);
    }

    // Read bytes from file and calculate frequencies
    private HashMap<Wrapper, Long> build_frequencies(File file, int n) throws IOException {
        HashMap<Wrapper, Long> frequencies = new HashMap<>();
        byte[] data = new byte[n * 1024];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 1024 * 1024 * 10)) {
            int bytes_read;
            while ((bytes_read = bis.read(data)) != -1) {
                for (int i = 0; i < bytes_read; i += n) {
                    int length = Math.min(n, bytes_read - i);
                    byte[] temp = new byte[length];
                    System.arraycopy(data, i, temp, 0, length);
                    Wrapper wrapper = new Wrapper(temp);
                    frequencies.put(wrapper, frequencies.getOrDefault(wrapper, 0L) + 1);
                }
            }
        }
        return frequencies;
    }

    // Build Huffman tree
    private void build_tree(HashMap<Wrapper, Long> frequencies, PriorityQueue<Node> queue) {
        for (Wrapper key : frequencies.keySet()) {
            queue.add(new Node(key, frequencies.get(key)));
        }

        while (queue.size() > 1) {
            Node first = queue.poll();
            Node second = queue.poll();

            assert first != null;
            assert second != null;

            long frequencies_sum = first.getFrequency() + second.getFrequency();
            first.setCode("0");
            second.setCode("1");

            Node current = new Node(frequencies_sum, first, second);
            queue.add(current);
        }
    }

    // Fill Huffman codes
    private void build_huffman_codes(HashMap<Wrapper, String> huffman_codes, Node root, String s) {
        Stack<Node> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.left == null && node.right == null) {
                huffman_codes.put(node.value, node.getCode());
            } else {
                if (node.right != null) {
                    node.right.setCode(node.getCode() + "1");
                    stack.push(node.right);
                }
                if (node.left != null) {
                    node.left.setCode(node.getCode() + "0");
                    stack.push(node.left);
                }
            }
        }
    }

    // Compress file using Huffman codes
    private void compress_file(File file, HashMap<Wrapper, String> huffman_codes, byte[] dictionary, int n) throws IOException {
        this.original_size = file.length();
        String old_file = file.getName();
        String compressed_file = file.getAbsolutePath().replace(old_file, "20012263." + n + "." + old_file + ".hc");
        FileOutputStream fos = new FileOutputStream(compressed_file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(dictionary);
        write_file(file, bos, n, huffman_codes);
        this.compressed_size = new File(compressed_file).length();
    }

    // Get compression size
    public long get_compressed_size() {
        return this.compressed_size;
    }

    // Get compression ratio
    public double get_compression_ratio() {
        return ((double) this.compressed_size / this.original_size) * 100;
    }

    // Make dictionary
    private byte[] make_dictionary(Node root, int n) {
        Stack<Node> stk = new Stack<>();
        stk.push(root);
        StringBuilder builder = new StringBuilder();

        while (!stk.isEmpty()) {
            Node temp = stk.pop();
            if (temp.left == null && temp.right == null) {
                builder.append("1");
                number_ones++;
                if (temp.value.data.length < n) {
                    this.index = number_ones;
                    this.size = temp.value.data.length;
                }
                builder.append(new String(temp.value.data, StandardCharsets.ISO_8859_1));
            } else builder.append("0");
            if (temp.right == null && temp.left != null) stk.push(temp.left);
            else if (temp.left == null && temp.right != null) stk.push(temp.right);
            else if (temp.left != null) {
                stk.push(temp.right);
                stk.push(temp.left);
            }
        }
        String sb = n + "," + this.index + "," + this.size + "," + builder.length() + "," + builder;
        return sb.getBytes(StandardCharsets.ISO_8859_1);
    }

    // Write compressed file
    private void write_file(File file, BufferedOutputStream bos, int n, HashMap<Wrapper, String> huffman_codes) throws IOException {
        StringBuilder sb = new StringBuilder();
        Wrapper wrapper;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 1024 * 1024 * 2)) {
            byte[] data = new byte[n];
            int bytes_read;

            while ((bytes_read = bis.read(data)) != -1) {
                if (bytes_read < n) {
                    byte[] temp = new byte[bytes_read];
                    System.arraycopy(data, 0, temp, 0, bytes_read);
                    wrapper = new Wrapper(temp.clone());
                } else {
                    wrapper = new Wrapper(data.clone());
                }
                sb.append(huffman_codes.get(wrapper));
                while (sb.length() >= 8) {
                    String written_string = sb.substring(0, 8);
                    bos.write(((Integer) Integer.parseInt(written_string, 2)).byteValue());
                    sb.delete(0, 8);
                }
                data = new byte[n];
            }

            int count_zeros = 0;
            if (!sb.isEmpty()) {
                for (int i = sb.length(); i < 8; i++) {
                    sb.append("0");
                    count_zeros++;
                }
                bos.write(((Integer) Integer.parseInt(sb.toString(), 2)).byteValue());
            }
            bos.write(((byte) count_zeros));
        }
    }

}