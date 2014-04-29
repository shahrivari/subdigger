package org.tmu.subdigger;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by Saeed on 3/7/14.
 */
public class BoolArray implements Comparable<BoolArray> {
    private boolean[] array;

    public boolean[] getArray() {
        return array;
    }

    public BoolArray(boolean[] array) {
        this.array = array.clone();
    }

    public int size() {
        return getArray().length;
    }

    private BoolArray() {
    }

    public static BoolArray buildFrom(boolean[] array) {
        BoolArray result = new BoolArray();
        result.array = array;
        return result;
    }

    public BigInteger getAsBigInt() {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < getArray().length; i++)
            if (getArray()[i])
                result = result.setBit(i);
        return result;
    }

    @Override
    public String toString() {
        char[] signature = new char[getArray().length];
        for (int i = 0; i < getArray().length; i++)
            if (getArray()[i])
                signature[i] = '1';
            else
                signature[i] = '0';
        return new String(signature);
    }

    @Override
    public boolean equals(Object aThat) {
        if (this == aThat) return true;
        if (aThat == null) return false;
        if (!(aThat instanceof BoolArray)) return false;
        BoolArray that = (BoolArray) aThat;
        return Arrays.equals(getArray(), that.getArray());
    }

    @Override
    public int hashCode() {
        if (getArray() == null)
            return 0;
        int result = 1;
        for (int i = 0; i < getArray().length; i++)
            result = 31 * result + (getArray()[i] ? 1231 : 1237);
        return result;
    }


    @Override
    public int compareTo(BoolArray o) {
        if (getArray().length < o.getArray().length)
            return -1;
        if (getArray().length > o.getArray().length)
            return 1;

        for (int i = 0; i < getArray().length; i++) {
            if (getArray()[i] == o.getArray()[i])
                continue;
            if (getArray()[i] == true)
                return 1;
            else
                return -1;
        }
        return 0;
    }

    public static boolean[] longToBoolArray(long a, int size) {
        boolean[] arr = new boolean[size];
        for (int i = 0; i < size; i++)
            if ((a & (1L << i)) != 0)
                arr[i] = true;
        return arr;
    }

    public static long boolArrayToLong(boolean[] arr) {
        if (arr.length > 64)
            throw new IllegalStateException("Array size is larger than 64: " + arr.length);

        long result = 0;
        for (int i = 0; i < arr.length; i++)
            if (arr[i])
                result |= (1L << i);

        return result;
    }

}
