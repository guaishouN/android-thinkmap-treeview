package com.gyso.treeview.util;

/**
 * Created by OlaWang on 2017/7/12.
 */

/**
 * This util support the looper sate callback.
 * if you input the {@link LooperFlag#loopBody} value of {1,2,3}
 * you call the {@link LooperFlag#next()}
 * the callback will return the 1,2,3,1,2,3,1,2,3....
 *
 * @param <T>
 */
public class LooperFlag<T> {

    private T[] loopBody;
    private LooperListener<T> mListener;
    private int point = 0;

    public LooperFlag(T[] loopBody, LooperListener<T> listener) {
        this.loopBody = loopBody;
        mListener = listener;
    }

    public T next() {
        point += 1;
        if (point == loopBody.length) {
            point = 0;
        }

        if (mListener != null) {
            mListener.onLooper(loopBody[point]);
        }

        return loopBody[point];
    }

    public void setLooperListener(LooperListener<T> listener) {
        mListener = listener;
    }

    public interface LooperListener<T> {
        void onLooper(T item);
    }
}
