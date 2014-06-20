package org.tmu.subdigger;

import com.carrotsearch.hppc.LongLongOpenHashMap;

import java.util.Stack;

/**
 * Created by Saeed on 6/20/14.
 */
public class SubgraphEnumerator {

    public static FreqMap enumerateState(final Graph graph, SMPState init_state, int k) {
        FreqMap freqmap = new FreqMap();
        Stack<SMPState> stack = new Stack<SMPState>();
        stack.push(init_state);
        int[] foundSubGraph = new int[k];

        while (stack.size() > 0) {
            SMPState state = stack.pop();
            if (state.subgraph.length >= k)
                throw new IllegalStateException("This must never HAPPEN!!!");

            while (!state.extension.isEmpty()) {
                int w = state.extension.get(state.extension.size() - 1);
                state.extension.remove(state.extension.size() - 1);
                if (state.subgraph.length == k - 1) {
                    System.arraycopy(state.subgraph, 0, foundSubGraph, 0, k - 1);
                    foundSubGraph[k - 1] = w;//state.extension[i];
                    SubGraphStructure sub = graph.getSubGraph(foundSubGraph);
                    freqmap.add(sub.getAdjacencyArray(), 1);
                } else {
                    SMPState new_state = state.expand(w, graph);
                    if (new_state.extension.size() > 0)
                        stack.add(new_state);
                }
            }
        }
        return freqmap;
    }

    public static LongLongOpenHashMap enumerateStateHPPC(final Graph graph, SMPState init_state, int k) {
        if(k>8)
            throw new IllegalArgumentException("k must be smaller or equal to 8.");

        LongLongOpenHashMap freqmap = new LongLongOpenHashMap(1024,0.5f);
        Stack<SMPState> stack = new Stack<SMPState>();
        stack.push(init_state);
        int[] foundSubGraph = new int[k];

        while (stack.size() > 0) {
            SMPState state = stack.pop();
            if (state.subgraph.length >= k)
                throw new IllegalStateException("This must never HAPPEN!!!");

            while (!state.extension.isEmpty()) {
                int w = state.extension.get(state.extension.size() - 1);
                state.extension.remove(state.extension.size() - 1);
                if (state.subgraph.length == k - 1) {
                    System.arraycopy(state.subgraph, 0, foundSubGraph, 0, k - 1);
                    foundSubGraph[k - 1] = w;//state.extension[i];
                    long subl = graph.getSubGraphAsLong(foundSubGraph);
                    freqmap.putOrAdd(subl, 1, 1);
                } else {
                    SMPState new_state = state.expand(w, graph);
                    if (new_state.extension.size() > 0)
                        stack.add(new_state);
                }
            }
        }
        return freqmap;
    }

}
