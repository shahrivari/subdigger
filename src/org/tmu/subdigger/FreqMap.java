package org.tmu.subdigger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Saeed on 4/17/14.
 */
final public class FreqMap {
    final class Count {
        long value;

        Count(int value) {
            this.value = value;
        }

        public long get() {
            return value;
        }

        public long getAndAdd(int delta) {
            long result = value;
            value = result + delta;
            return result;
        }


        @Override
        public int hashCode() {
            return (int) (value ^ (value >>> 32));
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Count && ((Count) obj).value == value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }

    }

    HashMap<BoolArray, Count> map = new HashMap<BoolArray, Count>();

    public void add(BoolArray arr, int occurrences) {
        Count freq = map.get(arr);
        if (freq == null)
            map.put(arr, new Count(occurrences));
        else
            freq.getAndAdd(occurrences);
    }

    public int size() {
        return map.size();
    }

    public long totalFreq() {
        long sum = 0;
        for (Map.Entry<BoolArray, Count> e : map.entrySet())
            sum += e.getValue().get();
        return sum;
    }

    public void clear() {
        map.clear();
    }

}
