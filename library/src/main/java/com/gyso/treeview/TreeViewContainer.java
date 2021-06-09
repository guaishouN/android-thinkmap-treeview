package com.gyso.treeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.cache_pool.PointPool;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.listener.TreeViewNotifier;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.touch.DragBlock;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * guaishouN 674149099@qq.com
 */

public class TreeViewContainer extends ViewGroup implements TreeViewNotifier {
    private static final String TAG = TreeViewContainer.class.getSimpleName();
    public static final Object IS_EDIT_DRAGGING = new Object();
    public static final double DRAG_HIT_SLOP = 50;
    public static final float Z_NOR = 0f;
    public static final float Z_SELECT = 10f;
    public TreeModel<?> mTreeModel;
    private DrawInfo drawInfo;
    private TreeLayoutManager mTreeLayoutManager;
    private int viewWidth;
    private int viewHeight;
    private int winWidth;
    private int winHeight;
    private float minScale = 1f;
    private Map<NodeModel<?>, TreeViewHolder<?>> nodeViewMap =null;
    private Paint mPaint;
    private Path mPath;
    private Matrix centerMatrix;
    private TreeViewAdapter<?> adapter;
    private final DragBlock dragBlock;
    private boolean isEditMode;
    private final ViewDragHelper dragHelper;

    public TreeViewContainer(Context context) {
        this(context, null, 0);
    }

