package org.tmu.subdigger;

import com.carrotsearch.hppc.LongLongOpenHashMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Saeed on 3/8/14.
 */
public class SignatureRepo {
    //HashMultiset<BoolArray> labelMap = HashMultiset.create();
    FreqMap labelMap = new FreqMap();
    LongLongOpenHashMap longLabelMap = new LongLongOpenHashMap();

    public static int getCapacity() {
        return capacity;
    }

    public static void setCapacity(int capacity) {
        SignatureRepo.capacity = capacity;
    }

    static int capacity = 64 * 1000 * 1000;


    FileWriter writer;

    public SignatureRepo(String path) throws IOException {
        writer = new FileWriter(path);
    }

    private boolean verbose = true;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private ReentrantLock lock = new ReentrantLock();

    public void add(Multiset<BoolArray> multiset) {
        lock.lock();
        for (HashMultiset.Entry<BoolArray> entry : multiset.entrySet()) {
            BoolArray label = new SubGraphStructure(entry.getElement().getArray()).getOrderedForm().getAdjacencyArray();
            labelMap.add(label, entry.getCount());
        }

        if (isVerbose()) {
            System.out.printf("Added %,d new signatures. LabelMap size:%,d\n", multiset.elementSet().size(), size());
        }
        if (size() > capacity)
            try {
                flush();
            } catch (IOException exp) {
                exp.printStackTrace();
                System.exit(-1);
            }
        lock.unlock();
    }

    public void add(LongLongOpenHashMap longMap, int k) {
        lock.lock();
        for (int i = 0; i < longMap.keys.length; i++) {
            if (longMap.allocated[i]) {
                long key = longMap.keys[i];
                key = BoolArray.boolArrayToLong(new SubGraphStructure(BoolArray.longToBoolArray(key, k * k)).getOrderedForm().getAdjacencyArray().getArray());
                longLabelMap.putOrAdd(key, longMap.values[i], longMap.values[i]);
            }
        }

        if (isVerbose()) {
            System.out.printf("Added %,d new signatures. LabelMap size:%,d\n", longMap.size(), size());
        }
        if (size() > capacity)
            try {
                flush();
            } catch (IOException exp) {
                exp.printStackTrace();
                System.exit(-1);
            }
        lock.unlock();
    }


    public int size() {
        return labelMap.size() + longLabelMap.size();
    }

    public void flush() throws IOException {
        lock.lock();
        for (Map.Entry<BoolArray, FreqMap.Count> entry : labelMap.map.entrySet())
            writer.write(entry.getKey().getAsBigInt().toString(32) + " " + entry.getValue() + "\n");
        for (int i = 0; i < longLabelMap.keys.length; i++)
            if (longLabelMap.allocated[i])
                writer.write(Long.toString(longLabelMap.keys[i], 32) + " " + longLabelMap.values[i] + "\n");

        if (isVerbose())
            System.out.printf("Flushed LabelMap to disk. Total %,d subgraphs.\n", size());
        labelMap.clear();
        longLabelMap.clear();
        writer.flush();
        lock.unlock();
    }

    public void close() throws IOException {
        flush();
        writer.close();
    }
}
