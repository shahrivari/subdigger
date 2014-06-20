package org.tmu.subdigger;

import com.carrotsearch.hppc.LongLongOpenHashMap;
import com.carrotsearch.hppc.cursors.LongLongCursor;
import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.util.List;

/**
 * Created by Saeed on 6/16/14.
 */
public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        int k = 5;
        Stopwatch stopwatch=Stopwatch.createUnstarted();
        long mem = Runtime.getRuntime().freeMemory();
        Graph g = HashGraph.readStructureFromFile("d:\\temp\\celegans.txt");
        SMPEnumerator.setVerbose(false);
        stopwatch.start();
        long found = SMPEnumerator.enumerateNonIsoInParallel(g, k, 4, "x:\\out.txt");
        System.out.printf("Found: %,d \t time:%s\n", found,stopwatch);

        stopwatch.reset().start();
//        LongLongOpenHashMap res=SubgraphEnumerator.enumerateAllHPPC(g, k);
//        found=0;
//        for(LongLongCursor cur:res)
//            found+=cur.value;
        System.out.printf("Found: %,d \t time:%s\n", found,stopwatch);

        System.out.printf("Used: %,d", Runtime.getRuntime().freeMemory() - mem);
    }

}
