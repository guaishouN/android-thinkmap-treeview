package com.gyso.treeview.layout;

import android.content.Context;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.ViewBox;

public class UpTreeLayoutManager  extends DownTreeLayoutManager {
    private static final String TAG = UpTreeLayoutManager.class.getSimpleName();
    private boolean isJustCalculate;
    public UpTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {
        isJustCalculate = true;
        super.performLayout(treeViewContainer);
        isJustCalculate = false;
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            final int cy= fixedViewBox.getHeight()/2;
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    mirrorByCy(next,treeViewContainer,cy);
                }
                @Override
                public void finish() {
                    onManagerFinishLayoutAllNodes(treeViewContainer);
                }
            });
        }
    }

    private void mirrorByCy(NodeModel<?> currentNode, TreeViewContainer treeViewContainer,int centerY){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView = currentHolder == null ? null : currentHolder.getView();
        if (currentNodeView == null) {
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();
        int left =currentNodeView.getLeft();
        int right = left+currentWidth;
        int top = centerY*2- currentNodeView.getTop();
        int bottom = top+currentHeight;
        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        onManagerLayoutNode(currentNode, currentNodeView, finalLocation, treeViewContainer);
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode, View currentNodeView, ViewBox finalLocation, TreeViewContainer treeViewContainer) {
        super.onManagerLayoutNode(currentNode, currentNodeView, finalLocation, treeViewContainer);
    }

    @Override
    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer) {
        if(!isJustCalculate){
            super.onManagerFinishLayoutAllNodes(treeViewContainer);
        }
    }
}
