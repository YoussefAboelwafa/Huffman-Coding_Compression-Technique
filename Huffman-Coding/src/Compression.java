import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

public class Compression {
    private int index;
    private int number_ones = 0;
    private int size;
    private long original_size = 0;
    private long compressed_size = 0;

    public void compress(File file, int n) throws IOException {

        HashMap<Wrapper, Long> frequencies = read_bytes(file, n);

        PriorityQueue<Node> queue = new PriorityQueue<>(frequencies.size(), Comparator.comparingLong(Node::getFrequency));

        build_tree(frequencies, queue);
        HashMap<Wrapper, String> huffman_codes = new HashMap<>();

        Node root = queue.peek();
        if ((root != null ? root.left : null) == null && (root != null ? root.right : null) == null) {
            huffman_codes.put(root != null ? root.value : null, "0");
        } else {
            construct_code_words(huffman_codes, root, "");
        }
        compress_file(file, huffman_codes, make_dictionary(root, n), n);
    }

    private HashMap<Wrapper, Long> read_bytes(File file, int n) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis, 32000);
        HashMap<Wrapper, Long> frequencies = new HashMap<>();
        byte[] data = new byte[n];
        int bytes_read;

        while ((bytes_read = bis.read(data)) != -1) {
            if (bytes_read < n) {
                byte[] temp = new byte[bytes_read];
                System.arraycopy(data, 0, temp, 0, bytes_read);
                calculate_frequency(temp, frequencies);
            } else {
                calculate_frequency(data, frequencies);
            }
            data = new byte[n];
        }
        bis.close();
        return frequencies;
    }

    private void calculate_frequency(byte[] bytes, HashMap<Wrapper, Long> frequencies) {
        Wrapper temp = new Wrapper(bytes.clone());
        if (frequencies.containsKey(temp)) frequencies.put(temp, frequencies.get(temp) + 1);
        else frequencies.put(temp, (long) 1);
    }

    private void build_tree(HashMap<Wrapper, Long> frequencies, PriorityQueue<Node> queue) {
        for (Wrapper key : frequencies.keySet()) {
            queue.add(new Node(key, frequencies.get(key)));
        }
        construct_tree(queue);
    }

    private void construct_tree(PriorityQueue<Node> queue) {
        int i = queue.size();
        while (i > 1) {
            Node first = queue.poll();
            Node second = queue.poll();
            assert first != null;
            assert second != null;
            long frequencies_sum = first.getFrequency() + second.getFrequency();
            first.setCode("0");
            second.setCode("1");
            Node current = new Node(frequencies_sum, first, second);
            queue.add(current);
            i--;
        }
    }

    private void construct_code_words(HashMap<Wrapper, String> huffman_codes, Node root, String s) {
        s = s + root.getCode();
        if (root.left == null && root.right == null) {
            huffman_codes.put(root.value, s);
            root.setCode(s);
        } else {
            assert root.left != null;
            construct_code_words(huffman_codes, root.left, s);
            construct_code_words(huffman_codes, root.right, s);
        }
    }

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

    public long getCompressed_size() {
        return this.compressed_size;
    }

    public double getCompression_ratio() {
        return ((double) this.compressed_size / this.original_size) * 100;
    }

    private byte[] make_dictionary(Node root, int n) {
        Stack<Node> stk = new Stack<>();
        stk.push(root);
        StringBuilder dictionary = new StringBuilder();

        while (!stk.isEmpty()) {
            Node temp = stk.pop();
            if (temp.left == null && temp.right == null) {
                dictionary.append("1");
                number_ones++;
                if (temp.value.data.length < n) {
                    this.index = number_ones;
                    this.size = temp.value.data.length;
                }
                dictionary.append(new String(temp.value.data, StandardCharsets.ISO_8859_1));
            } else dictionary.append("0");
            if (temp.right == null && temp.left != null) stk.push(temp.left);
            else if (temp.left == null && temp.right != null) stk.push(temp.right);
            else if (temp.left != null) {
                stk.push(temp.right);
                stk.push(temp.left);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(n).append(",").append(this.index).append(",").append(this.size);
        sb.append(",").append(sb.length()).append(",").append(sb);
        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private void write_file(File file, BufferedOutputStream bos, int n, HashMap<Wrapper, String> huffman_codes) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis, 32000);
        byte[] data = new byte[n];
        int bytes_read;
        StringBuilder sb = new StringBuilder();
        Wrapper wrapper;
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
        bos.close();
        bis.close();
    }

}