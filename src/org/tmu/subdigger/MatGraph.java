package org.tmu.subdigger;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.google.common.primitives.Ints;

import java.io.*;
import java.util.*;

public class MatGraph implements Graph {
    public class Adjacency {
        IntOpenHashSet outSet = new IntOpenHashSet();
        IntOpenHashSet allSet = new IntOpenHashSet();
        int[] allArr = new int[0];
        int[] outArr = new int[0];
    }

    public List<Adjacency> table = new ArrayList<Adjacency>();
    public HashSet<Integer> vertices = new HashSet<Integer>();
    public boolean[] adjArr;
    private int edgeCount = 0;

    public int vertexCount() {
        return vertices.size();
    }

    public int edgeCount(){
        int sum=0;
        for(int v:vertices)
            sum+=getNeighbors(v).length;
        return sum;
    }

    @Override
    public Set<Integer> getVertices() {
        return vertices;
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

    final public int[] getOutNeighborArray(int vertex) {
        return table.get(vertex).outArr;
    }

    final public int getDegree(int vertex) {
        return table.get(vertex).allArr.length;
    }

    @Override
    public SubGraphStructure getSubGraph(int[] vertex_set) {
        SubGraphStructure sub_graph = new SubGraphStructure(vertex_set.length);
        System.arraycopy(vertex_set, 0, sub_graph.nodes, 0, vertex_set.length);

        for (int i = 0; i < vertex_set.length; i++) {
            for (int j = 0; j < vertex_set.length; j++)
                if (adjArr[vertex_set[i] * table.size() + vertex_set[j]])
                    sub_graph.setEdgeAt(i, j);
        }

        return sub_graph;
    }

    @Override
    public long getSubGraphAsLong(int[] vertex_set) {
        if (vertex_set.length > 8)
            throw new IllegalStateException("SubGraph size is larger than 8: " + vertex_set.length);

        long result = 0;
        for (int i = 0; i < vertex_set.length; i++) {
            for (int j = 0; j < vertex_set.length; j++)
                if (adjArr[vertex_set[i] * table.size() + vertex_set[j]])
                    result |= (1L << vertex_set.length * i + j);
        }
        return result;

    }

    public boolean areNeighbor(int v, int w) {
        return adjArr[table.size() * v + w] || adjArr[table.size() * w + v];
    }

    public boolean hasEdge(int v, int w) {
        return adjArr[table.size() * v + w];
    }


    final public int getDegreeSum() {
        int sum = 0;
        for (int v : vertices)
            sum += getDegree(v);
        return sum;
    }

    final private void update() {
        edgeCount = 0;
        adjArr = new boolean[table.size() * table.size()];
        for (int v : vertices) {
            Adjacency adj = table.get(v);

            adj.outArr = adj.outSet.toArray();
            if (vertexCount() < 10000)
                Arrays.sort(adj.outArr);
            for (int w : adj.outArr)
                adjArr[v * table.size() + w] = true;
            adj.outSet = new IntOpenHashSet(adj.outArr.length, 0.5f);
            adj.outSet.add(adj.outArr);

            adj.allArr = adj.allSet.toArray();
            if (vertexCount() < 10000)
                Arrays.sort(adj.allArr);
            adj.allSet = new IntOpenHashSet(adj.allArr.length, 0.5f);
            adj.allSet.add(adj.allArr);
            edgeCount += adj.outArr.length;
        }

    }


    public void printInfo() {
        System.out.printf("Total vertices: %,d\n", vertexCount());
        System.out.printf("Total edges: %,d\n", edgeCount);
        System.out.printf("Average degree: %f\n", getDegreeSum() / (double) vertexCount());
    }

    public static MatGraph readGraph(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        MatGraph graph = new MatGraph();

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


    public static MatGraph readStructure(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        MatGraph graph = new MatGraph();
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
                //throw new IOException("The input file is malformed!");
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


    public static MatGraph readFromFile(String path) throws IOException {
        return readGraph(new FileReader(path));
    }

    public static MatGraph readStructureFromFile(String path) throws IOException {
        return readStructure(new FileReader(path));
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

}