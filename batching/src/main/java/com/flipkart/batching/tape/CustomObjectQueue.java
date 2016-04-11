package com.flipkart.batching.tape;

import java.util.Collection;

/**
 * Created by kushal.sharma on 11/04/16.
 */
public interface CustomObjectQueue<E> extends ObjectQueue<E> {
    Collection<E> peek(int max);
}
