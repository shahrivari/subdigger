package org.tmu.subdigger;

import com.carrotsearch.hppc.IntArrayList;

import java.util.Arrays;

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

    @Override
    public String toString() {
        String result = Arrays.toString(subgraph) + "  extentsion:" + extension.toString();//Arrays.toString(extension);
        return result;
    }

}