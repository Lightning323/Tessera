package com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.comparator;

import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.PriorityFuture;

import java.util.Comparator;

public class HighValueComparator implements Comparator<Runnable> {
    public int compare(Runnable first, Runnable second) {
        /**
         * 0 means that the two objects being compared are equal in terms of the ordering imposed by the Comparator.
         * 1 means that the first object being compared is greater than the second object.
         * -1 means that the first object being compared is less than the second object.
         */
        if (first == null && second == null)
            return 0;
        else if (first == null)
            return 1;
        else if (second == null)
            return -1;
        else {
            long o1 = ((PriorityFuture<?>) first).getPriority();
            long o2 = ((PriorityFuture<?>) second).getPriority();

            return (o1 > o2) ? -1 : ((o1 == o2) ? 0 : 1);
        }
    }
}