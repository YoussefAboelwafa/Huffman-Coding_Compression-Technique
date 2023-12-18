import java.io.File;

import lombok.Getter;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Decompression {

    Decompression(File file) throws IOException, ClassNotFoundException {
        decode(file);
    }

    private void decode(File inputFile) {
        String originalFileName = inputFile.getName().replace(".hc", "");
        String outputFileName = "extracted." + originalFileName;
        File outputFile = new File(inputFile.getParent(), outputFileName);

        try (FileInputStream fis = new FileInputStream(inputFile); ObjectInputStream ois = new ObjectInputStream(fis)) {
            // Read the Huffman codes from the file
            Map<String, BitSet> huffman_codes = (Map<String, BitSet>) ois.readObject();

            // Build the Huffman tree from the Huffman codes
            Node root = build_tree(huffman_codes);

            try (FileInputStream fis2 = new FileInputStream(inputFile); FileOutputStream fos = new FileOutputStream(outputFile)) {
                // Skip over the Huffman codes
                long skipped = fis2.skip(fis2.available() - fis2.read());

                // Read the compressed data from the file
                byte[] compressedData = fis2.readAllBytes();

                // Decode the compressed data
                Node node = root;
                for (byte b : compressedData) {
                    for (int i = 7; i >= 0; i--) {
                        boolean bit = (b & (1 << i)) != 0;
                        if (node == null) {
                            throw new IOException("Invalid compressed data or Huffman tree");
                        }
                        if (bit) {
                            node = node.getRightNode();
                        } else {
                            node = node.getLeftNode();
                        }
                        if (node instanceof Leaf leaf) {
                            // Write the decoded string to the output file
                            fos.write(leaf.getString().getBytes());
                            node = root;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (ClassCastException e) {
            System.err.println("Invalid object type: " + e.getMessage());
        }
    }

    private Node build_tree(Map<String, BitSet> huffman_codes) {
        Node root = new Node(null, null);
        for (Map.Entry<String, BitSet> entry : huffman_codes.entrySet()) {
            String string = entry.getKey();
            BitSet code = entry.getValue();
            Node node = root;
            for (int i = 0; i < code.length(); i++) {
                boolean bit = code.get(i);
                if (bit) {
                    if (node.getRightNode() == null) {
                        node.setRightNode(new Node(null, null));
                    }
                    node = node.getRightNode();
                } else {
                    if (node.getLeftNode() == null) {
                        node.setLeftNode(new Node(null, null));
                    }
                    node = node.getLeftNode();
                }
            }
            node.setLeaf(new Leaf(string, 0));
        }
        return root;
    }
}
