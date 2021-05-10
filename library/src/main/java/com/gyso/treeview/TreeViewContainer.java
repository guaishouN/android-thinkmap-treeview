package com.gyso.treeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.Baseline;
import com.gyso.treeview.listener.TreeViewItemClick;
import com.gyso.treeview.listener.TreeViewItemLongClick;
import com.gyso.treeview.listener.TreeViewNotifier;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
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
    public  TreeModel<?> mTreeModel;
    private DrawInfo drawInfo;
    private TreeLayoutManager mTreeLayoutManager;
    private TreeViewItemClick mTreeViewItemClick;
    private TreeViewItemLongClick mTreeViewItemLongClick;
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

    public TreeViewContainer(Context context) {
        this(context, null, 0);
        init();
    }

    public TreeViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public TreeViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
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
        if(Math.abs(scale-1)>0.01f){
            //setPivotX((winWidth*scale-viewWidth)/(2*(scale-1)));
            //setPivotY((winHeight*scale-viewHeight)/(2*(scale-1)));
            setPivotX(0);
            setPivotY(0);
            setScaleX(1f/scale);
            setScaleY(1f/scale);
            minScale = 1f/scale;
            if (scale>1) {
                setTranslationX((winWidth-(viewWidth/scale))/2f);
                setTranslationY((winHeight-(viewHeight/scale))/2f);
            }
        }

        //仅记录一次
        if(centerMatrix==null&&winWidth>0&&winHeight>0){
            centerMatrix = new Matrix();
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
            centerMatrix = new Matrix();
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
            drawInfo.setToHolder(getTreeViewHolder(node));
            Baseline adapterDrawLine = adapter.onDrawLine(drawInfo);
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
        //set the node click
        view.setOnClickListener(this::performTreeItemClick);
        view.setOnLongClickListener(this::preformTreeItemLongClick);
        this.addView(view);
        if(nodeViewMap !=null ){
            nodeViewMap.put(node,treeViewHolder);
        }
    }

    public void setTreeViewItemClick(TreeViewItemClick treeViewItemClick) {
        mTreeViewItemClick = treeViewItemClick;
    }

    public void setTreeViewItemLongClick(TreeViewItemLongClick treeViewItemLongClick) {
        mTreeViewItemLongClick = treeViewItemLongClick;
    }

    private boolean preformTreeItemLongClick(View v) {
        if (mTreeViewItemLongClick != null) {
            mTreeViewItemLongClick.onLongClick(v);
        }
        return mTreeViewItemLongClick != null;
    }

    private void performTreeItemClick(View view) {
        if (mTreeViewItemClick != null) {
            mTreeViewItemClick.onItemClick(view);
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
            nodeViewMap = new HashMap<>(getChildCount());
            addNoteViews();
            mTreeModel.calculateTreeNodesDeep();
        }
    }

    @Override
    public void onItemViewChange(NodeModel<?> nodeModel){

    }
}
