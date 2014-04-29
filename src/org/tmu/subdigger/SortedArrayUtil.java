package org.tmu.subdigger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Saeed on 3/1/14.
 */
public class SortedArrayUtil {

    public static int[] omitSmallerOrEqualElements(int[] list, int x) {
        int[] result = new int[list.length];
        int new_size = 0;
        for (int i = 0; i < list.length; i++)
            if (list[i] > x)
                result[new_size++] = list[i];
        return Arrays.copyOf(result, new_size);
    }

    public static boolean hasDuplicates(final int[] array) {
        Set<Integer> lump = new HashSet<Integer>();
        for (int i : array) {
            if (lump.contains(i)) return true;
            lump.add(i);
        }
        return false;
    }

    public static int contains(int[] list, int x) {
        int pos = -1;
        if (list.length > 10)
            pos = Arrays.binarySearch(list, x);
        else
            for (int i = 0; i < list.length; i++)
                if (list[i] == x)
                    pos = i;
        return pos >= 0 ? pos : -1;
    }

    public static int[] insert(int[] list, int x) {
        int pos = -1;
        if (list.length > 10)
            pos = Arrays.binarySearch(list, x);
        else
            for (int i = 0; i < list.length; i++)
                if (list[i] == x)
                    pos = i;

        int[] result = null;
        if (pos >= 0)
            return list.clone();//Arrays.copyOf(list, list.length);
        else {
            int i = 0;
            result = new int[list.length + 1];
            while (i < list.length && list[i] < x) {
                result[i] = list[i];
                i++;
            }
            result[i] = x;
            while (i < list.length && list[i] > x) {
                result[i + 1] = list[i];
                i++;
            }
            return result;
        }
    }


    public static int[] union(int[] sorted1, int[] sorted2) {
        int[] result = new int[sorted1.length + sorted2.length];
        int i = 0, j = 0, k = 0;

        while (i < sorted1.length && j < sorted2.length) {
            if (sorted1[i] < sorted2[j]) {
                result[k] = sorted1[i];
                i++;
            } else if (sorted1[i] > sorted2[j]) {
                result[k] = sorted2[j];
                j++;
            } else { //equal
                result[k] = sorted2[j];
                j++;
                i++;
            }
//            if (k > 0 && last == result[k])
//                k--;
//            last = result[k];
            k++;
        }

        while (i < sorted1.length) {
            result[k] = sorted1[i];
            i++;
            k++;
        }

        while (j < sorted2.length) {
            result[k] = sorted2[j];
            j++;
            k++;
        }

        return Arrays.copyOf(result, k);
    }

    public static int[] intersect(int[] sorted1, int[] sorted2) {
        int[] result = new int[Math.min(sorted1.length, sorted2.length)];
        int i = 0, j = 0, k = 0;

        while (i < sorted1.length && j < sorted2.length) {
            if (sorted1[i] < sorted2[j])
                i++;
            else if (sorted1[i] > sorted2[j])
                j++;
            else {
                result[k++] = sorted1[i];
                i++;
                j++;
            }
        }

        return Arrays.copyOf(result, k);
    }

    public static int[] difference(int[] sorted1, int[] sorted2) {
        int[] result = new int[sorted1.length];
        int i = 0, j = 0, k = 0;

        while (i < sorted1.length && j < sorted2.length) {
            if (sorted1[i] < sorted2[j]) {
                result[k++] = sorted1[i];
                i++;
            } else if (sorted1[i] > sorted2[j]) {
                j++;
            } else {
                i++;
                j++;
            }
        }
        for (; i < sorted1.length; i++)
            result[k++] = sorted1[i];
        return Arrays.copyOf(result, k);
    }

}
