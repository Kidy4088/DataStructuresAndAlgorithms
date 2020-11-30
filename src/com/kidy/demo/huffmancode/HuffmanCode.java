package com.kidy.demo.huffmancode;

import java.io.*;
import java.util.*;

/**
 * 赫夫曼编码
 *
 * @author Kidy
 * @date 2020/11/27 21:11
 */
public class HuffmanCode {
    public static void main(String[] args) {
        // String content = "i like like like java do you like a java";
        // byte[] huffmanCodeBytes = huffmanZip(content.getBytes());
        //
        // System.out.println(Arrays.toString(huffmanCodeBytes));
        //
        // byte[] decode = decode(huffmanCodeBytes, huffmanCodes);
        // System.out.println(new String(decode));

        // 测试压缩文件
        String sourceFile = "C:\\Users\\20132\\Pictures\\src1.bmp";
        String targetFile = "C:\\Users\\20132\\Pictures\\崩坏1.zip";
        // zipFile(sourceFile, targetFile);
        unZipFile(targetFile, sourceFile);
    }

    private static void unZipFile(String zipFile, String targetFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        ObjectInputStream ois = null;
        try {
            in = new FileInputStream(new File(zipFile));
            ois = new ObjectInputStream(in);
            byte[] huffmanBytes = (byte[]) ois.readObject();
            Map<Byte, String> huffmanCodes = (Map<Byte, String>) ois.readObject();
            byte[] bytes = decode(huffmanBytes, huffmanCodes);
            out = new FileOutputStream(new File(targetFile));
            out.write(bytes);
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩文件
     *
     * @param sourceFile 源文件路径
     * @param targetFile 目标文件路径
     */
    private static void zipFile(String sourceFile, String targetFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        ObjectOutputStream oos = null;
        try {
            in = new FileInputStream(new File(sourceFile));
            byte[] b = new byte[in.available()];
            //noinspection ResultOfMethodCallIgnored
            in.read(b);
            byte[] huffmanBytes = huffmanZip(b);
            out = new FileOutputStream(new File(targetFile));
            oos = new ObjectOutputStream(out);
            oos.writeObject(huffmanBytes);
            oos.writeObject(huffmanCodes);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 赫夫曼解码
     *
     * @param huffmanBytes
     * @param huffmanCodes
     * @return
     */
    private static byte[] decode(byte[] huffmanBytes, Map<Byte, String> huffmanCodes) {
        // 得到 huffmanBytes 对应的二进制字符串
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < huffmanBytes.length; i++) {
            stringBuilder.append(byteToBitString(i != huffmanBytes.length - 1, huffmanBytes[i]));
        }
        String string = stringBuilder.toString();

        Map<String, Byte> map = new HashMap<>();
        for (Map.Entry<Byte, String> entry : huffmanCodes.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }

        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < string.length(); ) {
            int count = 1;
            boolean flag = true;
            Byte b = null;
            while (flag) {
                String key = string.substring(i, i + count);
                b = map.get(key);
                if (null == b) {
                    count++;
                } else {
                    flag = false;
                }
            }
            i += count;
            bytes.add(b);
        }

        byte[] b = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            b[i] = bytes.get(i);
        }
        return b;
    }

    /**
     * byte转二进制字符串
     *
     * @param flag true:补高位 false:不补高位
     * @param b    传入的byte
     * @return byte对应的二进制字符串(补码)
     */
    private static String byteToBitString(boolean flag, byte b) {
        int temp = b;
        if (flag) {
            temp |= 256;
        }
        String string = Integer.toBinaryString(temp);
        return flag ? string.substring(string.length() - 8) : string;
    }

    /**
     * @param bytes 原始字节数组
     * @return 赫夫曼编码后的字节数组
     */
    private static byte[] huffmanZip(byte[] bytes) {
        List<Node> nodes = getNodes(bytes);
        Node huffmanTreeRoot = createHuffmanTree(nodes);
        Map<Byte, String> huffmanCodes = createHuffmanCodes(huffmanTreeRoot);
        return zip(bytes, huffmanCodes);
    }

    /**
     * 赫夫曼编码
     *
     * @param bytes        原始字符串对应的byte[]
     * @param huffmanCodes 生成的赫夫曼编码表
     * @return 返回赫夫曼编码处理后的byte[]
     */
    private static byte[] zip(byte[] bytes, Map<Byte, String> huffmanCodes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(huffmanCodes.get(b));
        }
        String string = stringBuilder.toString();

        int len = (string.length() + 7) / 8;
        byte[] huffmanCodeBytes = new byte[len];
        for (int i = 0; i < len; i++) {
            int start = i * 8;
            int end = start + 8;
            huffmanCodeBytes[i] = (byte) Integer.parseInt(string.substring(start, Math.min(end, string.length())), 2);
        }
        return huffmanCodeBytes;
    }

