package org.tmu.subdigger;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Saeed on 4/25/14.
 */
public interface Graph {
    boolean areNeighbor(int v, int w);

    public int[] getNeighbors(int v);

    public void printInfo();

    public int vertexCount();

    public int edgeCount();

    public Set<Integer> getVertices();

    public int getDegree(int vertex);

    public SubGraphStructure getSubGraph(int[] vertex_set);

    public long getSubGraphAsLong(int[] vertex_set);

    public boolean hasEdge(int v, int w);

    public void printToFile(String path) throws IOException;
}
