package com.gyso.treeview.layout;

import android.content.Context;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.Baseline;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.ViewBox;

/**
 * guaishouN xw 674149099@qq.com
 */
public class RightTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = RightTreeLayoutManager.class.getSimpleName();

    public RightTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, Baseline baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_HORIZON_RIGHT;
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            floorMax.clear();
            deepMax.clear();
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
        int preMaxW = floorMax.get(node.floor);
        int curW = currentNodeView.getMeasuredWidth();
        if(preMaxW < curW){
            floorMax.put(node.floor,curW);
            int delta = spaceParentToChild +curW-preMaxW;
            mContentViewBox.right += delta;
        }

        int preMaxH = deepMax.get(node.deep);
        int curH = currentNodeView.getMeasuredHeight();
        if(preMaxH < curH){
            deepMax.put(node.deep,curH);
            int delta = spacePeerToPeer +curH-preMaxH;
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
        int height = deepMax.get(currentNode.deep, currentNodeView.getMeasuredHeight());
        int width = floorMax.get(currentNode.floor, currentNodeView.getMeasuredWidth());

        int deltaHeight = 0;
        if(leafCount>1){
            deltaHeight = (leafCount-1)*(height+ spacePeerToPeer)/2;
        }

        int top  =deep*(height+ spacePeerToPeer) + deltaHeight + mFixedDy+ paddingBox.top;
        int left = spaceParentToChild +(parentNodeView==null?0:parentNodeView.getRight())+(currentNode.floor==0?(mFixedDx+paddingBox.left):0);
        int bottom = top+height;
        int right = left+width;

        currentNodeView.layout(left,top,right,bottom);
    }
}
