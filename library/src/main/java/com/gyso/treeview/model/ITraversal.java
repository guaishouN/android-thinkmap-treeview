package com.gyso.treeview.model;

import java.io.Serializable;

/**
 * traversal callback
 *  guaishouN 674149099@qq.com
 */

public interface ITraversal<T> extends Serializable {
    void next(T next);
    default void finish(){}
}
