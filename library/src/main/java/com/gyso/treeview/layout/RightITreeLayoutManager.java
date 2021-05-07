package com.gyso.treeview.layout;

import android.util.SparseIntArray;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.ViewBox;

/**
 * guaishouN xw 674149099@qq.com
 */
public class RightITreeLayoutManager implements ITreeLayoutManager {
    private static final String TAG = RightITreeLayoutManager.class.getSimpleName();

    /**
     * the content padding, unit is dp;
     */
    private static final int DEFAULT_CONTENT_PADDING_DP = 100;

    private final ViewBox mContentViewBox;
    private int mDy;
    private int mDx;

    /**
     * the fixedViewBox means that the fixedViewBox's width/height is the same as the given viewPort's.
     */
    private final ViewBox fixedViewBox;
    private int mFixedDx;
    private int mFixedDy;

    /**
     * content padding box
     */
    private final ViewBox paddingBox;

    /**
     * the max Width in the same floor
     */
    private SparseIntArray floorMaxWidth = new SparseIntArray(10);
    /**
     * the max height in the same deep
     */
    private SparseIntArray deepMaxHeight = new SparseIntArray(200);
    private int winHeight;
    private int winWidth;

    public RightITreeLayoutManager(int dx, int dy) {
        mContentViewBox = new ViewBox();
        fixedViewBox = new ViewBox();
        paddingBox = new ViewBox();
        this.mDx = dx;
        this.mDy = dy;
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            floorMaxWidth.clear();
            deepMaxHeight.clear();
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    measure(next, treeViewContainer);
                }

                @Override
                public void finish() {
                    getPadding(treeViewContainer);
                    mContentViewBox.bottom += (paddingBox.bottom+paddingBox.top);
                    mContentViewBox.right  += (paddingBox.left+paddingBox.right);
                    fixedViewBox.setValues(mContentViewBox.top,mContentViewBox.left,mContentViewBox.right,mContentViewBox.bottom);
                    if(winHeight == 0 || winWidth==0){
                        return;
                    }
                    float scale = 1f*winWidth/winHeight;
                    float wr = 1f* mContentViewBox.getWidth()/winWidth;
                    float hr = 1f* mContentViewBox.getHeight()/winHeight;
                    if(wr>=hr){
                        float bh =  mContentViewBox.getWidth()/scale;
                        fixedViewBox.bottom = (int)bh;
                    }else{
                        float bw =  mContentViewBox.getHeight()*scale;
                        fixedViewBox.right = (int)bw;
                    }
                    mFixedDx = (fixedViewBox.getWidth()-mContentViewBox.getWidth())/2;
                    mFixedDy = (fixedViewBox.getHeight()-mContentViewBox.getHeight())/2;
                }
            };
            mTreeModel.doTraversalNodes(traversal);
        }
    }

    /**
     * set the padding box
     * @param treeViewContainer tree view
     */
    private void getPadding(TreeViewContainer treeViewContainer) {
        if(treeViewContainer.getPaddingStart()>0){
            paddingBox.setValues(
                    treeViewContainer.getPaddingTop(),
                    treeViewContainer.getPaddingLeft(),
                    treeViewContainer.getPaddingRight(),
                    treeViewContainer.getPaddingBottom());
        }else{
            int padding = DensityUtils.dp2px(treeViewContainer.getContext(),DEFAULT_CONTENT_PADDING_DP);
            paddingBox.setValues(padding,padding,padding,padding);
        }
    }

    private void measure(NodeModel<?> node, TreeViewContainer treeViewContainer) {
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(node);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int preMaxW = floorMaxWidth.get(node.floor);
        int curW = currentNodeView.getMeasuredWidth();
        if(preMaxW < curW){
            floorMaxWidth.put(node.floor,curW);
            int delta = mDx+curW-preMaxW;
            mContentViewBox.right += delta;
        }

        int preMaxH = deepMaxHeight.get(node.deep);
        int curH = currentNodeView.getMeasuredHeight();
        if(preMaxH < curH){
            deepMaxHeight.put(node.deep,curH);
            int delta = mDy+curH-preMaxH;
            mContentViewBox.bottom += delta;
        }
    }

    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    layoutNodes(next, treeViewContainer);
                }

                @Override
                public void finish() {

                }
            });
        }
    }

    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    @Override
    public void setViewport(int winHeight, int winWidth) {
        this.winHeight =winHeight;
        this.winWidth = winWidth;
    }

    private void layoutNodes(NodeModel<?> currentNode, TreeViewContainer treeViewContainer){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        NodeModel<?> parentNode = currentNode.getParentNode();
        TreeViewHolder<?> treeViewHolder = treeViewContainer.getTreeViewHolder(parentNode);
        View parentNodeView = treeViewHolder==null?null:treeViewHolder.getView();
        int deep = currentNode.deep;
        int leafCount = currentNode.leafCount;

        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int height = deepMaxHeight.get(currentNode.deep, currentNodeView.getMeasuredHeight());
        int width = floorMaxWidth.get(currentNode.floor, currentNodeView.getMeasuredWidth());

        int deltaHeight = 0;
        if(leafCount>1){
            deltaHeight = (leafCount-1)*(height+mDy)/2;
        }

        int top  =deep*(height+mDy) + deltaHeight + mFixedDy+ paddingBox.top;
        int left = mDx+(parentNodeView==null?0:parentNodeView.getRight())+(currentNode.floor==0?(mFixedDx+paddingBox.left):0);
        int bottom = top+height;
        int right = left+width;

        currentNodeView.layout(left,top,right,bottom);
    }
}
