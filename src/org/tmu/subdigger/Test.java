package org.tmu.subdigger;

import java.io.IOException;
import java.util.List;

/**
 * Created by Saeed on 6/16/14.
 */
public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        int k = 4;
        long mem = Runtime.getRuntime().freeMemory();
        Graph g = HashGraph.readStructureFromFile("x:\\networks\\marusumi\\celegans.txt");
        SMPEnumerator.setVerbose(true);
        long found = SMPEnumerator.enumerateNonIsoInParallel(g, k, 4, "x:\\out.txt");
        System.out.printf("Found: %,d\n", found);

        List<SMPState> list = SMPState.getAllBistates(g);
        found = 0;

        for (SMPState state : list)
            found += SMPEnumerator.enumerateState(g, k, state).totalFreq();

        System.out.printf("Found: %,d\n", found);

        System.out.printf("Used: %,d", Runtime.getRuntime().freeMemory() - mem);
    }

}
