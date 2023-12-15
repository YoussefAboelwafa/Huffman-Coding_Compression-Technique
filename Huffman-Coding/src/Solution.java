import java.io.*;


public class Solution {
    public void compress(File file, int bytes) throws IOException {
        // TODO: compress function
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
            }
        } catch (IOException e) {
            System.out.println("\u001B[31mError reading file\u001B[0m");
            throw e;
        }
    }

    public void decompress(File file) {
        // TODO: decompress function
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
                bytes = Integer.parseInt(args[2]);
            }

            System.out.println("Method: \u001B[33m" + method + "\u001B[0m");
            System.out.println("File path: \u001B[33m" + filePath + "\u001B[0m");
            System.out.println("Bytes: \u001B[33m" + bytes + "\u001B[0m");

        } else {
            System.out.println("\u001B[31mPlease provide a correct arguments format\u001B[0m");
            return;
        }

        File file = new File(filePath);

        if (method == 'c') {
            System.out.println("Original File size: \u001B[33m" + file.length() + " bytes\u001B[0m");
            try {
                solution.compress(file, bytes);
            } catch (IOException e) {
                System.out.println("\u001B[31mError compressing file\u001B[0m");
            }
        } else {
            solution.decompress(file);
        }
    }
}
