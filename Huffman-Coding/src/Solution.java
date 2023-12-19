import java.io.*;


public class Solution {

    public static void main(String[] args) {

        Solution solution = new Solution();
        char method;
        String filePath;
        int bytes = 1;

        // Check if the arguments are valid
        if (args.length > 0 && args.length < 4) {
            method = args[0].charAt(0);

            // Check if the method is valid
            if (method != 'c' && method != 'd') {
                System.out.println("\u001B[31mPlease provide a valid method (c => compression, d => decompression)\u001B[0m");
                return;
            }
            filePath = args[1];

            // Check if the file path is valid
            if (!new File(filePath).exists()) {
                System.out.println("\u001B[31mPlease provide a valid file path\u001B[0m");
                return;
            }

            // Check if the bytes is valid
            if (method == 'c' && args.length == 2) {
                System.out.println("\u001B[31mPlease provide n bytes\u001B[0m");
                return;
            } else if (method == 'c') {
                bytes = Integer.parseInt(args[2]);
            }

            System.out.println("File Path: \u001B[33m" + filePath + "\u001B[0m");

        } else {
            System.out.println("\u001B[31mPlease provide a correct arguments format\u001B[0m");
            return;
        }

        File file = new File(filePath);

        if (method == 'c') {
            try {
                // Compress the file
                solution.compress(file, bytes);
            } catch (IOException e) {
                System.out.println("\u001B[31mError compressing file\u001B[0m");
            }
        } else {
            // Decompress the file
            solution.decompress(file);
        }
    }

    // Main method for compressing a file
    public void compress(File file, int bytes) throws IOException {
        try {
            long start = System.currentTimeMillis();
            Compression compression = new Compression();
            compression.compress(file, bytes);
            long end = System.currentTimeMillis();

            System.out.println("Compression Time: \u001B[35m" + (end - start) / 1000 + " \u001B[0ms");
            System.out.println("Original Size: \u001B[33m" + file.length() + " \u001B[0mbytes");
            System.out.println("Compressed Size: \u001B[32m" + compression.get_compressed_size() + " \u001B[0mbytes");
            System.out.println("Compression Ratio: \u001B[36m" + compression.get_compression_ratio() + " \u001B[0m");


        } catch (IOException e) {
            System.out.println("\u001B[31mError reading file\u001B[0m");
            throw e;
        }
    }

    // Main method for decompressing a file
    public void decompress(File file) {
        try {
            long start = System.currentTimeMillis();
            Decompression decompression = new Decompression();
            decompression.decompress(file);
            long end = System.currentTimeMillis();

            System.out.println("Decompression Time: \u001B[35m" + (end - start) / 1000 + " \u001B[0ms");

        } catch (IOException e) {
            System.out.println("\u001B[31mError reading file\u001B[0m");
        }
    }
}
