package com.gyso.treeview.touch;

import android.view.View;
import android.view.ViewParent;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.util.ViewBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: 怪兽N
 * @Time: 2021/6/7  17:56
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * drag block
 */
public class DragBlock {
    private final List<View> tmp;
    private TreeViewContainer container = null;
    private Map<View, ViewBox> originPositionMap = new HashMap<>();
    public DragBlock(View view){
        tmp = new ArrayList<>();
        ViewParent parent = view.getParent();
        if(parent instanceof TreeViewContainer){
            container = (TreeViewContainer)parent;
        }
        tmp.add(view);
        originPositionMap.put(view,new ViewBox(view));
        addItem(view);
    }

    private void addItem(View view){
        Object tag = view.getTag(R.id.item_holder);
        if(tag instanceof TreeViewHolder){
            TreeViewHolder<?> holder = (TreeViewHolder<?>)tag;
            NodeModel<?> node = holder.getNode();
            for (NodeModel<?> n:node.getChildNodes()){
                TreeViewHolder<?> h = container.getTreeViewHolder(n);
                tmp.add(h.getView());
                originPositionMap.put(h.getView(),new ViewBox(h.getView()));
                addItem(h.getView());
            }
        }
    }

    public void drag(int dx, int dy){
        for (int i = 0; i < tmp.size(); i++) {
            View view = tmp.get(i);
            view.offsetLeftAndRight(dx);
            view.offsetTopAndBottom(dy);
        }
    }

    public void release(){
        container = null;
        originPositionMap.clear();
        tmp.clear();
        System.gc();
    }
}
