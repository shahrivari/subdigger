package org.tmu.subdigger;

import com.carrotsearch.hppc.FloatArrayList;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.Ordering;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;

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
                int a = state1.extension.elementsCount;
                int b = state2.extension.elementsCount;
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

    public static List<SMPState> getAllOneStatesOrderedByLoad(final Graph graph) {
        List<SMPState> list = getAllOneStates(graph);
        Ordering<SMPState> ordering = new Ordering<SMPState>() {
            @Override
            public int compare(SMPState state1, SMPState state2) {
                return state1.extension.elementsCount > state2.extension.elementsCount ? +1 : state1.extension.elementsCount < state2.extension.elementsCount ? -1 : 0;
            }
        };
        List<SMPState> sorted = ordering.reverse().sortedCopy(list);
        return sorted;
    }

    public static List<SMPState> getSeedStates(final Graph graph){
        if(graph.vertexCount()<7000)
            return getAllBiStatesOrderedByLoad(graph);

        List<SMPState> one_states = getAllOneStatesOrderedByLoad(graph);
        IntArrayList degrees=IntArrayList.newInstance();

        int cut_point=100;

        for(SMPState s:one_states)
            degrees.add(s.extension.elementsCount);

        List<SMPState> result=new ArrayList<SMPState>();

        for(int x=one_states.size()-1;x>0;x--){
            SMPState state=one_states.get(x);
            one_states.remove(x);
            if(state.extension.elementsCount==0) continue;
            if(x>cut_point){
                result.add(state);
            }else{
                while (!state.extension.isEmpty()) {
                    int w = state.extension.get(state.extension.size() - 1);
                    state.extension.remove(state.extension.size() - 1);
                    result.add(state.expand(w, graph));
                }
            }

        }


//        degrees.resize(degrees.elementsCount/8);
//        double degree_threshold = DoubleMath.mean(degrees.toArray());
//
//        if(degree_threshold<degrees.get(256))
//            degree_threshold=degrees.get(256);
//
//
//
//        for(SMPState state:one_states){
//
//            if(state.extension.elementsCount>degree_threshold){//expand
//                while (!state.extension.isEmpty()) {
//                    int w = state.extension.get(state.extension.size() - 1);
//                    state.extension.remove(state.extension.size() - 1);
//                    result.add(state.expand(w, graph));
//                }
//            }
//            else
//                result.add(state);
//        }
        return result;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (subgraph.length > 0) {
            for (int x : subgraph)
            result.append(x + ",");
        result.deleteCharAt(result.length() - 1);
        }
        result.append("#");

        if (extension.size() > 0) {
            for (IntCursor x : extension)
            result.append(x.value + ",");
        result.deleteCharAt(result.length() - 1);
        }
        return result.toString();

//        String result = Arrays.toString(subgraph) + "  extentsion:" + extension.toString();//Arrays.toString(extension);
//        return result;
    }

    public static SMPState fromString(String s) {
        SMPState state = new SMPState();
        if (!s.contains("#"))
            throw new IllegalArgumentException("Bad input String.");

        String[] tokens = s.split("#");
        IntArrayList list = new IntArrayList();
        for (String x : tokens[0].split(","))
            list.add(Integer.parseInt(x));
        state.subgraph = list.toArray();

        state.extension = new IntArrayList();
        if (tokens.length > 1)
            for (String x : tokens[1].split(","))
                state.extension.add(Integer.parseInt(x));
        return state;
    }

}