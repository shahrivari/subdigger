package org.tmu.subdigger;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Saeed on 4/12/14.
 */
public class SMPState {
    int[] subgraph;
    IntArrayList extension;

    public SMPState() {
    }

    public SMPState(int root, int[] neighbors) {
        subgraph = new int[]{root};
        extension = IntArrayList.newInstanceWithCapacity(16);
        for (int v : neighbors)
            if (v > root)
                extension.add(v);
    }

    public SMPState expand(int w, Graph graph) {
        SMPState new_state = new SMPState();
        new_state.subgraph = Arrays.copyOf(subgraph, subgraph.length + 1);
        new_state.subgraph[new_state.subgraph.length - 1] = w;

        int[] w_neighs = graph.getNeighbors(w);
        new_state.extension = new IntArrayList(extension.size() + w_neighs.length);
        for (int i = 0; i < extension.size(); i++)
            new_state.extension.add(extension.buffer[i]);

        int j = 0;
        for (int i = 0; i < w_neighs.length; i++) {
            if (w_neighs[i] <= subgraph[0]) continue;
            for (j = 0; j < subgraph.length; j++)
                if (graph.areNeighbor(subgraph[j], w_neighs[i])) break;
            if (j == subgraph.length) new_state.extension.add(w_neighs[i]);
        }

        return new_state;
    }

    public static List<SMPState> getAllBiStates(Graph graph) {
        List<SMPState> list = new ArrayList<SMPState>();

        for (int v : graph.getVertices()) {
            SMPState state = new SMPState(v, graph.getNeighbors(v));
            while (!state.extension.isEmpty()) {
                int w = state.extension.get(state.extension.size() - 1);
                state.extension.remove(state.extension.size() - 1);
                list.add(state.expand(w, graph));
            }
        }

        return list;
    }

    public static List<SMPState> getAllBiStatesOrderedByLoad(final Graph graph) {
        List<SMPState> list = getAllBiStates(graph);
        Ordering<SMPState> ordering = new Ordering<SMPState>() {
            @Override
            public int compare(SMPState state1, SMPState state2) {
                int a = graph.getDegree(state1.subgraph[0]) + graph.getDegree(state1.subgraph[1]);
                int b = graph.getDegree(state2.subgraph[0]) + graph.getDegree(state2.subgraph[1]);
                return a > b ? +1 : a < b ? -1 : 0;
            }
        };
        List<SMPState> sorted = ordering.reverse().sortedCopy(list);
        return sorted;
    }


    public static List<SMPState> getAllOneStates(Graph graph) {
        List<SMPState> list = new ArrayList<SMPState>();

        for (int v : graph.getVertices())
            list.add(new SMPState(v, graph.getNeighbors(v)));
        return list;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int x : subgraph)
            result.append(x + ",");
        result.deleteCharAt(result.length() - 1);
        result.append("#");

        for (IntCursor x : extension)
            result.append(x.value + ",");
        result.deleteCharAt(result.length() - 1);
        return result.toString();

//        String result = Arrays.toString(subgraph) + "  extentsion:" + extension.toString();//Arrays.toString(extension);
//        return result;
    }

    public static SMPState fromString(String s) {
        SMPState state = new SMPState();
        String[] tokens = s.split("#");
        if (tokens.length != 2)
            throw new IllegalArgumentException("Bad input String.");

        IntArrayList list = new IntArrayList();
        for (String x : tokens[0].split(","))
            list.add(Integer.parseInt(x));
        state.subgraph = list.toArray();
        list.clear();

        for (String x : tokens[1].split(","))
            list.add(Integer.parseInt(x));
        state.extension = list;
        return state;
    }

}