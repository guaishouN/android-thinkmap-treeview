package com.gyso.treeview.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.gyso.treeview.model.NodeModel;

/**
 * @Author: 怪兽N
 * @Time: 2021/4/23  15:20
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * View holder
 */
public class TreeViewHolder<T> {
    private View view;
    private NodeModel<T> node;
    public TreeViewHolder(View view, @NonNull NodeModel<T> node) {
        this.view = view;
        this.node = node;
    }

    public NodeModel<T> getNode() {
        return node;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setNode(NodeModel<?> node) {
        this.node = (NodeModel<T>)node;
    }
}
