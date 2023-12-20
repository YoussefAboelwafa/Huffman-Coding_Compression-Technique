import java.io.*;
import java.nio.charset.StandardCharsets;

public class Decompression {
    private static final int BUFFER_SIZE = 32000;
    int number_ones = 0;
    int dictionary_index = 0;
    boolean check = false;
    Node temp_root;
    Node root;

    public void decompress(File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int n = Integer.parseInt(read_until_comma(bis));
            int index = Integer.parseInt(read_until_comma(bis));
            int size = Integer.parseInt(read_until_comma(bis));
            int size_of_dictionary = Integer.parseInt(read_until_comma(bis));

            String dictionary_string = read_bytes(bis, size_of_dictionary);
            StringBuilder dictionary = new StringBuilder(dictionary_string);

            this.root = reconstruct_huffman_tree(dictionary, n, index, size);

            if (this.root.left == null && this.root.right == null) {
                this.check = true;
            }
            this.temp_root = this.root;

            decompress_body(bis, file);
        }
    }

    private String read_until_comma(BufferedInputStream bis) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] temp = new byte[1];
        String string_to_read;
        while (bis.read(temp) != -1) {
            string_to_read = new String(temp, StandardCharsets.ISO_8859_1);
            if (string_to_read.equals(","))
                break;
            sb.append(string_to_read);
        }
        return sb.toString();
    }

    private String read_bytes(BufferedInputStream bis, int size) throws IOException {
        byte[] temp = new byte[size];
        bis.read(temp);
        return new String(temp, StandardCharsets.ISO_8859_1);
    }

    private Node reconstruct_huffman_tree(StringBuilder dictionary, int n, int index, int size) {
        if (dictionary.charAt(dictionary_index) == '1') {
            number_ones++;
            byte[] bytes;
            if (number_ones == index) {
                bytes = dictionary.substring(dictionary_index + 1, dictionary_index + size + 1).getBytes(StandardCharsets.ISO_8859_1);
                dictionary_index += (size + 1);
            } else {
                bytes = dictionary.substring(dictionary_index + 1, dictionary_index + n + 1).getBytes(StandardCharsets.ISO_8859_1);
                dictionary_index += (n + 1);
            }
            return new Node(new Wrapper(bytes.clone()), null, null);
        } else {
            dictionary_index++;
            Node left_child = reconstruct_huffman_tree(dictionary, n, index, size);
            Node right_child = reconstruct_huffman_tree(dictionary, n, index, size);
            return new Node(null, left_child, right_child);
        }
    }

    public void decompress_body(BufferedInputStream bis, File file) throws IOException {
        String b = "extracted." + file.getName().replace(".hc", "");
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().replace(file.getName(), b));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[BUFFER_SIZE];
        StringBuilder string_of_bits = new StringBuilder();
        int bytes_read;
        int shift;
        while ((bytes_read = bis.read(buffer)) != -1) {

            if (bytes_read < BUFFER_SIZE) {
                byte[] temp = new byte[bytes_read];
                System.arraycopy(buffer, 0, temp, 0, bytes_read);
                for (byte value : temp)
                    string_of_bits.append(String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
            } else {
                for (byte value : buffer)
                    string_of_bits.append(String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
            }
            if (bis.available() == 1) {
                byte[] zeros_byte = new byte[1];
                bis.read(zeros_byte);
                String st = String.format("%8s", Integer.toBinaryString(buffer[0] & 0xFF)).replace(' ', '0');
                shift = Integer.parseInt(st, 2);
                string_of_bits = new StringBuilder(string_of_bits.substring(0, string_of_bits.length() - shift));
            } else if (bis.available() == 0) {
                shift = Integer.parseInt(string_of_bits.substring(string_of_bits.length() - 8, string_of_bits.length()), 2);
                string_of_bits = new StringBuilder(string_of_bits.substring(0, string_of_bits.length() - shift - 8));
            }
            helper(bos, string_of_bits);
            string_of_bits = new StringBuilder();
            buffer = new byte[BUFFER_SIZE];

        }

        bis.close();
        bos.close();
    }

    private void helper(BufferedOutputStream bos, StringBuilder bits_string) throws IOException {
        if (this.temp_root == null) {
            throw new RuntimeException("Temporary root of the tree is null");
        }
        for (int i = 0; i < bits_string.length(); i++) {
            if (bits_string.charAt(i) == '0' && !this.check) {
                if (this.temp_root.left != null) {
                    this.temp_root = this.temp_root.left;
                } else {
                    throw new RuntimeException("Left node is null");
                }
            } else if (bits_string.charAt(i) == '1') {
                if (this.temp_root.right != null) {
                    this.temp_root = this.temp_root.right;
                } else {
                    throw new RuntimeException("Right node is null");
                }
            }
            if (this.temp_root.left == null && this.temp_root.right == null) {
                bos.write(this.temp_root.value.data);
                this.temp_root = this.root;
            }
        }

    }
}
