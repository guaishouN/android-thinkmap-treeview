package com.gyso.treeview.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.gyso.treeview.listener.TreeViewNotifier;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

/**
 * @Author: 怪兽N
 * @Time: 2021/4/23  15:19
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * The view adapter for the {@link com.gyso.treeview.TreeViewContainer}
 */
public abstract class TreeViewAdapter<T> {
    private TreeViewNotifier notifier;
    private TreeModel<T> treeModel;

    public void setTreeModel(TreeModel<T> treeModel) {
        this.treeModel = treeModel;
        notifyDataSetChange();
    }

    /**
     * get tree model
     * @return tree model
     */
    public TreeModel<T> getTreeModel(){
        return treeModel;
    }

    /**
     * for create view holder by your self
     * @param viewGroup parent
     * @param model node
     * @return holder
     */
    public abstract TreeViewHolder<T> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<T> model);

    /**
     * when bind the holder, set up you view
     * @param holder holder
     */
    public abstract void onBindViewHolder(@NonNull TreeViewHolder<T> holder);

    /**
     * draw line between node and node by you decision
     * @param canvas total canvas for this tree view
     * @param fromHolder holder from which one, you can get fromView and fromNodeModel from this holder
     * @param toHolder holder to which one, you can get toView and toNodeModel from this holder
     * @param paint paint from parent, don't create a new one because the method of onDraw(Canvas canvas) exec too much
     * @param path path from parent, same as paint
     */
    public abstract void onDrawLine(Canvas canvas, TreeViewHolder<T> fromHolder, TreeViewHolder<T> toHolder,Paint paint, Path path);

    public void setNotifier(TreeViewNotifier notifier){
        this.notifier = notifier;
    }

    public void notifyDataSetChange(){
        if(notifier!=null){
            notifier.onDataSetChange();
        }
    }

    public void notifyItemViewChange(NodeModel<T> node){
        if(notifier!=null){
            notifier.onItemViewChange(node);
        }
    }
}
