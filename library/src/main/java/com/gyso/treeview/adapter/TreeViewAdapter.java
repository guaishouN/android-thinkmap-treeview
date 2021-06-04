package com.gyso.treeview.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.listener.TreeViewNotifier;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @Author: 怪兽N
 * @Time: 2021/4/23  15:19
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe: The view adapter for the {@link com.gyso.treeview.TreeViewContainer}
 */
public abstract class TreeViewAdapter<T> {
    private TreeViewNotifier notifier;
    private TreeModel<T> treeModel;

    private ArrayList<NodeModel<T>> nodeList = new ArrayList<>();

    public void setTreeModel(TreeModel<T> treeModel) {
        this.treeModel = treeModel;
        notifyDataSetChange();
        resetNodeList();
    }


    public void resetNodeList() {
        this.nodeList.clear();
        this.nodeList.add(treeModel.getRootNode());
        resetNode(treeModel.getRootNode().childNodes);
    }

    /**
     * 递归放置每个节点
     *
     * @param list
     */
    private void resetNode(LinkedList<NodeModel<T>> list) {
        for (NodeModel<T> node : list) {
            this.nodeList.add(node);
            resetNode(node.childNodes);
        }
    }

    /**
     * 获取全部的节点 平级排列, 不论父子, 循环匹配使用
     */
    public ArrayList<NodeModel<T>> getNodeList() {
        return nodeList;
    }


    /**
     * Get tree model
     *
     * @return tree model
     */
    public TreeModel<T> getTreeModel() {
        return treeModel;
    }

    /**
     * For create view holder by your self
     *
     * @param viewGroup parent
     * @param model     node
     * @return holder
     */
    public abstract TreeViewHolder<T> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<T> model);

    /**
     * when bind the holder, set up you view
     *
     * @param holder holder
     */
    public abstract void onBindViewHolder(@NonNull TreeViewHolder<T> holder);

    /**
     * Draw line between node and node by you decision.
     * If you return an BaseLine, line will be draw by the return one instead of TreeViewLayoutManager's.
     *
     * @param drawInfo provides all you need to draw you line
     * @return the line draw you want to use for different nodes
     */
    public abstract BaseLine onDrawLine(DrawInfo drawInfo);


    public void setNotifier(TreeViewNotifier notifier) {
        this.notifier = notifier;
    }

    public void notifyDataSetChange() {
        if (notifier != null) {
            notifier.onDataSetChange();
        }
    }

    public void notifyItemViewChange(NodeModel<T> node) {
        if (notifier != null) {
            notifier.onItemViewChange(node);
        }
    }
}