    /**
     * 根据byte[]生成List<Node>
     *
     * @param bytes
     * @return
     */
    private static List<Node> getNodes(byte[] bytes) {
        List<Node> nodes = new ArrayList<>();
        // 统计每个byte出现次数
        Map<Byte, Integer> counts = new HashMap<>();
        for (byte b : bytes) {
            counts.put(b, counts.get(b) == null ? 1 : counts.get(b) + 1);
        }
        for (Map.Entry<Byte, Integer> entry : counts.entrySet()) {
            nodes.add(new Node(entry.getKey(), entry.getValue()));
        }
        return nodes;
    }

    /**
     * 生成赫夫曼树
     *
     * @param nodes
     * @return
     */
    private static Node createHuffmanTree(List<Node> nodes) {
        while (nodes.size() > 1) {
            Collections.sort(nodes);
            Node leftNode = nodes.remove(0);
            Node rightNode = nodes.remove(0);
            Node parent = new Node(null, leftNode.getWeight() + rightNode.getWeight());
            parent.setLeft(leftNode);
            parent.setRight(rightNode);
            nodes.add(parent);
        }
        return nodes.get(0);
    }

    /**
     * 前序遍历
     *
     * @param root 根结点
     */
    private static void preOrder(Node root) {
        if (root == null) {
            return;
        }
        root.preOrder();
    }

    public static Map<Byte, String> huffmanCodes = new HashMap<>(50);
    public static StringBuilder stringBuilder = new StringBuilder();

    private static Map<Byte, String> createHuffmanCodes(Node root) {
        createHuffmanCodes(root, "", stringBuilder);
        return huffmanCodes;
    }

    /**
     * 生成赫夫曼编码表
     *
     * @param node          当前结点
     * @param code
     * @param stringBuilder
     */
    private static void createHuffmanCodes(Node node, String code, StringBuilder stringBuilder) {
        StringBuilder builder = new StringBuilder(stringBuilder);
        if (null == node) {
            return;
        }
        builder.append(code);
        if (null == node.getData()) {
            createHuffmanCodes(node.getLeft(), "0", builder);
            createHuffmanCodes(node.getRight(), "1", builder);
        } else {
            huffmanCodes.put(node.getData(), builder.toString());
        }
    }
}

/**
 * 创建Node
 */
class Node implements Comparable<Node> {
    /**
     * 数据(字符)本身
     */
    private Byte data;
    /**
     * 权值,字符出现的次数
     */
    private Integer weight;
    private Node left;
    private Node right;

    public Node(Byte data, Integer weight) {
        this.data = data;
        this.weight = weight;
    }

    public Byte getData() {
        return data;
    }

    public void setData(Byte data) {
        this.data = data;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    @Override
    public int compareTo(Node o) {
        return this.getWeight() - o.getWeight();
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                ", weight=" + weight +
                '}';
    }

    /**
     * 前序遍历
     */
    public void preOrder() {
        System.out.println(this);
        if (null != this.getLeft()) {
            this.getLeft().preOrder();
        }
        if (null != this.getRight()) {
            this.getRight().preOrder();
        }
    }
}
