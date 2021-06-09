package com.gyso.treeview.touch;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
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
    private volatile boolean isDragging;
    private final TreeViewContainer container;
    private final Map<View, ViewBox> originPositionMap;
    private final OverScroller mScroller;
    private PointF prePointF = null;
    /**
     * Interpolator defining the animation curve for mScroller
     */
    private static final Interpolator sInterpolator = t -> {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    };

    public DragBlock(TreeViewContainer parent){
        tmp = new ArrayList<>();
        container = parent;
        this.mScroller = new OverScroller(container.getContext(), sInterpolator);
        originPositionMap = new HashMap<>();
    }

    public boolean load(View view){
        if(originPositionMap.isEmpty() && tmp.isEmpty()){
            tmp.add(view);
            originPositionMap.put(view,new ViewBox(view));
            addItem(view);
            return true;
        }
        return false;
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
        if(!mScroller.isFinished()){
            return;
        }
        this.isDragging = true;
        for (int i = 0; i < tmp.size(); i++) {
            View view = tmp.get(i);
            view.offsetLeftAndRight(dx);
            view.offsetTopAndBottom(dy);
        }
    }

    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    private void autoRelease(){
        if(mScroller.isFinished() && !isDragging){
            originPositionMap.clear();
            tmp.clear();
            System.gc();
        }
    }

    public void smoothRecover(View referenceView) {
        if(!mScroller.isFinished()){
            return;
        }
        ViewBox rBox = originPositionMap.get(referenceView);
        if(rBox ==null){
            return;
        }
        prePointF=PointPool.obtain(0f,0f);
        mScroller.startScroll(0,0,referenceView.getLeft()-rBox.left,referenceView.getTop()-rBox.top);
    }

    public boolean computeScroll() {
        boolean isSuc = mScroller.computeScrollOffset();
        if(isSuc){
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
            return true;
        }
        autoRelease();
        return false;
    }
}
