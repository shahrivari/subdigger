package org.tmu.subdigger;

import com.google.common.base.Stopwatch;

import java.io.IOException;

/**
 * Created by Saeed on 6/16/14.
 */
public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        int k = 4;
        Stopwatch stopwatch = Stopwatch.createStarted();
        long mem = Runtime.getRuntime().freeMemory();
        Graph g = HashGraph.readStructureFromFile("X:\\networks\\marusumi\\jazz.txt");
        g.printInfo();
        System.out.printf("Used: %,d\n", mem - Runtime.getRuntime().freeMemory());
        System.out.printf("Time:%s\n", stopwatch);
        stopwatch.reset();


        SMPEnumerator.setVerbose(true);
        stopwatch.start();
        long found = SMPEnumerator.enumerateNonIsoInParallel(g, k, 4, "x:\\out.txt");
        System.out.printf("Found: %,d \t time:%s\n", found,stopwatch);

        found = 0;
        for (int v : g.getVertices()) {
            for (int w : g.getNeighbors(v)) {
                SMPState new_state = SMPState.makeState(v, w, g);
                if (new_state == null)
                    continue;
                found += SubgraphEnumerator.enumerateState(g, new_state, k).totalFreq();
            }
        }
        System.out.printf("Found: %,d \t time:%s\n", found, stopwatch);
    }

}
