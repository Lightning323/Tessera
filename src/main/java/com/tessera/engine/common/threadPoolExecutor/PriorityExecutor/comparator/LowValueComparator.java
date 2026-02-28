package com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.comparator;

import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.PriorityFuture;

import java.util.Comparator;

public class LowValueComparator implements Comparator<Runnable> {
    public int compare(Runnable o1, Runnable o2) {
        if (o1 == null && o2 == null)
            return 0;
        else if (o1 == null)
            return -1;
        else if (o2 == null)
            return 1;
        else {
            long p1 = ((PriorityFuture<?>) o1).getPriority();
            long p2 = ((PriorityFuture<?>) o2).getPriority();

            return p1 > p2 ? 1 : (p1 == p2 ? 0 : -1);
        }
    }
}