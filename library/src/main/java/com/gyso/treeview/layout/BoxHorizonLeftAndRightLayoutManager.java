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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * xw guaishouN
 * qq 674149099@qq.com
 */
public class BoxHorizonLeftAndRightLayoutManager extends BoxRightTreeLayoutManager {
    private static final String TAG = BoxHorizonLeftAndRightLayoutManager.class.getSimpleName();
    private final Point translateOfLowIndexPart = new Point();
    private final Point translateOfHighIndexPart = new Point();
    private final Set<NodeModel<?>> parentNodeOfLowIndexPart = new HashSet<>();
    private final Set<NodeModel<?>> parentNodeOfHighIndexPart = new HashSet<>();
    private final Set<NodeModel<?>> rootNodes = new HashSet<>();
    private final ViewBox tmpTranslateBox = new ViewBox();
    public BoxHorizonLeftAndRightLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_HORIZON_LEFT_AND_RIGHT;
    }

    @Override
    public void onManagerFinishMeasureAllNodes(TreeViewContainer treeViewContainer) {
        super.onManagerFinishMeasureAllNodes(treeViewContainer);
        calculateTranslate(treeViewContainer);
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode,
                                    View currentNodeView,
                                    ViewBox finalLocation,
                                    TreeViewContainer treeViewContainer){
        NodeModel<?> tmpParentNode = currentNode;
        tmpTranslateBox.clear();

        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
        }
    }

    private void changeNodeLayoutType(NodeModel<?> currentNode,TreeViewContainer treeViewContainer,int layoutType){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView = currentHolder == null ? null : currentHolder.getView();
        if (currentNodeView == null) {
            throw new NullPointerException(" currentNodeView can not be null");
        }
        currentHolder.setHolderLayoutType(layoutType);
    }

    private void calculateTranslate(TreeViewContainer treeViewContainer) {
        parentNodeOfLowIndexPart.clear();
        parentNodeOfHighIndexPart.clear();
        rootNodes.clear();
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        LinkedList<? extends NodeModel<?>> rootNodes = mTreeModel.getChildNodes();
        LinkedList<? extends NodeModel<?>> rootNodeChildNodes;
        ViewBox lowBox = new ViewBox();
        ViewBox highBox = new ViewBox();
        ViewBox oneRootNodeBox = new ViewBox();
        if(rootNodes.size()==1){
            rootNodeChildNodes = rootNodes.get(0).getChildNodes();
        }else{
            rootNodeChildNodes = rootNodes;
        }
        this.rootNodes.addAll(rootNodes);
        int divider = rootNodeChildNodes.size()/2;
        int count = 0;
        for (NodeModel<?> currentNode : rootNodeChildNodes) {
            if(count<divider){
                lowBox.extend(currentNode.viewBox);
            }else{
                highBox.extend(currentNode.viewBox);
            }
            count++;
        }

        int centerX = fixedViewBox.getWidth()/2;
        int centerY = fixedViewBox.getHeight()/2;

        translateOfLowIndexPart.x  = centerX;
        //translateOfLowIndexPart.y  = centerY - low/2;

        translateOfHighIndexPart.x = centerX;
        //translateOfHighIndexPart.y = centerY - high/2;
    }
}
