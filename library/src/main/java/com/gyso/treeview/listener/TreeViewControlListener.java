package com.gyso.treeview.listener;

import android.view.View;

import androidx.annotation.Nullable;

import com.gyso.treeview.model.NodeModel;

/**
 * @Author: 怪兽N
 * @Time: 2021/6/15
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * Listener for drag-move node, scale, drag the hold treeView
 */
public interface TreeViewControlListener {
    int MIN_SCALE  = -1;
    int FREE_SCALE = 0;
    int MAX_SCALE  = 1;
    void onScaling(int state, int percent);
    void onDragMoveNodesHit(@Nullable NodeModel<?> draggingNode, @Nullable NodeModel<?> hittingNode, @Nullable View draggingView, @Nullable View hittingView);
}
