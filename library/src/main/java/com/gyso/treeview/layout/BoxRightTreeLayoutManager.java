package com.gyso.treeview.layout;

import android.content.Context;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.ViewBox;

import java.util.HashMap;
import java.util.Map;

/**
 * guaishouN xw 674149099@qq.com
 */
public class BoxRightTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = BoxRightTreeLayoutManager.class.getSimpleName();
    private Map<NodeModel<?>,Integer> nodeSumHeight = new HashMap<>();
    public BoxRightTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public void calculateByLayoutAlgorithm(TreeModel<?> mTreeModel) {

    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_HORIZON_RIGHT;
    }

    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            nodeSumHeight.clear();
            ViewBox contentBox = mTreeModel.onMeasure(this,treeViewContainer);;
            mContentViewBox.setValues(contentBox);
            onManagerFinishMeasureAllNodes(treeViewContainer);
        }
    }

    @Override
    public ViewBox onMeasureNode(NodeModel<?> node, TreeViewContainer treeViewContainer) {
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(node);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int curW = currentNodeView.getMeasuredWidth();
        int curH = currentNodeView.getMeasuredHeight();
        ViewBox viewBox = node.getViewBox();
        viewBox.clear();
        viewBox.right = curW;
        viewBox.bottom = curH;
        return viewBox;
    }


    @Override
    public void onMeasureNodeBox(NodeModel<?> parentNode, NodeModel<?> childNode, TreeViewContainer container){
        TreeViewHolder<?> parentHolder = treeViewContainer.getTreeViewHolder(parentNode);
        View parentNodeView =  parentHolder==null?null:parentHolder.getView();
        if(parentNodeView==null){
            throw new NullPointerException(" parentNodeView can not be null");
        }
        TreeViewHolder<?> childHolder = treeViewContainer.getTreeViewHolder(childNode);
        View childNodeView =  childHolder==null?null:childHolder.getView();

        if(childNodeView==null){
            throw new NullPointerException(" childNodeView can not be null");
        }
        ViewBox parentBox = parentNode.getViewBox();
        ViewBox childBox = childNode.getViewBox();

        Integer sumHeight = nodeSumHeight.get(parentNode);
        if(sumHeight==null){
            sumHeight = 0;
        }

        int childWidth = childBox.getWidth();
        int childHeight = childBox.getHeight();
        childBox.top = sumHeight;
        childBox.left = spaceParentToChild*2 + parentNodeView.getMeasuredWidth();
        childBox.bottom = childBox.top+childHeight;
        childBox.right = childBox.left+childWidth;


        sumHeight += spacePeerToPeer + childBox.getHeight();
        parentBox.top    = 0;
        parentBox.left   = 0;
        parentBox.right  = Math.max(parentBox.right,spaceParentToChild*2 + childBox.getWidth()+parentNodeView.getMeasuredWidth());
        parentBox.bottom = Math.max(spacePeerToPeer + parentNodeView.getMeasuredHeight(),sumHeight);
        nodeSumHeight.put(parentNode, sumHeight);
    }

    @Override
    public void onManagerFinishMeasureAllNodes(TreeViewContainer treeViewContainer) {
        getPadding(treeViewContainer);
        mContentViewBox.bottom += (paddingBox.bottom+paddingBox.top);
        mContentViewBox.right  += (paddingBox.left+paddingBox.right);
        fixedViewBox.setValues(mContentViewBox);
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
        mFixedDx = paddingBox.top+(fixedViewBox.getWidth()-mContentViewBox.getWidth())/2;
        mFixedDy = paddingBox.left+(fixedViewBox.getHeight()-mContentViewBox.getHeight())/2;
    }

    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mTreeModel.onLayout(new ViewBox(),this,treeViewContainer);
            onManagerFinishLayoutAllNodes(treeViewContainer);
        }
    }

    @Override
    public ViewBox onLayoutNodeBox(ViewBox parentLocationBox, NodeModel<?> tNodeModel, TreeViewContainer container){
        return parentLocationBox.add(tNodeModel.getViewBox());
    }

    @Override
    public void onLayoutNode(ViewBox baseLocationBox, NodeModel<?> currentNode, TreeViewContainer container) {
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();

        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        ViewBox viewBox = currentNode.getViewBox();
        int centerFix = Math.max(viewBox.getHeight(),currentHeight)-currentHeight;
        int top  = mFixedDy+viewBox.top+centerFix/2+baseLocationBox.top;
        int left = mFixedDx+viewBox.left+baseLocationBox.left;
        int bottom = top+currentHeight;
        int right = left+currentWidth;

        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        onManagerLayoutNode(currentNode, currentNodeView, finalLocation, treeViewContainer);
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
    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer){
        layoutAnimate(treeViewContainer);
    }
}
