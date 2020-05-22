package com.leaf.common.utils;

import java.util.Collection;

public class Collections {

    public static <E> boolean isEmpty(Collection<E> collection) {
        if (collection == null || collection.size() == 0) {
            return true;
        }
        return false;
    }

    public static <E> boolean isEmpty(E... e) {
        if (e == null || e.length == 0) {
            return true;
        }
        return false;
    }

    public static <E> boolean isNotEmpty(Collection<E> collection) {
        return !isEmpty(collection);
    }

    public static <E> boolean isNotEmpty(E... e) {
        return !isEmpty(e);
    }
}
