package org.tmu.subdigger;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.google.common.primitives.Ints;

import java.io.*;
import java.util.*;

public class HashGraph implements Graph {

    public List<Adjacency> table = new ArrayList<Adjacency>();
    public Set<Integer> vertices = new HashSet<Integer>();
    private int edgeCount = 0;

    public static HashGraph readGraph(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        HashGraph graph = new HashGraph();

        while ((line = br.readLine()) != null) {
            if (line.isEmpty())
                continue;
            if (line.startsWith("#")) {
                System.out.printf("Skipped a line: [%s]\n", line);
                continue;
            }
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                System.out.printf("Skipped a line: [%s]\n", line);
                continue;
                //throw new IOException("The input file is malformed!");
            }
            int src = Integer.parseInt(tokens[0]);
            int dest = Integer.parseInt(tokens[1]);
            graph.addEdge(src, dest);
        }
        br.close();
        graph.update();
        return graph;
    }

    public static HashGraph readStructure(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        HashGraph graph = new HashGraph();
        Map<String, Integer> map = new HashMap<String, Integer>();
        int last_v = 0;

        while ((line = br.readLine()) != null) {
            if (line.isEmpty())
                continue;
            if (line.startsWith("#")) {
                System.out.printf("Skipped a line: [%s]\n", line);
                continue;
            }
            String[] tokens = line.split("\\s+");
            if (tokens.length < 2) {
                System.out.printf("Skipped a line: [%s]\n", line);
                continue;
            }
            if (!map.containsKey(tokens[0]))
                map.put(tokens[0], last_v++);
            if (!map.containsKey(tokens[1]))
                map.put(tokens[1], last_v++);
            graph.addEdge(map.get(tokens[0]), map.get(tokens[1]));
        }
        br.close();
        graph.update();
        return graph;
    }

    public static HashGraph readFromFile(String path) throws IOException {
        return readGraph(new FileReader(path));
    }

    public static HashGraph readStructureFromFile(String path) throws IOException {
        return readStructure(new FileReader(path));
    }

    public Set<Integer> getVertices() {
        return vertices;
    }

    public int vertexCount() {
        return vertices.size();
    }

    public int edgeCount(){
        int sum=0;
        for(int v:vertices)
            sum+=getNeighbors(v).length;
        return sum;
    }

    private boolean containsVertex(int vertex) {
        return vertices.contains(vertex);
    }

    private void addVertex(int vertex) {
        if (containsVertex(vertex))
            throw new IllegalArgumentException("The vertex is already available: " + vertex);
        vertices.add(vertex);
        while (table.size() <= vertex)
            table.add(new Adjacency());
    }

    private void addEdge(int source, int dest) {
        if (!containsVertex(source))
            addVertex(source);
        if (!containsVertex(dest))
            addVertex(dest);
        table.get(source).outSet.add(dest);
        table.get(source).allSet.add(dest);
        table.get(dest).allSet.add(source);
    }

    final public int[] getNeighbors(int vertex) {
        return table.get(vertex).allArr;
    }

    final public int getDegree(int vertex) {
        return table.get(vertex).allArr.length;
    }

    public boolean areNeighbor(int v, int w) {
        return table.get(v).allSet.contains(w);
    }

    public boolean hasEdge(int v, int w) {
        return table.get(v).outSet.contains(w);//Util.arrayContains(table.get(v).outArr,w)>=0;
    }

    final public int getDegreeSum() {
        int sum = 0;
        for (int v : vertices)
            sum += getDegree(v);
        return sum;
    }

    final private void update() {
        edgeCount = 0;
        for (int v : vertices) {
            Adjacency adj = table.get(v);

            adj.outArr = adj.outSet.toArray();
            Arrays.sort(adj.outArr);
            adj.outSet = new IntOpenHashSet(adj.outArr.length, 0.5f);
            adj.outSet.add(adj.outArr);


            adj.allArr = adj.allSet.toArray();
            Arrays.sort(adj.allArr);
            adj.allSet = new IntOpenHashSet(adj.allArr.length, 0.5f);
            adj.allSet.add(adj.allArr);

            edgeCount += adj.outArr.length;
        }

    }

    public void printInfo() {
        System.out.printf("Total vertices: %,d\n", vertexCount());
        System.out.printf("Total edges: %,d\n", edgeCount);
        double degree_mean = getDegreeSum() / (double) vertexCount();
        System.out.printf("Average degree: %f\n", degree_mean);
        double variance = 0;
        for (int v : vertices)
            variance += (degree_mean - getDegree(v)) * (degree_mean - getDegree(v));
        System.out.printf("STD degree: %f\n", Math.sqrt(variance / vertexCount()));

    }

    public void printToFile(String path) throws IOException {
        FileWriter writer = new FileWriter(path);
        int[] vs = Ints.toArray(vertices);
        Arrays.sort(vs);
        for (int v : vs) {
            Adjacency adj = table.get(v);
            writer.write(v + "\t" + Arrays.toString(adj.allArr) + "\n");
        }
        writer.close();
    }

    final public SubGraphStructure getSubGraph(int[] vertex_set) {
        SubGraphStructure sub_graph = new SubGraphStructure(vertex_set.length);
        System.arraycopy(vertex_set, 0, sub_graph.nodes, 0, vertex_set.length);

        for (int i = 0; i < vertex_set.length; i++) {
            for (int j = 0; j < vertex_set.length; j++)
                if (hasEdge(vertex_set[i], vertex_set[j]))
                    sub_graph.setEdgeAt(i, j);
        }

        return sub_graph;
    }

    final public long getSubGraphAsLong(int[] vertex_set) {
        if (vertex_set.length > 8)
            throw new IllegalStateException("SubGraph size is larger than 8: " + vertex_set.length);

        long result = 0;
        for (int i = 0; i < vertex_set.length; i++) {
            for (int j = 0; j < vertex_set.length; j++)
                if (hasEdge(vertex_set[i], vertex_set[j]))
                    result |= (1L << vertex_set.length * i + j);
        }
        return result;
    }

    public class Adjacency {
        IntOpenHashSet outSet = new IntOpenHashSet();
        IntOpenHashSet allSet = new IntOpenHashSet();
        int[] allArr = new int[0];
        int[] outArr = new int[0];
    }
}