    public TreeViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TreeViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        dragBlock = new DragBlock(this);
        dragHelper = ViewDragHelper.create(this, dragCallback);
    }

    private void init() {
        setClipChildren(false);
        setClipToPadding(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPath = new Path();
        mPath.reset();
        drawInfo = new DrawInfo();
        drawInfo.setPaint(mPaint);
        drawInfo.setPath(mPath);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        TreeViewLog.e(TAG,"onMeasure");
        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        if(MeasureSpec.getSize(widthMeasureSpec)>0 && MeasureSpec.getSize(heightMeasureSpec)>0){
            winWidth  = MeasureSpec.getSize(widthMeasureSpec);
            winHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (mTreeLayoutManager != null && mTreeModel != null) {
            mTreeLayoutManager.setViewport(winHeight,winWidth);
            mTreeLayoutManager.performMeasure(this);
            ViewBox viewBox = mTreeLayoutManager.getTreeLayoutBox();
            drawInfo.setSpace(mTreeLayoutManager.getSpacePeerToPeer(),mTreeLayoutManager.getSpaceParentToChild());
            int specWidth = MeasureSpec.makeMeasureSpec(Math.max(winWidth, viewBox.getWidth()), MeasureSpec.EXACTLY);
            int specHeight = MeasureSpec.makeMeasureSpec(Math.max(winHeight,viewBox.getHeight()),MeasureSpec.EXACTLY);
            setMeasuredDimension(specWidth,specHeight);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        TreeViewLog.e(TAG,"onLayout");
        if (mTreeLayoutManager != null && mTreeModel != null) {
            mTreeLayoutManager.performLayout(this);
            fixWindow();
        }
    }

    /**
     * fix view tree
     */
    private void fixWindow() {
        float scale;
        float hr = 1f*viewHeight/winHeight;
        float wr = 1f*viewWidth/winWidth;
        scale = Math.max(hr, wr);
        minScale = 1f/scale;

        //when first init
        if(centerMatrix==null&&winWidth>0&&winHeight>0){
            if(Math.abs(scale-1)>0.01f){
                //setPivotX((winWidth*scale-viewWidth)/(2*(scale-1)));
                //setPivotY((winHeight*scale-viewHeight)/(2*(scale-1)));
                setPivotX(0);
                setPivotY(0);
                setScaleX(1f/scale);
                setScaleY(1f/scale);
                if (scale>1) {
                    setTranslationX((winWidth-(viewWidth/scale))/2f);
                    setTranslationY((winHeight-(viewHeight/scale))/2f);
                }
            }
            centerMatrix = new Matrix();
        }

        if(centerMatrix!=null){
            centerMatrix.set(getMatrix());
            float[] centerM = new float[9];
            centerMatrix.getValues(centerM);
            centerM[2]=getX();
            centerM[5]=getY();
            centerMatrix.setValues(centerM);
        }

        //if child both w h is smaller than win than move to center
        int dx = (winWidth- mTreeLayoutManager.getTreeLayoutBox().getWidth())/2;
        int dy = (winHeight- mTreeLayoutManager.getTreeLayoutBox().getHeight())/2;
        if(dx>0 || dy>0){
            dx = Math.max(dx, 0);
            dy = Math.max(dy, 0);
            final int size = getChildCount();
            for (int i = 0; i < size; i++) {
                View child = getChildAt(i);
                child.layout(child.getLeft()+dx,child.getTop()+dy,child.getRight()+dx,child.getBottom()+dy);
            }
        }
        setTouchDelegate();
    }

    /**
    * setTouchDelegate, make the tree view can get touch event outside bounce
    */
    private void setTouchDelegate(){
        //make the touch Delegate
        post(()->{
            Rect delegateArea = new Rect();
            getHitRect(delegateArea);
            delegateArea.left   -= 1000;
            delegateArea.top    -= 1000;
            delegateArea.right  += 1000;
            delegateArea.bottom += 1000;
            TouchDelegate touchDelegate= new TouchDelegate(delegateArea,this);
            if(getParent() instanceof View){
                ((View) getParent()).setTouchDelegate(touchDelegate);
            }
        });
    }

    /**
     * 中点对焦
     */
    public void focusMidLocation() {
        TreeViewLog.e(TAG, "focusMidLocation: "+getMatrix());
        float[] centerM = new float[9];
        if(centerMatrix==null){
            TreeViewLog.e(TAG, "no centerMatrix!!!");
            return;
        }
        centerMatrix.getValues(centerM);
        float[] now = new float[9];
        getMatrix().getValues(now);
        TreeViewLog.e(TAG, "focusMidLocation: \n"
                + Arrays.toString(centerM)+"\n"
                + Arrays.toString(now));
        if(now[0]>0&&now[4]>0){
            animate().scaleX(centerM[0]).
                    translationX(centerM[2])
                    .scaleY(centerM[4]).
                    translationY(centerM[5])
                    .setDuration(500)
                    .start();
        }
    }

    public float getMinScale(){
        return minScale;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onInterceptTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        return isEditMode && dragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        if(isEditMode) {
            dragHelper.processTouchEvent(event);
        }
        return isEditMode;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        drawInfo.setWindowWidth(w);
        drawInfo.setWindowHeight(h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mTreeModel != null) {
            drawInfo.setCanvas(canvas);
            drawTreeLine(mTreeModel.getRootNode());
        }
    }

    /**
     * 绘制树形的连线
     * @param root root node
     */
    private void drawTreeLine(NodeModel<?> root) {
        LinkedList<? extends NodeModel<?>> childNodes = root.getChildNodes();
        for (NodeModel<?> node : childNodes) {
            drawInfo.setFromHolder(getTreeViewHolder(root));
            TreeViewHolder<?> toHolder = getTreeViewHolder(node);
            drawInfo.setToHolder(toHolder);
            drawDragBackGround(toHolder.getView());
            if(isEditMode && toHolder.getView().getTag(R.id.edit_and_dragging) == IS_EDIT_DRAGGING){
               //Is editing and dragging, so not draw line.
                drawTreeLine(node);
               continue;
            }
            BaseLine adapterDrawLine = adapter.onDrawLine(drawInfo);
            if(adapterDrawLine!=null){
                adapterDrawLine.draw(drawInfo);
            }else{
                mTreeLayoutManager.performDrawLine(drawInfo);
            }
            drawTreeLine(node);
        }
    }

    public TreeModel<?> getTreeModel() {
        return adapter.getTreeModel();
    }

    /**
     * 添加所有的NoteView
     */
    private void addNoteViews() {
        if (mTreeModel != null) {
            NodeModel<?> rootNode = mTreeModel.getRootNode();
            Deque<NodeModel<?>> deque = new ArrayDeque<>();
            deque.add(rootNode);
            while (!deque.isEmpty()) {
                NodeModel<?> poll = deque.poll();
                addNodeViewToGroup(poll);
                if(poll!=null){
                    LinkedList<? extends NodeModel<?>> childNodes = poll.getChildNodes();
                    deque.addAll(childNodes);
                }
            }
        }
    }

    private void addNodeViewToGroup(NodeModel<?> node) {
        TreeViewHolder<?> treeViewHolder = adapter.onCreateViewHolder(this, (NodeModel)node);
        adapter.onBindViewHolder((TreeViewHolder)treeViewHolder);
        View view = treeViewHolder.getView();
        view.setElevation(Z_NOR);
        this.addView(view);
        view.setTag(R.id.item_holder,treeViewHolder);
        if(nodeViewMap !=null ){
            nodeViewMap.put(node,treeViewHolder);
        }
    }

    private final ViewDragHelper.Callback dragCallback = new ViewDragHelper.Callback(){
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            TreeViewLog.d(TAG, "tryCaptureView: ");
            if(isEditMode && dragBlock.load(child)){
                child.setTag(R.id.edit_and_dragging,IS_EDIT_DRAGGING);
                child.setElevation(Z_SELECT);
                return true;
            }
            return false;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull  View child) {
            TreeViewLog.d(TAG, "getViewHorizontalDragRange: ");
            return Integer.MAX_VALUE;
        }

        @Override
        public int getViewVerticalDragRange(@NonNull  View child) {
            TreeViewLog.d(TAG, "getViewVerticalDragRange: ");
            return Integer.MAX_VALUE;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull  View child, int left, int dx) {
            TreeViewLog.d(TAG, "clampViewPositionHorizontal: ");
            final int oldLeft = child.getLeft();
            dragBlock.drag(dx,0);
            estimateToHitTarget(child);
            invalidate();
            return oldLeft;
        }

        @Override
        public int clampViewPositionVertical(@NonNull  View child, int top, int dy) {
            TreeViewLog.d(TAG, "clampViewPositionVertical: ");
            final int oldTop = child.getTop();
            dragBlock.drag(0,dy);
            estimateToHitTarget(child);
            invalidate();
            return oldTop;
        }

        @Override
        public void onViewReleased(@NonNull  View releasedChild, float xvel, float yvel) {
            TreeViewLog.d(TAG, "onViewReleased: ");
            Object fTag = releasedChild.getTag(R.id.the_hit_target);
            boolean getHit = fTag != null;
            if(getHit){
                TreeViewHolder<?> targetHolder = getTreeViewHolder((NodeModel)fTag);
                NodeModel<?> targetHolderNode = targetHolder.getNode();

                TreeViewHolder<?> releasedChildHolder = (TreeViewHolder<?>)releasedChild.getTag(R.id.item_holder);
                NodeModel<?> releasedChildHolderNode = releasedChildHolder.getNode();
                if(releasedChildHolderNode.getParentNode()!=null){
                    mTreeModel.removeNode(releasedChildHolderNode.getParentNode(),releasedChildHolderNode);
                }
                mTreeModel.addNode(targetHolderNode,releasedChildHolderNode);
                mTreeModel.calculateTreeNodesDeep();
                requestLayout();
            }else{
                //recover
                dragBlock.smoothRecover(releasedChild);
            }
            dragBlock.setDragging(false);
            releasedChild.setElevation(Z_NOR);
            releasedChild.setTag(R.id.edit_and_dragging,null);
            releasedChild.setTag(R.id.the_hit_target, null);
            invalidate();
        }
    };

    @Override
    public void computeScroll() {
        if(dragBlock.computeScroll()){
            invalidate();
        }
    }

    /**
     * find the hit node
     * @param srcView src view
     */
    private boolean estimateToHitTarget(View srcView) {
        PointF src = getCenterPoint(srcView);
        //hasHitOne
        Object tag = srcView.getTag(R.id.the_hit_target);

        if(tag instanceof NodeModel){
            TreeViewHolder<?> holder = getTreeViewHolder((NodeModel)tag);
            PointF opa = getCenterPoint(holder.getView());
            double v = Math.hypot(opa.x - src.x, opa.y - src.y);
            boolean keepHitting = DRAG_HIT_SLOP-v>0;
            TreeViewLog.d(TAG, "keep hitting: "+keepHitting);
            if(!keepHitting){
                srcView.setTag(R.id.the_hit_target,null);
            }
            PointPool.free(opa);
        }

        if(srcView.getTag(R.id.the_hit_target)==null){
            mTreeModel.doTraversalNodes((ITraversal<NodeModel<?>>) next -> {
                TreeViewHolder<?> holder = getTreeViewHolder(next);
                TreeViewLog.d(TAG, "try target : "+ holder.getNode().getValue());
                PointF op = getCenterPoint(holder.getView());
                double v = Math.hypot(op.x - src.x, op.y - src.y);
                boolean hasHit = DRAG_HIT_SLOP-v>0;
                if(hasHit && holder.getView()!=srcView){
                    TreeViewLog.d(TAG, "hit target : "+ holder.getNode().getValue());
                    mTreeModel.setFinishTraversal(true);
                    srcView.setTag(R.id.the_hit_target, holder.getNode());
                }
                PointPool.free(op);
            });
        }

        PointPool.free(src);

        return srcView.getTag(R.id.the_hit_target)!=null;
    }

    private void drawDragBackGround(View view){
        Object fTag = view.getTag(R.id.the_hit_target);
        boolean getHit = fTag != null;
        if(getHit){
            //draw
            double srcR = Math.hypot(view.getWidth(), view.getHeight());
            TreeViewHolder<?> holder = getTreeViewHolder((NodeModel)fTag);
            View targetView = holder.getView();
            double tarR = Math.hypot(targetView.getWidth(), targetView.getHeight());
            double fR = Math.max(srcR, tarR) / 2;

            mPaint.reset();
            mPaint.setColor(Color.parseColor("#4FF1286C"));
            mPaint.setStrokeWidth(20);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            PointF centerPoint = getCenterPoint(view);
            drawInfo.getCanvas().drawCircle(centerPoint.x,centerPoint.y,(float)fR,mPaint);
            PointPool.free(centerPoint);
        }
    }

    private PointF getCenterPoint(View view){
        return PointPool.obtain(view.getX()+view.getWidth()/2f, view.getY()+view.getHeight()/2f);
    }

    protected void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        ViewParent parent = getParent();
        if (parent instanceof View) {
            parent.requestDisallowInterceptTouchEvent(isEditMode);
        }
    }

    public void setTreeLayoutManager(TreeLayoutManager TreeLayoutManager) {
        mTreeLayoutManager = TreeLayoutManager;
        drawInfo.setLayoutType(mTreeLayoutManager.getTreeLayoutType());
    }

    public TreeViewAdapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(TreeViewAdapter<?> adapter) {
        this.adapter = adapter;
        this.adapter.setNotifier(this);
    }

    public TreeViewHolder<?> getTreeViewHolder(NodeModel<?> nodeModel) {
        if(nodeModel==null || nodeViewMap ==null){
            return null;
        }
        return nodeViewMap.get(nodeModel);
    }

    @Override
    public void onDataSetChange(){
        mTreeModel = adapter.getTreeModel();
        removeAllViews();
        if(mTreeModel!=null){
            nodeViewMap = nodeViewMap==null? new HashMap<>():nodeViewMap;
            nodeViewMap.clear();
            addNoteViews();
            mTreeModel.calculateTreeNodesDeep();
        }
    }

    @Override
    public void onItemViewChange(NodeModel<?> nodeModel){

    }
}
