package com.gyso.treeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.cache_pool.PointPool;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.listener.TreeViewItemClick;
import com.gyso.treeview.listener.TreeViewItemLongClick;
import com.gyso.treeview.touch.TouchEventHandler;
import com.gyso.treeview.util.TreeViewLog;


/**
 * @Author: 怪兽N
 * @Time: 2021/4/29  14:09
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * the main tree view.
 */
public class GysoTreeView extends FrameLayout {
    public static final String TAG = GysoTreeView.class.getSimpleName();
    private final TreeViewContainer treeViewContainer;
    private TouchEventHandler treeViewGestureHandler;
    public GysoTreeView(@NonNull Context context) {
        this(context, null,0);
    }

    public GysoTreeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GysoTreeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        treeViewContainer = new TreeViewContainer(getContext());
        treeViewContainer.setLayoutParams(layoutParams);
        addView(treeViewContainer);
        treeViewGestureHandler = new TouchEventHandler(getContext(), treeViewContainer);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onInterceptTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        return treeViewGestureHandler.detectInterceptTouchEvent(event) || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        return treeViewGestureHandler.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        treeViewGestureHandler.setViewport(w,h);
    }

    public void setAdapter(TreeViewAdapter adapter) {
        treeViewContainer.setAdapter(adapter);
    }

    public TreeViewAdapter getAdapter() {
        return treeViewContainer.getAdapter();
    }

    public void setTreeLayoutManager(TreeLayoutManager TreeLayoutManager) {
        treeViewContainer.setTreeLayoutManager(TreeLayoutManager);
    }

    public void focusMidLocation(){
        treeViewContainer.focusMidLocation();
    }

    public void setTreeViewItemClick(TreeViewItemClick treeViewItemClick) {
        treeViewContainer.setTreeViewItemClick(treeViewItemClick);
    }

    public void setTreeViewItemLongClick(TreeViewItemLongClick treeViewItemLongClick) {
        treeViewContainer.setTreeViewItemLongClick(treeViewItemLongClick);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PointPool.freeAll();
        TreeViewLog.d(TAG, "onDetachedFromWindow: ");
    }
}
