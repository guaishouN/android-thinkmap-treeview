package com.gyso.treeview.layout;

import android.content.Context;
import android.graphics.Point;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.ViewBox;

import java.util.LinkedList;

/**
 * xw guaishouN
 * qq 674149099@qq.com
 */
public class BoxHorizonLeftAndRightLayoutManager extends BoxRightTreeLayoutManager {
    private static final String TAG = BoxHorizonLeftAndRightLayoutManager.class.getSimpleName();
    private final Point translateOfLowIndexPart = new Point();
    private final Point translateOfHighIndexPart = new Point();
    public BoxHorizonLeftAndRightLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_HORIZON_LEFT_AND_RIGHT;
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode,
                                    View currentNodeView,
                                    ViewBox finalLocation,
                                    TreeViewContainer treeViewContainer){
        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
        }
    }

    @Override
    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer) {

        super.onManagerFinishLayoutAllNodes(treeViewContainer);
    }

    private void calculateTranslate(TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        LinkedList<? extends NodeModel<?>> rootNodes = mTreeModel.getChildNodes();
        LinkedList<? extends NodeModel<?>> rootNodeChildNodes;
        if(rootNodes.size()==1){
            rootNodeChildNodes = rootNodes.get(0).getChildNodes();
        }else{
            rootNodeChildNodes = rootNodes;
        }

        int divider = rootNodeChildNodes.size()/2;
        int count = 0;
        int minLowIndex,maxLowIndex,minHighIndex,maxHighIndex;
        minLowIndex = minHighIndex = Integer.MAX_VALUE;
        maxLowIndex= maxHighIndex = Integer.MIN_VALUE;

        for (NodeModel<?> currentNode : rootNodeChildNodes) {
            TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
            View currentNodeView = currentHolder == null ? null : currentHolder.getView();
            if (currentNodeView == null) {
                throw new NullPointerException(" currentNodeView can not be null");
            }
            int left =currentNodeView.getLeft();
            int top = currentNodeView.getTop();
            int currentHeight = currentNodeView.getMeasuredHeight();
            int currentWidth = currentNodeView.getMeasuredWidth();
            if(count<divider){
                minLowIndex = Math.min(minLowIndex,top);
                maxLowIndex = Math.max(maxLowIndex, top+currentHeight);
            }else{
                minHighIndex = Math.min(minHighIndex,top);
                maxHighIndex = Math.max(maxHighIndex, top+currentHeight);
            }
            count++;
        }

        int centerX = fixedViewBox.getWidth()/2;
        int centerY = fixedViewBox.getHeight()/2;

        translateOfLowIndexPart.x  = centerX;
        translateOfLowIndexPart.y  = centerY - (maxLowIndex+minLowIndex)/2;

        translateOfHighIndexPart.x = -centerX;
        translateOfHighIndexPart.y = centerY - (maxHighIndex+minHighIndex)/2;
    }
}
