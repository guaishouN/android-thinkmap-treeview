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
 * xw guaishouN
 * qq 674149099@qq.com
 */
public class BoxDownTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = BoxDownTreeLayoutManager.class.getSimpleName();
    private Map<NodeModel<?>,Integer> nodeSumWidth = new HashMap<>();
    public BoxDownTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public void calculateByLayoutAlgorithm(TreeModel<?> mTreeModel) {

    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_VERTICAL_DOWN;
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
            nodeSumWidth.clear();
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
            return node.getViewBox();
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

        ViewBox parentBox = parentNode.getViewBox();
        ViewBox childBox = childNode.getViewBox();

        Integer sumwidth = nodeSumWidth.get(parentNode);
        if(sumwidth==null){
            sumwidth = 0;
        }
        int childWidth = childBox.getWidth();
        int childHeight = childBox.getHeight();

        if(parentNodeView==null){
            childBox.top = 0;
            childBox.left = sumwidth;
            childBox.bottom = childBox.top+childHeight;
            childBox.right = childBox.left+childWidth;

            parentBox.top    = 0;
            parentBox.left   = 0;
            parentBox.bottom = Math.max(parentBox.top,spaceParentToChild*2 + childBox.getHeight());
            sumwidth += spacePeerToPeer + childBox.getWidth();
            parentBox.right  = sumwidth;
            nodeSumWidth.put(parentNode, sumwidth);
            return;
        }

        childBox.top = spaceParentToChild*2 + parentNodeView.getMeasuredHeight();
        childBox.left = sumwidth;
        childBox.bottom = childBox.top+childHeight;
        childBox.right = childBox.left+childWidth;


        sumwidth += spacePeerToPeer + childBox.getWidth();
        parentBox.top    = 0;
        parentBox.left   = 0;
        parentBox.right  = Math.max(spacePeerToPeer + parentNodeView.getMeasuredWidth(),sumwidth);
        parentBox.bottom = Math.max(parentBox.bottom,spaceParentToChild*2 + childBox.getHeight()+parentNodeView.getMeasuredHeight());
        nodeSumWidth.put(parentNode, sumwidth);
        if(parentNode.childNodes.getLast()==childNode){
            sumwidth -= spacePeerToPeer;
            parentBox.right= Math.max(parentNodeView.getMeasuredWidth(),sumwidth);
            nodeSumWidth.put(parentNode, sumwidth);
            if(sumwidth < parentNodeView.getMeasuredWidth()){
                final int parentBoxFixed = (parentNodeView.getMeasuredWidth()-sumwidth)/2;
                parentNode.traverseDirectChildren(next->{
                    ViewBox nextBox = next.getViewBox();
                    nextBox.left    += parentBoxFixed;
                    nextBox.right += parentBoxFixed;
                });
            }
        }
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
        mViewportFixedDx = paddingBox.left+(fixedViewBox.getHeight()-mContentViewBox.getHeight())/2;
        mViewportFixedDy = paddingBox.top+(fixedViewBox.getWidth()-mContentViewBox.getWidth())/2;
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
            throw new NullPointerException("currentNodeView can not be null. #"+currentNode);
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        ViewBox viewBox = currentNode.getViewBox();
        int centerFix = Math.max(viewBox.getWidth(),currentWidth)-currentWidth;
        int top  = mViewportFixedDx +viewBox.top+baseLocationBox.top;
        int left = mViewportFixedDy + viewBox.left + centerFix/2+baseLocationBox.left;
        int bottom = top+currentHeight;
        int right = left+currentWidth;

        ViewBox finalLocation = new ViewBox(left,top,right,bottom);
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
