package com.gyso.treeview.listener;

import com.gyso.treeview.model.NodeModel;

/**
 * 作者    GUOQI
 * 时间    2021/6/2 13:29
 * 描述    拖动释放后回调
 */
public interface TreeViewDragMoveListener {
    /**
     * 释放
     *
     * @param sourceNode
     * @param targetNode
     */
    void onMove(NodeModel<?> sourceNode, NodeModel<?> targetNode);
}
