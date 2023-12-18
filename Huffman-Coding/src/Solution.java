import java.io.*;


public class Solution {
    public void compress(File file, int bytes, int chunk_size) throws IOException {
        System.out.println("Original Size: \u001B[33m" + file.length() + " \u001B[0mbytes");
        try {

            long start = System.currentTimeMillis();
            Compression compression = new Compression(file, bytes, chunk_size);
            long end = System.currentTimeMillis();

            System.out.println("Time: \u001B[35m" + (end - start) / 1000 + " \u001B[0ms");
            System.out.println("Compressed Size: \u001B[32m" + compression.get_compressed_size() + " \u001B[0mbytes");
            System.out.println("Compression Ratio: \u001B[34m" + compression.get_compression_ratio() + "\u001B[0m");

        } catch (IOException e) {
            System.out.println("\u001B[31mError reading file\u001B[0m");
            throw e;
        }
    }

    public void decompress(File file) {
        try {
            long start = System.currentTimeMillis();
            Decompression decompression = new Decompression(file);
            long end = System.currentTimeMillis();

            System.out.println("Time: \u001B[35m" + (end - start) / 1000 + " \u001B[0ms");

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\u001B[31mError reading file\u001B[0m");
        }
    }

    public static void main(String[] args) {
        Solution solution = new Solution();

        char method = 0;
        String filePath = null;
        int bytes = 1;

        if (args.length > 0 && args.length < 4) {
            method = args[0].charAt(0);

            if (method != 'c' && method != 'd') {
                System.out.println("\u001B[31mPlease provide a valid method (c => compression, d => decompression)\u001B[0m");
                return;
            }
            filePath = args[1];

            if (!new File(filePath).exists()) {
                System.out.println("\u001B[31mPlease provide a valid file path\u001B[0m");
                return;
            }

            if (method == 'c' && args.length == 2) {
                System.out.println("\u001B[31mPlease provide n bytes\u001B[0m");
                return;
            } else if (method == 'c') {
//                bytes = Integer.parseInt(args[2]);
            }

            System.out.println("File Path: \u001B[33m" + filePath + "\u001B[0m");

        } else {
            System.out.println("\u001B[31mPlease provide a correct arguments format\u001B[0m");
            return;
        }

        File file = new File(filePath);

        if (method == 'c') {
            try {
                long maxMemory = Runtime.getRuntime().maxMemory();
                int chunk_size = (int) (maxMemory / (4 * bytes));
                solution.compress(file, bytes, chunk_size);
            } catch (IOException e) {
                System.out.println("\u001B[31mError compressing file\u001B[0m");
            }
        } else {
            solution.decompress(file);
        }
    }
}
