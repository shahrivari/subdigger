package org.tmu.subdigger;

import com.carrotsearch.hppc.LongLongOpenHashMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Ordering;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Saeed on 4/12/14.
 */
public class SMPEnumerator {

    private static boolean useHPPC = true;

    public static void setUseHPPC(boolean useHPPC) {
        SMPEnumerator.useHPPC = useHPPC;
    }


    private static int uniqueCap = 4 * 1000 * 1000;

    public static int getUniqueCap() {
        return uniqueCap;
    }

    public static void setUniqueCap(int cap) {
        uniqueCap = cap;
    }

    private static int reportStep = 10000000;
    private static boolean verbose = true;

    public static int getReportStep() {
        return reportStep;
    }

    public static void setReportStep(int reportStep) {
        SMPEnumerator.reportStep = reportStep;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        SMPEnumerator.verbose = verbose;
    }

    public static long enumerateNonIsoInParallel(final Graph graph, final int k, final int thread_count, final String out_path) throws IOException, InterruptedException {
        final AtomicLong found = new AtomicLong(0);
        final AtomicLong lastReport = new AtomicLong(0);
        final ArrayDeque<SMPState> queue = new ArrayDeque<SMPState>();
        for (int v : graph.getVertices()) {
            SMPState state = new SMPState(v, graph.getNeighbors(v));
            while (!state.extension.isEmpty()) {
                int w = state.extension.get(state.extension.size() - 1);
                state.extension.remove(state.extension.size() - 1);
                queue.push(state.expand(w, graph));
            }
        }


        Ordering<SMPState> ordering = new Ordering<SMPState>() {
            @Override
            public int compare(SMPState state1, SMPState state2) {
                int a = graph.getDegree(state1.subgraph[0]) + graph.getDegree(state1.subgraph[1]);
                int b = graph.getDegree(state2.subgraph[0]) + graph.getDegree(state2.subgraph[1]);
                return a > b ? +1 : a < b ? -1 : 0;
            }
        };
        List<SMPState> sorted = ordering.reverse().sortedCopy(queue);
        final ConcurrentLinkedQueue<SMPState> bq = new ConcurrentLinkedQueue<SMPState>(sorted);

        final SignatureRepo signatureRepo = new SignatureRepo(out_path);
        signatureRepo.setVerbose(verbose);

        Thread[] threads = new Thread[thread_count];
        final AtomicInteger live_threads = new AtomicInteger(thread_count);
        for (int i = 0; i < thread_count; i++) {
            final int thread_id = i;
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {

                    final HashMultiset<BoolArray> uniqueMap = HashMultiset.create();
                    final LongLongOpenHashMap luniqueMap = new LongLongOpenHashMap();

                    while (bq.size() > 0) {
                        SMPState top = null;
                        try {
                            top = bq.poll();
                        } catch (NoSuchElementException exp) {
                            break;
                        }
                        if (top == null) {
                            break;
                        }

                        Stack<SMPState> stack = new Stack<SMPState>();
                        stack.push(top);
                        int[] foundSubGraph = new int[k];

                        while (stack.size() > 0) {
                            SMPState state = stack.pop();
                            if (state.subgraph.length >= k)
                                throw new IllegalStateException("This must never HAPPEN!!!");

                            while (!state.extension.isEmpty()) {
                                int w = state.extension.get(state.extension.size() - 1);
                                state.extension.remove(state.extension.size() - 1);
                                if (state.subgraph.length == k - 1) {
                                    found.getAndIncrement();
                                    System.arraycopy(state.subgraph, 0, foundSubGraph, 0, k - 1);
                                    foundSubGraph[k - 1] = w;//state.extension[i];
                                    if (useHPPC && k <= 8) {
                                        long subl = graph.getSubGraphAsLong(foundSubGraph);
                                        luniqueMap.putOrAdd(subl, 1, 1);
                                        if (luniqueMap.size() > uniqueCap) {
                                            signatureRepo.add(luniqueMap, k);
                                            luniqueMap.clear();
                                        }
                                    } else {
                                        SubGraphStructure sub = graph.getSubGraph(foundSubGraph);
                                        uniqueMap.add(sub.getAdjacencyArray());
                                        if (uniqueMap.elementSet().size() > uniqueCap) {
                                            signatureRepo.add(uniqueMap);
                                            uniqueMap.clear();
                                        }
                                    }

                                    if (verbose && found.get() % reportStep == 0 && found.get() != lastReport.get()) {
                                        lastReport.set(found.get());
                                        System.out.printf("Found: %,d   \t LabelSet: %,d\n", found.get(), signatureRepo.size());
                                    }
                                } else {
                                    SMPState new_state = state.expand(w, graph);
                                    if (new_state.extension.size() > 0)
                                        stack.add(new_state);
                                }
                            }
                        }
                    }
                    signatureRepo.add(uniqueMap);
                    signatureRepo.add(luniqueMap, k);
                    uniqueMap.clear();
                    luniqueMap.clear();
                    System.out.printf("Thread %d finished. %d threads remaining.\n", thread_id, live_threads.decrementAndGet());
                }
            });
        }

        for (int i = 0; i < thread_count; i++)
            threads[i].start();

        for (int i = 0; i < thread_count; i++)
            threads[i].join();

        signatureRepo.close();

        return found.get();
    }

}