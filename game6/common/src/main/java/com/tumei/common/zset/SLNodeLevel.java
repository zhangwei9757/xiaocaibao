package com.tumei.common.zset;

public class SLNodeLevel<K extends Comparable<K>, V extends Comparable<V>> {
    private int span = 0;

    private SLNode<K, V> forward = null;

    public SLNode<K, V> getForward() {
        return forward;
    }

    public void setForward(SLNode<K, V> forward) {
        this.forward = forward;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }
}
