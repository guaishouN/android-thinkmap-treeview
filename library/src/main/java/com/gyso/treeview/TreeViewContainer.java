package com.gyso.treeview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.view.MotionEventCompat;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.listener.TreeViewDragMoveListener;
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
    public TreeModel<?> mTreeModel;
    private DrawInfo drawInfo;
    private TreeLayoutManager mTreeLayoutManager;
    private TreeViewItemClick mTreeViewItemClick;
    private TreeViewItemLongClick mTreeViewItemLongClick;
    private int viewWidth;
    private int viewHeight;
    private int winWidth;
    private int winHeight;
    private float minScale = 1f;
    private Map<NodeModel<?>, TreeViewHolder<?>> nodeViewMap = null;
    private Paint mPaint;
    private Path mPath;
    private Matrix centerMatrix;
    private TreeViewAdapter<?> adapter;

    //----------------------拖动新增------------------
    //是否进入编辑模式
    private Boolean isEditMode = false;
    //是否开始拖动
    private Boolean isStartDrag = false;
    // touch 点击开始时间
    private long startTime;
    // touch 间隔时间
    private static final long SPACE_TIME = 800;
    private WindowManager mWindowManager;
    //镜像的布局参数
    private WindowManager.LayoutParams mWindowLayoutParams;
    //用于拖拽的镜像
    private ImageView mDragImageView;
    //缩放后,实际拖动view的宽高
    private int viewW, viewH;
    //缩放系数
    private float treeScale = 0f;

    //拖动节点
    NodeModel<?> sourceNode = null;
    NodeModel<?> targetNode = null;
    NodeModel<?> lastNode = null;

    private TreeViewDragMoveListener dragMoveListener;

    //----------------------拖动新增(end)------------------


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

        //初始化
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void setOnDragMoveListener(TreeViewDragMoveListener listener) {
        this.dragMoveListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        if (MeasureSpec.getSize(widthMeasureSpec) > 0 && MeasureSpec.getSize(heightMeasureSpec) > 0) {
            winWidth = MeasureSpec.getSize(widthMeasureSpec);
            winHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (mTreeLayoutManager != null && mTreeModel != null) {
            mTreeLayoutManager.setViewport(winHeight, winWidth);
            mTreeLayoutManager.performMeasure(this);
            ViewBox viewBox = mTreeLayoutManager.getTreeLayoutBox();
            drawInfo.setSpace(mTreeLayoutManager.getSpacePeerToPeer(), mTreeLayoutManager.getSpaceParentToChild());
            int specWidth = MeasureSpec.makeMeasureSpec(Math.max(winWidth, viewBox.getWidth()), MeasureSpec.EXACTLY);
            int specHeight = MeasureSpec.makeMeasureSpec(Math.max(winHeight, viewBox.getHeight()), MeasureSpec.EXACTLY);
            setMeasuredDimension(specWidth, specHeight);
        } else {
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
        float hr = 1f * viewHeight / winHeight;
        float wr = 1f * viewWidth / winWidth;
        scale = Math.max(hr, wr);
        if (Math.abs(scale - 1) > 0.01f) {
            //setPivotX((winWidth*scale-viewWidth)/(2*(scale-1)));
            //setPivotY((winHeight*scale-viewHeight)/(2*(scale-1)));
            setPivotX(0);
            setPivotY(0);
            setScaleX(1f / scale);
            setScaleY(1f / scale);
            minScale = 1f / scale;
            if (scale > 1) {
                setTranslationX((winWidth - (viewWidth / scale)) / 2f);
                setTranslationY((winHeight - (viewHeight / scale)) / 2f);
            }
        }
        this.treeScale = 1 / scale;
        //Log.e(TAG, "初始化缩放系数 = " + treeScale);


        //仅记录一次
        if (centerMatrix == null && winWidth > 0 && winHeight > 0) {
            centerMatrix = new Matrix();
            centerMatrix.set(getMatrix());
            float[] centerM = new float[9];
            centerMatrix.getValues(centerM);
            centerM[2] = getX();
            centerM[5] = getY();
            centerMatrix.setValues(centerM);
        }

        //if child both w h is smaller than win than move to center
        int dx = (winWidth - mTreeLayoutManager.getTreeLayoutBox().getWidth()) / 2;
        int dy = (winHeight - mTreeLayoutManager.getTreeLayoutBox().getHeight()) / 2;
        if (dx > 0 || dy > 0) {
            dx = Math.max(dx, 0);
            dy = Math.max(dy, 0);
            final int size = getChildCount();
            for (int i = 0; i < size; i++) {
                View child = getChildAt(i);
                child.layout(child.getLeft() + dx, child.getTop() + dy, child.getRight() + dx, child.getBottom() + dy);
            }
        }
        setTouchDelegate();
    }

    /**
     * setTouchDelegate, make the tree view can get touch event outside bounce
     */
    private void setTouchDelegate() {
        //make the touch Delegate
        post(() -> {
            Rect delegateArea = new Rect();
            getHitRect(delegateArea);
            delegateArea.left -= 1000;
            delegateArea.top -= 1000;
            delegateArea.right += 1000;
            delegateArea.bottom += 1000;
            TouchDelegate touchDelegate = new TouchDelegate(delegateArea, this);
            if (getParent() instanceof View) {
                ((View) getParent()).setTouchDelegate(touchDelegate);
            }
        });
    }

    /**
     * 中点对焦
     */
    public void focusMidLocation() {
        TreeViewLog.e(TAG, "focusMidLocation: " + getMatrix());
        float[] centerM = new float[9];
        if (centerMatrix == null) {
            centerMatrix = new Matrix();
        }
        centerMatrix.getValues(centerM);
        float[] now = new float[9];
        getMatrix().getValues(now);
        TreeViewLog.e(TAG, "focusMidLocation: \n"
                + Arrays.toString(centerM) + "\n"
                + Arrays.toString(now));
        if (now[0] > 0 && now[4] > 0) {
            animate().scaleX(centerM[0]).
                    translationX(centerM[2])
                    .scaleY(centerM[4]).
                    translationY(centerM[5])
                    .setDuration(500)
                    .start();
        }
    }

    public float getMinScale() {
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
     *
     * @param root root node
     */
    private void drawTreeLine(NodeModel<?> root) {
        LinkedList<? extends NodeModel<?>> childNodes = root.getChildNodes();
        for (NodeModel<?> node : childNodes) {
            drawInfo.setFromHolder(getTreeViewHolder(root));
            drawInfo.setToHolder(getTreeViewHolder(node));
            BaseLine adapterDrawLine = adapter.onDrawLine(drawInfo);
            if (adapterDrawLine != null) {
                adapterDrawLine.draw(drawInfo);
            } else {
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
                if (poll != null) {
                    LinkedList<? extends NodeModel<?>> childNodes = poll.getChildNodes();
                    deque.addAll(childNodes);
                }
            }
        }
    }

    private void addNodeViewToGroup(NodeModel<?> node) {
        TreeViewHolder<?> treeViewHolder = adapter.onCreateViewHolder(this, (NodeModel) node);
        adapter.onBindViewHolder((TreeViewHolder) treeViewHolder);
        View view = treeViewHolder.getView();
        //set the node click
        view.setOnClickListener(this::performTreeItemClick);
//        view.setOnLongClickListener(this::preformTreeItemLongClick);
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case MotionEvent.ACTION_DOWN:
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                            //开启拖动
                            isEditMode = true;
                            int mOffset2Left = (int) (event.getRawX() - event.getX() - (getRight() - getLeft()) * treeScale);//点击位置 相对屏幕 所在View左上角的点x
                            int mOffset2Top = (int) (event.getRawY() - event.getY() - (getBottom() - getTop()) * treeScale);

                            if (!isStartDrag) {
                                Log.e(TAG, "创建镜像");
                                isStartDrag = true;

                                viewW = (int) (view.getWidth() * treeScale);
                                viewH = (int) (view.getHeight() * treeScale);

                                //绘制镜像
                                createDragImage(view, mOffset2Left, mOffset2Top, viewW, viewH);
                                sourceNode = treeViewHolder.getNode();
                                Log.e(TAG, "sourceNode = " + treeViewHolder.getNode().getValue().toString());

                                //外层view停止滑动
                                requestDisallowInterceptTouchEvent(true);
                            }
                            //拖动item
                            onDragItem((int) event.getRawX(), (int) event.getRawY());
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (isEditMode) {
                            startTime = 0;
                            isEditMode = false;
                            isStartDrag = false;
                            Log.e(TAG, "拖动结束");
                            removeDragImage();
                        }
                        break;
                }

                return false;
            }
        });
        this.addView(view);
        if (nodeViewMap != null) {
            nodeViewMap.put(node, treeViewHolder);
        }
    }

    private Paint customPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);//画笔宽度
        paint.setAntiAlias(true);//光滑
        return paint;
    }

    /**
     * 创建拖动的镜像
     */
    private void createDragImage(View view, int downX, int downY, int viewW, int viewH) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = downX;
        mWindowLayoutParams.y = downY;
        mWindowLayoutParams.alpha = 0.8f; //透明度
        mWindowLayoutParams.width = viewW;
        mWindowLayoutParams.height = viewH;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    /**
     * 更新镜像的位置
     */
    private void onDragItem(int moveX, int moveY) {
        mWindowLayoutParams.x = moveX;
        mWindowLayoutParams.y = moveY;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams);
        //判断位置
        isOverItem(moveX, moveY);

        // TODO: 拖动到边缘屏幕自动滚动
    }


    /**
     * 与拖动距离最近的节点边框变色
     */
    private TreeViewHolder<?> isOverItem(int x, int y) {
        if (mDragImageView == null) {
            return null;
        }
        double shortS = 500;
        int centerX = 0;
        int centerY = 0;
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        for (NodeModel<?> node : adapter.getNodeList()) {
            View v = nodeViewMap.get(node).getView();
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            left = location[0];
            top = location[1];
            right = (left + v.getMeasuredWidth());
            bottom = (top + v.getMeasuredHeight());

            //求树节点的center坐标
            centerX = left + (right - left) / 2;
            centerY = top + (bottom - top) / 2;
            float disX = Math.abs(centerX - x);
            float disY = Math.abs(centerY - y);
            double s = Math.sqrt(disX * disX + disY * disY);

            //找到直线最短距离的节点targetNode
            if (s < shortS) {
                shortS = s;
                targetNode = node;
            }
            //超出最大距离
            if (shortS > 200) {
                targetNode = null;
                lastNode = null;
            }
            //绘制两点连线
            //drawInfo.getCanvas().drawLine(x, y, centerX, centerY, customPaint(Color.BLUE));
        }

        //绘制红色边框
        if (targetNode != null) {
            View v = nodeViewMap.get(targetNode).getView();
            if (!targetNode.equals(lastNode)) {
                Log.e(TAG, "绘制 targetNode = " + v.getLeft() + "," + v.getTop() + "," + v.getRight() + "," + v.getBottom() + " |  " + targetNode.getValue().toString());
                drawInfo.getCanvas().drawRect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom(), customPaint(Color.RED));
            }
            lastNode = targetNode;
        }
        invalidate();
        return null;
    }

    /**
     * 从界面上面移动拖动镜像
     */
    private void removeDragImage() {
        if (mDragImageView != null) {
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
            if (dragMoveListener != null) {
                dragMoveListener.onMove(sourceNode, targetNode);
            }
            sourceNode = null;
            targetNode = null;
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
        if (nodeModel == null || nodeViewMap == null) {
            return null;
        }
        return nodeViewMap.get(nodeModel);
    }

    @Override
    public void onDataSetChange() {
        mTreeModel = adapter.getTreeModel();
        removeAllViews();
        if (mTreeModel != null) {
            nodeViewMap = new HashMap<>(getChildCount());
            addNoteViews();
            mTreeModel.calculateTreeNodesDeep();
        }
    }

    @Override
    public void onItemViewChange(NodeModel<?> nodeModel) {

    }


    /**
     * 树缩放回调
     *
     * @param scale
     */
    public void treeScaleChange(float scale) {
        this.treeScale = scale;
        //Log.e(TAG, "双指缩放系数 = " + scale);
    }

}
