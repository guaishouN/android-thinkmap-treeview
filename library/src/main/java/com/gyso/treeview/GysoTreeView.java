package com.gyso.treeview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RenderNode;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.BitmapCompat;

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
    //private Paint paint = new Paint();

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
        treeViewContainer.setAnimateRemove(true);
        treeViewContainer.setAnimateMove(true);
        //paint.setColor(Color.RED);
        //paint.setStyle(Paint.Style.FILL);        ;
        //setLayerType(LAYER_TYPE_HARDWARE,paint);
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
    //Bitmap bitmap = null;
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //canvas.drawRect(100,100,500,500,paint);
        //bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        //canvas.saveLayer(0,0,getWidth(),getHeight(),paint);
        super.dispatchDraw(canvas);
        //doBlur(bitmap);
        //canvas.drawBitmap(bitmap,0,0,new Paint());
        //canvas.restore();
    }

    private void doBlur(Bitmap bitmap){
        Bitmap newmap;
        for (int i = 0; i < 10; i++) {
            newmap = goBlur(bitmap, 20f, getContext());
            bitmap = newmap;
        }
    }

    public static Bitmap goBlur(Bitmap bitmap,float radius,Context mContext) {
        Log.d(TAG, "goBlur: ");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(mContext);

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, result);

        //Set the radius of the blur: 0 < radius <= 25
        blurScript.setRadius(radius);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(result);

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return result;
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
