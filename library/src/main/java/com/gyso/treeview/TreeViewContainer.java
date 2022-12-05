package com.gyso.treeview;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.cache_pool.HolderPool;
import com.gyso.treeview.cache_pool.PointPool;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.listener.TreeViewControlListener;
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
    private static final boolean isDebug = BuildConfig.isDebug;
    public static final Object IS_EDIT_DRAGGING = new Object();
    public static final double DRAG_HIT_SLOP = 60;
    public static final float Z_NOR = 10f;
    public static final float Z_SELECT = 20f;
    public static final int DEFAULT_FOCUS_DURATION = 300;
    public static final float DEFAULT_REMOVE_ANIMATOR_DES = 100;

    public TreeModel<?> mTreeModel;
    private DrawInfo drawInfo;
    private TreeLayoutManager mTreeLayoutManager;
    private int viewWidth;
    private int viewHeight;
    private int winWidth;
    private int winHeight;
    private float minScale = 0.2f;
    private Map<NodeModel<?>, TreeViewHolder<?>> nodeViewMap =null;
    private Paint mPaint;
    private Matrix centerMatrix;
    private TreeViewAdapter<?> adapter;
    private final DragBlock dragBlock;
    private boolean isDraggingNodeMode;
    private final ViewDragHelper dragHelper;
    private final SparseArray<HolderPool> holderPools = new SparseArray<>();
    private final ViewConfiguration viewConf;
    private LayoutTransition mLayoutTransition;
    private boolean isAnimateRemove;
    private boolean isAnimateAdd;
    private boolean isAnimateMove;
    private TreeViewControlListener controlListener = null;

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
        viewConf = ViewConfiguration.get(context);
        TreeViewLog.e(TAG,"TreeViewContainer constructor");
    }

    private void init() {
        setClipChildren(false);
        setClipToPadding(false);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        Path mPath = new Path();
        mPath.reset();
        drawInfo = new DrawInfo();
        drawInfo.setPaint(mPaint);
        drawInfo.setPath(mPath);
        if(isDebug){
            setBackgroundColor(getResources().getColor(R.color.debug_container_color));
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        TreeViewLog.e(TAG,"onVisibilityAggregated");
        if(mLayoutTransition==null){
            mLayoutTransition = new LayoutTransition();
            //CHANGE_APPEARING CHANGE_DISAPPEARING CHANGING APPEARING
            mLayoutTransition.setAnimator(LayoutTransition.CHANGE_APPEARING, null);
            mLayoutTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);
            mLayoutTransition.setAnimator(LayoutTransition.CHANGING, null);
            mLayoutTransition.setAnimator(LayoutTransition.APPEARING, null);

            mLayoutTransition.setDuration(LayoutTransition.DISAPPEARING,DEFAULT_FOCUS_DURATION);
            ObjectAnimator disappearAnimator = ObjectAnimator.ofFloat(null, "alpha", 1f, 0f);
            mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, disappearAnimator);
        }
        //setLayoutTransition(mLayoutTransition);
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
        if(Math.abs(scale-1)>0.01f){
            //setPivotX((winWidth*scale-viewWidth)/(2*(scale-1)));
            //setPivotY((winHeight*scale-viewHeight)/(2*(scale-1)));
            setPivotX(0);
            setPivotY(0);
            setScaleX(1f/scale);
            setScaleY(1f/scale);
        }
        //when first init
        if(centerMatrix==null){
            centerMatrix = new Matrix();
        }
        centerMatrix.set(getMatrix());
        float[] values = new float[9];
        centerMatrix.getValues(values);
        values[Matrix.MTRANS_X]=0f;
        values[Matrix.MTRANS_Y]=0f;
        centerMatrix.setValues(values);
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
        if(now[Matrix.MSCALE_X]>0&&now[Matrix.MSCALE_Y]>0){
            animate().scaleX(centerM[Matrix.MSCALE_X])
                    .translationX(centerM[Matrix.MTRANS_X])
                    .scaleY(centerM[Matrix.MSCALE_Y])
                    .translationY(centerM[Matrix.MTRANS_Y])
                    .setDuration(DEFAULT_FOCUS_DURATION)
                    .start();
        }
    }

    public float getMinScale(){
        return minScale;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = dragHelper.shouldInterceptTouchEvent(event);
        TreeViewLog.e(TAG, "onInterceptTouchEvent: "+MotionEvent.actionToString(event.getAction())+" intercept:"+intercept);
        return isDraggingNodeMode && intercept;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TreeViewLog.e(TAG, "onTouchEvent: "+MotionEvent.actionToString(event.getAction()));
        if(isDraggingNodeMode) {
            dragHelper.processTouchEvent(event);
        }
        return isDraggingNodeMode;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        TreeViewLog.e(TAG,"onSizeChanged w["+w+"]h["+h+"]oldw["+oldw+"]oldh["+oldh+"]");
        viewWidth = w;
        viewHeight = h;
        drawInfo.setWindowWidth(w);
        drawInfo.setWindowHeight(h);
        fixWindow();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mTreeModel != null) {
            drawInfo.setCanvas(canvas);
            mTreeModel.traverseDirectChildren(this::drawTreeLine);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * 绘制树形的连线
     * @param parent parent node
     */
    private void drawTreeLine(NodeModel<?> parent) {
        LinkedList<? extends NodeModel<?>> childNodes = parent.getChildNodes();
        for (NodeModel<?> node : childNodes) {
            TreeViewHolder<?> parentHolder = getTreeViewHolder(parent);
            if(parentHolder.getHolderLayoutType()==TreeLayoutManager.LAYOUT_TYPE_NONE){
                parentHolder.setHolderLayoutType(mTreeLayoutManager.getTreeLayoutType());
            }
            drawInfo.setFromHolder(parentHolder);
            TreeViewHolder<?> childHolder = getTreeViewHolder(node);
            if(childHolder.getHolderLayoutType()==TreeLayoutManager.LAYOUT_TYPE_NONE){
                childHolder.setHolderLayoutType(mTreeLayoutManager.getTreeLayoutType());
            }
            drawInfo.setToHolder(childHolder);
            drawDragBackGround(childHolder.getView());
            if(isDraggingNodeMode && childHolder.getView().getTag(R.id.edit_and_dragging) == IS_EDIT_DRAGGING){
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
            mTreeModel.doTraversalNodes(this::addNodeViewToGroup);
        }
    }

    private void addNodeViewToGroup(NodeModel<?> node) {
        if(nodeViewMap.get(node)!=null){
            adapter.onBindViewHolder((TreeViewHolder)nodeViewMap.get(node));
            return;
        }
        TreeViewHolder<?> treeViewHolder = createHolder(node);
        adapter.onBindViewHolder((TreeViewHolder)treeViewHolder);
        View view = treeViewHolder.getView();
        view.setElevation(Z_NOR);
        view.setTag(R.id.item_holder,treeViewHolder);
        if(nodeViewMap !=null ){
            nodeViewMap.put(node,treeViewHolder);
        }
        this.addView(view);
    }

    private final ViewDragHelper.Callback dragCallback = new ViewDragHelper.Callback(){
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            TreeViewLog.d(TAG, "tryCaptureView: ");
            if(isDraggingNodeMode && dragBlock.load(child)){
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
            if(dragHelper.getViewDragState()==ViewDragHelper.STATE_DRAGGING){
                final int oldLeft = child.getLeft();
                dragBlock.drag(dx,0);
                estimateToHitTarget(child);
                invalidate();
                return oldLeft;
            }else{
                return left;
            }
        }

        @Override
        public int clampViewPositionVertical(@NonNull  View child, int top, int dy) {
            TreeViewLog.d(TAG, "clampViewPositionVertical: ");
            if(dragHelper.getViewDragState()== ViewDragHelper.STATE_DRAGGING){
                final int oldTop = child.getTop();
                dragBlock.drag(0,dy);
                estimateToHitTarget(child);
                invalidate();
                return oldTop;
            }else{
                return top;
            }
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
                mTreeLayoutManager.calculateByLayoutAlgorithm(mTreeModel);
                if(isAnimateMove()){
                    recordAnchorLocationOnViewPort(false,false,targetHolderNode);
                }
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
                //un hit listener
                if(controlListener!=null){
                    Object srcViewHolderTag = srcView.getTag(R.id.item_holder);
                    if(srcViewHolderTag instanceof TreeViewHolder){
                        controlListener.onDragMoveNodesHit(((TreeViewHolder<?>) srcViewHolderTag).getNode(),null,srcView,null);
                    }
                }
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

                    //hit listener
                    if(controlListener!=null){
                        Object srcViewHolderTag = srcView.getTag(R.id.item_holder);
                        if(srcViewHolderTag instanceof TreeViewHolder){
                            controlListener.onDragMoveNodesHit(((TreeViewHolder<?>) srcViewHolderTag).getNode(),next,srcView,holder.getView());
                        }
                    }
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
            float minGap = Math.min(mTreeLayoutManager.getSpacePeerToPeer(), mTreeLayoutManager.getSpaceParentToChild());
            double fR = minGap/getScaleX() + Math.max(srcR, tarR) / 2;

            mPaint.reset();
            mPaint.setColor(Color.parseColor("#4FF1286C"));
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            PointF centerPoint = getCenterPoint(view);
            drawInfo.getCanvas().drawCircle(centerPoint.x,centerPoint.y,(float)fR,mPaint);
            PointPool.free(centerPoint);
        }
    }

    private PointF getCenterPoint(View view){
        return PointPool.obtain(view.getX()+view.getWidth()/2f, view.getY()+view.getHeight()/2f);
    }

    protected void requestMoveNodeByDragging(boolean isEditMode) {
        this.isDraggingNodeMode = isEditMode;
        ViewParent parent = getParent();
        if (parent instanceof View) {
            parent.requestDisallowInterceptTouchEvent(isEditMode);
        }
    }

    public void setTreeLayoutManager(TreeLayoutManager TreeLayoutManager) {
        mTreeLayoutManager = TreeLayoutManager;
        mTreeLayoutManager.setTreeViewContainer(this);
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
            mTreeLayoutManager.calculateByLayoutAlgorithm(mTreeModel);
        }
    }

    @Override
    public void onAddNodes(NodeModel<?> parent, NodeModel<?>... childNodes) {
        if(adapter!=null){
            if(isAnimateAdd()){
                recordAnchorLocationOnViewPort(false, false,parent);
            }
            mTreeModel.addNode(parent,childNodes);
            mTreeLayoutManager.calculateByLayoutAlgorithm(mTreeModel);
            if(parent.parentNode==null){
                addNodeViewToGroup(parent);
            }
            for (NodeModel<?> node:childNodes) {
                addNodeViewToGroup(node);
            }
        }
    }

    @Override
    public void onRemoveNode(NodeModel<?> nodeModel) {
        innerRemoveNode(nodeModel, false);
    }

    @Override
    public void onRemoveChildNodes(NodeModel<?> parentNode) {
        innerRemoveNode(parentNode,true);
    }

    private void innerRemoveNode(NodeModel<?> nodeModel, boolean isRemoveChildNodesOnly){
        if(adapter!=null){
            if(isAnimateRemove()){
                recordAnchorLocationOnViewPort(true,isRemoveChildNodesOnly ,nodeModel);
                if(isRemoveChildNodesOnly){
                    nodeModel.traverseDirectChildren(next->adapter.getTreeModel().removeNode(nodeModel, next));
                }else{
                    adapter.getTreeModel().removeNode(nodeModel.getParentNode(), nodeModel);
                }
            }else{
                if(isRemoveChildNodesOnly){
                    nodeModel.traverseExcludeSelf(this::removeViewByNode);
                    nodeModel.traverseDirectChildren(next->adapter.getTreeModel().removeNode(nodeModel, next));
                }else{
                    nodeModel.traverseIncludeSelf(this::removeViewByNode);
                    adapter.getTreeModel().removeNode(nodeModel.getParentNode(), nodeModel);
                }
            }
            mTreeLayoutManager.calculateByLayoutAlgorithm(mTreeModel);
            requestLayout();
        }
    }

    /**
     * Remove View By Node
     * @param nodeModel node to remove the view
     */
    private void removeViewByNode(NodeModel<?> nodeModel){
        //remove view
        TreeViewHolder<?> holder = getTreeViewHolder(nodeModel);
        if(holder != null){
            removeView(holder.getView());
            recycleHolder(holder);
        }
    }

    /**
     * Prepare moving, adding or removing nodes, record the last one node as an anchor node on view port, so that make it looks smooth change
     *  @param targetNode  the target one
     */
    private void recordAnchorLocationOnViewPort(boolean isRemove, boolean isRemoveChildrenOnly, NodeModel<?> targetNode) {
        if(targetNode==null){
            return;
        }
        if(isRemove){
            //if remove, parent will be the target node
            Map<NodeModel<?>,View> removeNodeMap = new HashMap<>();
            if(isRemoveChildrenOnly){
                targetNode.traverseExcludeSelf(node -> {
                    removeNodeMap.put(node,getTreeViewHolder(node).getView());
                });
            }else{
                targetNode.traverseIncludeSelf(node -> {
                    removeNodeMap.put(node,getTreeViewHolder(node).getView());
                });
                targetNode = targetNode.getParentNode();
            }
            setTag(R.id.mark_remove_views,removeNodeMap);
        }
        if(targetNode!=null){
            TreeViewHolder<?> targetHolder = getTreeViewHolder(targetNode);
            if(targetHolder!=null){
                View targetHolderView = targetHolder.getView();
                targetHolderView.setElevation(Z_SELECT);
                ViewBox targetBox = ViewBox.getViewBox(targetHolderView);
                //get target location on view port
                ViewBox targetBoxOnViewport = targetBox.convert(getMatrix());

                setTag(R.id.target_node,targetNode);
                setTag(R.id.target_location_on_viewport,targetBoxOnViewport);

                //The relative locations of other nodes
                Map<NodeModel<?>,ViewBox> relativeLocationMap = new HashMap<>();
                mTreeModel.doTraversalNodes(node->{
                    TreeViewHolder<?> oneHolder = getTreeViewHolder(node);
                    ViewBox relativeBox =
                            oneHolder!=null?
                            ViewBox.getViewBox(oneHolder.getView()).subtract(targetBox):
                            new ViewBox();
                    relativeLocationMap.put(node,relativeBox);
                });
                setTag(R.id.relative_locations,relativeLocationMap);
            }
        }
    }

    private TreeViewHolder<?> createHolder(NodeModel<?> node) {
        int type = adapter.getHolderType(node);
        HolderPool holderPool = holderPools.get(type);
        if(holderPool==null){
            holderPool = new HolderPool();
            holderPools.put(type,holderPool);
        }else {
            TreeViewHolder<?> holder = holderPool.obtain();
            if(holder!=null){
                holder.setNode(node);
                return holder;
            }
        }
        return adapter.onCreateViewHolder(this, (NodeModel)node);
    }

    public void recycleHolder(TreeViewHolder<?> holder){
        int type = adapter.getHolderType(holder.getNode());
        HolderPool holderPool = holderPools.get(type);
        if(holderPool==null){
            holderPool = new HolderPool();
            holderPools.put(type,holderPool);
        }
        holderPool.free(holder);
    }

    @Override
    public void onItemViewChange(NodeModel<?> nodeModel){

    }

    public void setAnimateRemove(boolean animateRemove) {
        isAnimateRemove = animateRemove;
    }

    public void setAnimateAdd(boolean animateAdd) {
        isAnimateAdd = animateAdd;
    }

    public void setAnimateMove(boolean animateMove) {
        isAnimateMove = animateMove;
    }

    public boolean isAnimateRemove() {
        return isAnimateRemove;
    }

    public boolean isAnimateAdd(){
        return isAnimateAdd;
    }

    public boolean isAnimateMove(){
        return isAnimateMove;
    }

    public void setControlListener(TreeViewControlListener controlListener) {
        this.controlListener = controlListener;
    }
}
