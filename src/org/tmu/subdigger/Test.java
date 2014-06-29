package org.tmu.subdigger;

import com.google.common.base.Stopwatch;

import java.io.IOException;

/**
 * Created by Saeed on 6/16/14.
 */
public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        int k = 3;
        Stopwatch stopwatch = Stopwatch.createStarted();
        long mem = Runtime.getRuntime().freeMemory();
        Graph g = HashGraph.readStructureFromFile("/home/saeed/Documents/marusumi2/web-Stanford.txt");
        g.printInfo();
        System.out.printf("Used: %,d\n", mem - Runtime.getRuntime().freeMemory());
        System.out.printf("Time:%s\n", stopwatch);
        stopwatch.reset();
        SMPState.getSeedStates(g);

//        List<SMPState> states = SMPState.getAllBiStates(g);
//
//        String s;
//        SMPState st;
//        for (SMPState state : states) {
//            s = state.toString();
//            st = SMPState.fromString(s);
//            if (!st.toString().equals(s))
//                throw new IllegalStateException();
//        }

        SMPEnumerator.setVerbose(true);
        stopwatch.start();
        long found = SMPEnumerator.enumerateNonIsoInParallel(g, k, 4, "x:\\out.txt");
        System.out.printf("Found: %,d \t time:%s\n", found,stopwatch);

        stopwatch.reset().start();
//        LongLongOpenHashMap res=SubgraphEnumerator.enumerateAllHPPC(g, k);
//        found=0;
//        for(LongLongCursor cur:res)
//            found+=cur.value;
//        System.out.printf("Found: %,d \t time:%s\n", found,stopwatch);

    }

}
