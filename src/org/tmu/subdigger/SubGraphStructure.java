package org.tmu.subdigger;

//import org.tmu.core.Util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: Saeed
 * Date: 6/5/13
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubGraphStructure {
    public static boolean beFast = false;
    public int[] nodes;
    public boolean[] edges;

    public SubGraphStructure(int size) {
        nodes = new int[size];
        edges = new boolean[size * size];

    }

    public SubGraphStructure(boolean[] adjacency) {
        if (Math.sqrt(adjacency.length) != (int) Math.sqrt(adjacency.length))
            throw new IllegalArgumentException("Adjacency size must be complete square integer!");

        int size = (int) Math.sqrt(adjacency.length);
        nodes = new int[size];
        for (int i = 0; i < size; i++)
            nodes[i] = i;
        edges = adjacency.clone();
    }

    public void setEdgeAt(int v, int w) {
        edges[v * nodes.length + w] = true;
    }

    public boolean getEdgeAt(int v, int w) {
        return edges[v * nodes.length + w];
    }

    @Override
    public String toString() {
        return Arrays.toString(nodes) + "\t" + getAdjacencyBits();
    }


    public int getOutDegree(int v) {
        int index = Util.arrayContains(nodes, v);
        if (index == -1)
            throw new IllegalArgumentException("The vertex is not available!");
        int out_degree = 0;
        for (int i = 0; i < nodes.length; i++)
            if (getEdgeAt(index, i))
                out_degree++;
        return out_degree;
    }

    public int getInDegree(int v) {
        int index = Util.arrayContains(nodes, v);
        if (index == -1)
            throw new IllegalArgumentException("The vertex is not available!");
        int in_degree = 0;
        for (int i = 0; i < nodes.length; i++)
            if (getEdgeAt(i, index))
                in_degree++;
        return in_degree;

    }


    final public SubGraphStructure getOrderedForm() {
        SubGraphStructure result = new SubGraphStructure(nodes.length);
        long[] ranks = new long[nodes.length];

        result.nodes = nodes.clone();//Arrays.copyOf(nodes, nodes.length);
        if (beFast)
            for (int v = 0; v < result.nodes.length; v++)
                ranks[v] = getOutDegree(result.nodes[v]);
        else
            for (int v = 0; v < result.nodes.length; v++)
                ranks[v] = getOutDegree(result.nodes[v]) * nodes.length + getInDegree(result.nodes[v]);

        Util.rankedInsertionSort(result.nodes, ranks);
        int[] index = new int[result.nodes.length];
        for (int i = 0; i < index.length; i++)
            index[i] = Util.arrayContains(result.nodes, nodes[i]);

        for (int i = 0; i < nodes.length; i++)
            for (int j = 0; j < nodes.length; j++)
                if (getEdgeAt(i, j))
                    result.setEdgeAt(index[i], index[j]);
        return result;
    }

    public BitSet getAdjacencyBits() {
        BitSet signature = new BitSet(nodes.length * nodes.length);
        for (int i = 0; i < nodes.length; i++)
            for (int j = 0; j < nodes.length; j++)
                if (getEdgeAt(i, j))
                    signature.set(i * nodes.length + j);
        return signature;
    }

    public BigInteger getAdjacencyAsBigInt() {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < nodes.length; i++)
            for (int j = 0; j < nodes.length; j++)
                if (getEdgeAt(i, j))
                    result = result.setBit(i * nodes.length + j);
        return result;
    }

    public BoolArray getAdjacencyArray() {
        return BoolArray.buildFrom(edges);
    }

    public String getAdjacencySignature() {
        char[] signature = new char[nodes.length * nodes.length];
        for (int i = 0; i < nodes.length; i++)
            for (int j = 0; j < nodes.length; j++)
                if (getEdgeAt(i, j))
                    signature[i * nodes.length + j] = '1';
                else
                    signature[i * nodes.length + j] = '0';
        return new String(signature);
    }
}
