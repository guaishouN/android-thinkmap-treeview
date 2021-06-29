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
import com.gyso.treeview.listener.TreeViewControlListener;
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
    private final TouchEventHandler treeViewGestureHandler;
    private boolean disallowIntercept = false;

    public GysoTreeView(@NonNull Context context) {
        this(context, null,0);
    }

    public GysoTreeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GysoTreeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        setClipChildren(false);
        setClipToPadding(false);
        treeViewContainer = new TreeViewContainer(getContext());
        treeViewContainer.setLayoutParams(layoutParams);
        addView(treeViewContainer);
        treeViewGestureHandler = new TouchEventHandler(getContext(), treeViewContainer);

        //Note: do not set setKeepInViewport(true), there still has bug
        treeViewGestureHandler.setKeepInViewport(false);

        //set animate default
        treeViewContainer.setAnimateAdd(true);
        treeViewContainer.setAnimateRemove(false);
        treeViewContainer.setAnimateMove(true);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        this.disallowIntercept = disallowIntercept;
        TreeViewLog.e(TAG, "requestDisallowInterceptTouchEvent:"+disallowIntercept);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onInterceptTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        return (!disallowIntercept && treeViewGestureHandler.detectInterceptTouchEvent(event)) || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        return !disallowIntercept && treeViewGestureHandler.onTouchEvent(event);
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

    public TreeViewEditor getEditor(){
        return new TreeViewEditor(treeViewContainer);
    }

    public void setTreeViewControlListener(TreeViewControlListener listener){
        treeViewGestureHandler.setControlListener(listener);
        treeViewContainer.setControlListener(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PointPool.freeAll();
        TreeViewLog.d(TAG, "onDetachedFromWindow: ");
    }
}
