import java.io.*;
import java.nio.charset.StandardCharsets;

public class Decompression {
    int number_ones = 0;
    int dictionary_index = 0;
    boolean check = false;
    Node temp_root;
    Node root;

    public void decompress(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        StringBuilder number_bytes = new StringBuilder();
        StringBuilder dictionary_size = new StringBuilder();
        StringBuilder index_string = new StringBuilder();
        StringBuilder size_string = new StringBuilder();
        StringBuilder dictionary = new StringBuilder();

        String string_to_read;
        byte[] temp = new byte[1];
        int bytesRead = 0;
        while ((bytesRead = bis.read(temp)) != -1) {
            string_to_read = new String(temp, StandardCharsets.ISO_8859_1);
            if (string_to_read.equals(","))
                break;
            number_bytes.append(string_to_read);
        }
        while ((bytesRead = bis.read(temp)) != -1) {
            string_to_read = new String(temp, StandardCharsets.ISO_8859_1);
            if (string_to_read.equals(","))
                break;
            index_string.append(string_to_read);
        }
        while ((bytesRead = bis.read(temp)) != -1) {
            string_to_read = new String(temp, StandardCharsets.ISO_8859_1);
            if (string_to_read.equals(","))
                break;
            size_string.append(string_to_read);
        }
        while ((bytesRead = bis.read(temp)) != -1) {
            string_to_read = new String(temp, StandardCharsets.ISO_8859_1);
            if (string_to_read.equals(","))
                break;
            dictionary_size.append(string_to_read);
        }
        int n = Integer.parseInt(number_bytes.toString());
        int index = Integer.parseInt(index_string.toString());
        int size = Integer.parseInt(size_string.toString());
        int size_of_dictionary = Integer.parseInt(dictionary_size.toString());

        temp = new byte[size_of_dictionary];
        bis.read(temp);
        string_to_read = new String(temp, StandardCharsets.ISO_8859_1);
        dictionary.append(string_to_read);


        this.root = reconstruct_tree(dictionary, n, index, size);

        if (this.root.left == null && this.root.right == null) {
            this.check = true;
        }
        this.temp_root = this.root;

        decompress_body(bis, n, file);
    }

    private Node reconstruct_tree(StringBuilder dictionary, int n, int index, int size) {
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
            Node left_child = reconstruct_tree(dictionary, n, index, size);
            Node right_child = reconstruct_tree(dictionary, n, index, size);
            return new Node(null, left_child, right_child);
        }
    }

    public void decompress_body(BufferedInputStream bis, int n, File file) throws IOException {
        String b = "extracted." + file.getName().replace(".hc", "");
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().replace(file.getName(), b));
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[32000];
        StringBuilder stringOfBits = new StringBuilder();
        int bytesRead = 0;
        int shift = 0;
        while ((bytesRead = bis.read(buffer)) != -1) {

            if (bytesRead < 32000) {
                byte[] temp = new byte[bytesRead];
                System.arraycopy(buffer, 0, temp, 0, bytesRead);
                for (int i = 0; i < temp.length; i++)
                    stringOfBits.append(String.format("%8s", Integer.toBinaryString(temp[i] & 0xFF)).replace(' ', '0'));
            } else {
                for (int i = 0; i < buffer.length; i++)
                    stringOfBits.append(String.format("%8s", Integer.toBinaryString(buffer[i] & 0xFF)).replace(' ', '0'));
            }
            if (bis.available() == 1) {
                byte[] ZerosByte = new byte[1];
                bis.read(ZerosByte);
                String st = String.format("%8s", Integer.toBinaryString(buffer[0] & 0xFF)).replace(' ', '0');
                shift = Integer.parseInt(st, 2);
                stringOfBits = new StringBuilder(stringOfBits.substring(0, stringOfBits.length() - shift));
            } else if (bis.available() == 0) {
                StringBuilder ZerosByte = new StringBuilder(stringOfBits.substring(stringOfBits.length() - 8, stringOfBits.length()));
                shift = Integer.parseInt(ZerosByte.toString(), 2);
                stringOfBits = new StringBuilder(stringOfBits.substring(0, stringOfBits.length() - shift - 8));
            }
            helper(bos, stringOfBits);
            stringOfBits = new StringBuilder();
            buffer = new byte[32000];

        }

        bis.close();
        bos.close();
    }

    private void helper(BufferedOutputStream bufferedOutputStream, StringBuilder stringOfBits) throws IOException {
        if (this.temp_root == null) {
            throw new RuntimeException("Temporary root of the tree is null");
        }
        for (int i = 0; i < stringOfBits.length(); i++) {
            if (stringOfBits.charAt(i) == '0' && !this.check) {
                if (this.temp_root.left != null) {
                    this.temp_root = this.temp_root.left;
                } else {
                    throw new RuntimeException("Left node is null");
                }
            } else if (stringOfBits.charAt(i) == '1') {
                if (this.temp_root.right != null) {
                    this.temp_root = this.temp_root.right;
                } else {
                    throw new RuntimeException("Right node is null");
                }
            }
            if (this.temp_root.left == null && this.temp_root.right == null) {
                bufferedOutputStream.write(this.temp_root.value.data);
                this.temp_root = this.root;
            }
        }

    }
}
