package com.gyso.treeview.touch;

import android.graphics.PointF;
import android.view.View;
import android.view.ViewParent;
import android.widget.OverScroller;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.cache_pool.PointPool;
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
    private PointF prePointF = null;
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
        PointPool.free(prePointF);
        tmp.clear();
        System.gc();
    }

    public void smoothRecover(View referenceView, OverScroller mScroller) {
        ViewBox rBox = originPositionMap.get(referenceView);
        prePointF=PointPool.obtain(0f,0f);
        mScroller.startScroll(0,0,referenceView.getLeft()-rBox.left,referenceView.getTop()-rBox.top);
    }

    public void computeScroll(OverScroller mScroller) {
        if(mScroller.isFinished()){
            return;
        }
        PointF curPointF = PointPool.obtain(mScroller.getCurrX(), mScroller.getCurrY());
        mScroller.getCurrY();
        for (int i = 0; i < tmp.size(); i++) {
            View view = tmp.get(i);
            int dx = (int)(curPointF.x-prePointF.x);
            int dy = (int)(curPointF.y-prePointF.y);
            view.offsetLeftAndRight(-dx);
            view.offsetTopAndBottom(-dy);
        }
        if(prePointF!=null){
            prePointF.set(curPointF);
        }
        PointPool.free(curPointF);
    }
}
