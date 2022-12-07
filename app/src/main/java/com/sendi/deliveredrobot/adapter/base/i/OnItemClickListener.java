package com.sendi.deliveredrobot.adapter.base.i;

public interface OnItemClickListener<E> extends OnRecyclerViewListener<E> {
    void onItemClick(E t);
}
