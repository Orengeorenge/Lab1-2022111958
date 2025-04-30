import java.io.*;
import java.util.*;

public class Lab1 {
    // 用 Map 来表示有向图
    // key：单词（节点），value：指向其他单词的边及权重（次数）
    private Map<String, Map<String, Integer>> graph;

    public Lab1() {
        graph = new HashMap<>();
    }

    /**
     * 根据给定文件路径读取文本数据并构建有向图
     * 说明：文本中将所有非字母字符（包括标点）当作空格处理，
     * 并统一转换为小写字母。相邻单词即构成一条边，边的权重为出现次数。
     */
    public void readFromFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 将整篇文本读入，并用空格替换换行符
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        // 处理文本数据：将所有非字母字符替换为空格，再统一转换为小写
        String cleaned = content.toString().replaceAll("[^a-zA-Z\\s]", " ").toLowerCase();
        String[] words = cleaned.trim().split("\\s+");

        // 根据相邻单词添加边（计数当作权重），同时保证图中存在所有节点
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            graph.putIfAbsent(word1, new HashMap<>());
            Map<String, Integer> edges = graph.get(word1);
            edges.put(word2, edges.getOrDefault(word2, 0) + 1);
            // 确保 word2 也作为节点存在于图中
            graph.putIfAbsent(word2, new HashMap<>());
        }
        System.out.println("Graph built successfully from file: " + filePath);
    }

    /**
     * 显示有向图，按照自定义格式打印每个节点及其出边和对应的权重
     */
    public void showDirectedGraph() {
        System.out.println("\nDirected Graph:");
        for (String node : graph.keySet()) {
            System.out.print(node + " -> ");
            Map<String, Integer> edges = graph.get(node);
            if (edges.isEmpty()) {
                System.out.print("No outgoing edges");
            } else {
                for (String target : edges.keySet()) {
                    System.out.print(target + "(" + edges.get(target) + ") ");
                }
            }
            System.out.println();
        }
    }


    /**
     * 查询桥接词：若图中存在 word1→word3 且 word3→word2，则 word3 为桥接词。
     * 若输入的 word1 或 word2 不存在，则返回提示消息；
     * 若无桥接词则返回相应提示，否则返回所有满足条件的桥接词（按字典序排序）。
     */
    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }
        Set<String> bridgeWords = new HashSet<>();
        Map<String, Integer> adjacent = graph.get(word1);
        for (String intermediary : adjacent.keySet()) {
            if (graph.containsKey(intermediary)) {
                Map<String, Integer> nextEdges = graph.get(intermediary);
                if (nextEdges.containsKey(word2)) {
                    bridgeWords.add(intermediary);
                }
            }
        }
        if (bridgeWords.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("The bridge words from \"").append(word1)
                    .append("\" to \"").append(word2).append("\" are: ");
            List<String> list = new ArrayList<>(bridgeWords);
            Collections.sort(list);
            for (int i = 0; i < list.size(); i++) {
                sb.append("\"").append(list.get(i)).append("\"");
                if (i != list.size() - 1)
                    sb.append(", ");
            }
            sb.append(".");
            return sb.toString();
        }
    }

    /**
     * 根据输入文本及图中桥接词生成新文本：
     * 对输入文本中每两个相邻的单词（按原样输出，但查询时转换为小写），
     * 若存在一个或多个桥接词，则随机选取一个插入到这两个单词之间，其它情况保持原样。
     */
    public String generateNewText(String inputText) {
        String[] words = inputText.split("\\s+");
        if (words.length == 0) return inputText;

        StringBuilder newText = new StringBuilder();
        newText.append(words[0]); // 第一个单词保持原样
        Random rand = new Random();
        for (int i = 0; i < words.length - 1; i++) {
            String current = words[i].toLowerCase();
            String next = words[i + 1].toLowerCase();
            List<String> bridgeCandidates = new ArrayList<>();
            if (graph.containsKey(current)) {
                Map<String, Integer> neighbors = graph.get(current);
                for (String intermediary : neighbors.keySet()) {
                    if (graph.containsKey(intermediary)) {
                        if (graph.get(intermediary).containsKey(next)) {
                            bridgeCandidates.add(intermediary);
                        }
                    }
                }
            }
            // 若存在桥接词，则随机选取一个插入
            if (!bridgeCandidates.isEmpty()) {
                String bridge = bridgeCandidates.get(rand.nextInt(bridgeCandidates.size()));
                newText.append(" ").append(bridge);
            }
            newText.append(" ").append(words[i + 1]);
        }
        return newText.toString();
    }

    /**
     * 计算从单词 start 到 word end 之间的最短路径（路径上边权和最小）
     * 采用 Dijkstra 算法实现。如果不存在路径，则返回提示信息。
     */
    public String calcShortestPath(String start, String end) {
        if (!graph.containsKey(start) || !graph.containsKey(end)) {
            return "No " + start + " or " + end + " in the graph!";
        }
        // Dijkstra 算法初始化
        Set<String> visited = new HashSet<>();
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        // 用 PriorityQueue 按当前累计距离进行排序
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        pq.add(start);
        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (u.equals(end))
                break;
            if (visited.contains(u))
                continue;
            visited.add(u);
            Map<String, Integer> neighbors = graph.get(u);
            for (String v : neighbors.keySet()) {
                int weight = neighbors.get(v);
                if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
                    dist.put(v, dist.get(u) + weight);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }
        if (dist.get(end) == Integer.MAX_VALUE) {
            return "There is no path from " + start + " to " + end + ".";
        }
        // 重构最短路径
        List<String> path = new ArrayList<>();
        for (String at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        StringBuilder sb = new StringBuilder();
        sb.append("Shortest path from \"").append(start).append("\" to \"").append(end).append("\" is: ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1)
                sb.append(" -> ");
        }
        sb.append("\nTotal weight: ").append(dist.get(end));
        return sb.toString();
    }

    /**
     * 计算输入单词的 PageRank 值
     * PageRank 算法公式：
     *   PR(u) = (1 - d)/N + d * Σ[PR(v) / L(v)], v∈Bu
     * 其中 Bu 为所有指向 u 的节点集合，L(v) 为节点 v 的出度，
     * d（阻尼因子）取 0.85，N 为图中节点个数。
     * 迭代直至收敛或达到最大迭代次数。
     */
    // 全局缓存变量，用于存储整个图的 PageRank 计算结果
    private Map<String, Double> cachedPageRank = null;

    private static final int Maximum_Iterations_Number = 500;
    private static final double DAMPING_FACTOR = 0.85;
    private static final double TOLERANCE = 1e-6;

    public Double calPageRank(String target) {
        // 如果已经计算过并缓存，则直接返回缓存结果
        if (cachedPageRank != null) {
            if (!cachedPageRank.containsKey(target)) {
                System.out.println("Word \"" + target + "\" not found in the graph.");
                return null;
            }
            return cachedPageRank.get(target);
        }

        // 检查目标单词是否在图中存在
        if (!graph.containsKey(target)) {
            System.out.println("Word \"" + target + "\" not found in the graph.");
            return null;
        }

        int N = graph.size();
        Map<String, Double> pr = new HashMap<>();

        // 初始化所有节点的 PageRank 值均为 1/N
        for (String node : graph.keySet()) {
            pr.put(node, 1.0 / N);
        }

        // 迭代计算 PageRank 值
        for (int iter = 0; iter < Maximum_Iterations_Number; iter++) {
            // 初始化每个节点的新 PR 值，基础部分为 (1-d)/N
            Map<String, Double> newPr = new HashMap<>();
            for (String node : graph.keySet()) {
                newPr.put(node, (1 - DAMPING_FACTOR) / N);
            }

            // 计算当前所有悬挂节点（无出边）的 PR 累积
            double danglingSum = 0.0;
            for (String node : graph.keySet()) {
                Map<String, Integer> outLinks = graph.get(node);
                if (outLinks == null || outLinks.isEmpty()) {
                    danglingSum += pr.get(node);
                }
            }

            // 分配非悬挂节点的 PageRank
            for (String node : graph.keySet()) {
                Map<String, Integer> outLinks = graph.get(node);
                double currentPR = pr.get(node);
                // 如果有出边，均分 PageRank 给所有邻接节点
                if (outLinks != null && !outLinks.isEmpty()) {
                    double share = currentPR / outLinks.size();
                    for (String neighbor : outLinks.keySet()) {
                        newPr.put(neighbor, newPr.get(neighbor) + DAMPING_FACTOR * share);
                    }
                }
            }

            // 将悬挂节点累积的 PageRank 均分到所有节点
            double danglingContribution = DAMPING_FACTOR * danglingSum / N;
            for (String node : newPr.keySet()) {
                newPr.put(node, newPr.get(node) + danglingContribution);
            }

            // 计算新旧向量之间的变化，判断是否收敛
            double diff = 0.0;
            for (String node : graph.keySet()) {
                diff += Math.abs(newPr.get(node) - pr.get(node));
            }

            pr = newPr;
            if (diff < TOLERANCE) {
                break;
            }
        }

        // 缓存整个图各节点的 PageRank 计算结果
        cachedPageRank = pr;
        return cachedPageRank.get(target);
    }


    /**
     * 随机游走：
     * 从图中随机选择一个起始节点，然后沿着其出边随机遍历。
     * 当遇到即将采用的边已在本次遍历中出现过，或当前节点无出边时停止。
     * 同时记录并返回遍历的节点序列。
     */
    public String randomWalk() {
        String outputFilePath = "random_walk.txt";
        if (graph.isEmpty())
            return "";

        Random rand = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String current = nodes.get(rand.nextInt(nodes.size()));
        StringBuilder walk = new StringBuilder();
        walk.append(current);
        Set<String> visitedEdges = new HashSet<>();

        while (true) {
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null || neighbors.isEmpty())
                break;

            List<String> candidateEdges = new ArrayList<>();
            for (String neighbor : neighbors.keySet()) {
                String edge = current + "->" + neighbor;
                if (!visitedEdges.contains(edge)) {
                    candidateEdges.add(neighbor);
                }
            }
            if (candidateEdges.isEmpty()) {
                break;
            }
            String next = candidateEdges.get(rand.nextInt(candidateEdges.size()));
            String chosenEdge = current + "->" + next;
            visitedEdges.add(chosenEdge);
            walk.append(" -> ").append(next);
            current = next;
        }

        String result = walk.toString();

        // 将结果写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(result);
            System.out.println("Random walk saved to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing random walk to file: " + e.getMessage());
        }
        return result;
    }

    /**
     * 使用 Graphviz 将有向图保存为图形文件（PNG格式）
     * @param dotFilePath 保存DOT描述的文件路径，例如 "graph.dot"
     * @param imagePath 最终生成的图形文件路径，例如 "graph.png"
     */
    public void saveDirectedGraphAsImage(String dotFilePath, String imagePath) {
        Scanner scanner = new Scanner(System.in);
        // 检查DOT文件路径，要求非空且以“.dot”结尾
        while (dotFilePath == null || dotFilePath.trim().isEmpty() || !dotFilePath.endsWith(".dot")) {
            System.out.println("输入的DOT文件路径不正确，请重新输入DOT文件路径（例如：graph.dot）：");
            dotFilePath = scanner.nextLine();
        }

        // 检查图像文件路径，要求非空且以“.png”结尾
        while (imagePath == null || imagePath.trim().isEmpty() || !imagePath.endsWith(".png")) {
            System.out.println("输入的图像文件路径不正确，请重新输入图像文件路径（例如：graph.png）：");
            imagePath = scanner.nextLine();
        }
        // 1. 生成DOT格式描述
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        // 可选：增加全局样式，例如 layout 方式和节点样式
        dot.append("  rankdir=TB;\n"); // 使图垂直方向排列
        dot.append("  node [shape=ellipse, fontname=\"Helvetica\"];\n");
        for (String node : graph.keySet()) {
            // 为每个节点生成节点声明
            dot.append("  \"").append(node).append("\";\n");
            Map<String, Integer> edges = graph.get(node);
            for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                String target = entry.getKey();
                int weight = entry.getValue();
                // 为每条边添加权重标签
                dot.append("  \"").append(node).append("\" -> \"").append(target).append("\" [label=\"").append(weight).append("\"];\n");
            }
        }
        dot.append("}");

        // 2. 将DOT格式内容写入到文件中
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFilePath))) {
            writer.write(dot.toString());
            System.out.println("DOT file saved as: " + dotFilePath);
        } catch (IOException ex) {
            System.err.println("Error writing DOT file: " + ex.getMessage());
            return;
        }

        // 3. 调用 Graphviz 的dot工具生成图像文件
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFilePath, "-o", imagePath);
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Graph image generated successfully: " + imagePath);
            } else {
                System.err.println("Graphviz failed with exit code: " + exitCode);
            }
        } catch (Exception ex) {
            System.err.println("Error generating graph image: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Lab1 lab = new Lab1();
        Scanner scanner = new Scanner(System.in);
        // 不断询问用户输入文件路径，直到文件成功读取
        while (true) {
            System.out.println("请输入文本文件路径（含文件名）：");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            // 首先检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                System.out.println("文件不存在或路径错误，请重新输入正确的文件路径。");
                continue;
            }

            try {
                lab.readFromFile(filePath);
                // 如果readFromFile中没有抛出异常，则认为读取成功，退出循环
                break;
            } catch (Exception ex) {
                System.out.println("读取文件失败，请重新录入文件路径。错误信息：" + ex.getMessage());
            }
        }

        // 交互菜单
        while (true) {
            System.out.println("\n请选择操作：");
            System.out.println("1. 显示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两个单词间的最短路径");
            System.out.println("5. 计算单词的PageRank值");
            System.out.println("6. 随机游走");
            System.out.println("7. 将有向图保存为图形文件 (PNG)");
            System.out.println("0. 退出");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    lab.showDirectedGraph();
                    break;
                case "2":
                    System.out.println("请输入第一个单词：");
                    String word1 = scanner.nextLine().toLowerCase();
                    System.out.println("请输入第二个单词：");
                    String word2 = scanner.nextLine().toLowerCase();
                    System.out.println(lab.queryBridgeWords(word1, word2));
                    break;
                case "3":
                    System.out.println("请输入一行新文本：");
                    String newTextInput = scanner.nextLine();
                    System.out.println("生成的新文本为：");
                    System.out.println(lab.generateNewText(newTextInput));
                    break;
                case "4":
                    System.out.println("请输入起始单词：");
                    String start = scanner.nextLine().toLowerCase();
                    System.out.println("请输入目标单词：");
                    String end = scanner.nextLine().toLowerCase();
                    System.out.println(lab.calcShortestPath(start, end));
                    break;
                case "5":
                    System.out.println("请输入需要计算PageRank的单词：");
                    String prWord = scanner.nextLine().toLowerCase();
                    Double prValue = lab.calPageRank(prWord);
                    if (prValue != null)
                        System.out.printf("单词 \"%s\" 的 PageRank 值为: %.4f\n", prWord, prValue);
                    break;
                case "6":
                    System.out.println("随机游走结果：");
                    System.out.println(lab.randomWalk());
                    break;
                case "7":
                    System.out.println("请输入保存DOT文件的路径（例如：graph.dot）：");
                    String dotFilePath = scanner.nextLine();
                    System.out.println("请输入生成图像文件的路径（例如：graph.png）：");
                    String imagePath = scanner.nextLine();
                    lab.saveDirectedGraphAsImage(dotFilePath, imagePath);
                    break;
                case "0":
                    System.out.println("退出程序。");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("无效选项，请重试！");
            }
        }
    }
}
