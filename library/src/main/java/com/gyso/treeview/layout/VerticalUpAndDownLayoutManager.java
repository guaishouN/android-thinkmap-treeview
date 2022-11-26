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

public class VerticalUpAndDownLayoutManager extends DownTreeLayoutManager {
    private static final String TAG = CompactVerticalUpAndDownLayoutManager.class.getSimpleName();
    private boolean isJustCalculate;
    public VerticalUpAndDownLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer, BaseLine baseline) {
        super(context, spaceParentToChild, spacePeerToPeer, baseline);
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_VERTICAL_DOWN_AND_UP;
    }

    @Override
    public void onManagerFinishMeasureAllNodes(TreeViewContainer treeViewContainer) {
        getPadding(treeViewContainer);
        extraDeltaY = mContentViewBox.bottom;
        mContentViewBox.bottom += (paddingBox.bottom+paddingBox.top)+extraDeltaY;
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
        mFixedDx = (fixedViewBox.getWidth()-mContentViewBox.getWidth())/2;
        mFixedDy = (fixedViewBox.getHeight()-mContentViewBox.getHeight())/2;

        //compute floor start position
        for (int i = 0; i <= floorMax.size(); i++) {
            int fn = (i == floorMax.size())?floorMax.size():floorMax.keyAt(i);
            int preStart = floorStart.get(fn - 1, 0);
            int preMax = floorMax.get(fn - 1, 0);
            int startPos = (fn==0?(mFixedDy + paddingBox.top):spaceParentToChild) + preStart + preMax;
            floorStart.put(fn,startPos);
        }

        //compute deep start position
        for (int i = 0; i <= deepMax.size(); i++) {
            int dn = (i == deepMax.size())?deepMax.size():deepMax.keyAt(i);
            int preStart = deepStart.get(dn - 1, 0);
            int preMax = deepMax.get(dn - 1, 0);
            int startPos = (dn==0?(mFixedDx + paddingBox.left):spacePeerToPeer) + preStart + preMax;
            deepStart.put(dn,startPos);
        }

        if(measureListener!=null){
            measureListener.onMeasureFinished();
        }
    }


    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {
        isJustCalculate = true;
        super.performLayout(treeViewContainer);
        isJustCalculate = false;
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            NodeModel<?> rootNode = mTreeModel.getRootNode();
            TreeViewHolder<?> rootNodeHolder = treeViewContainer.getTreeViewHolder(rootNode);
            View rootNodeView = rootNodeHolder == null ? null : rootNodeHolder.getView();
            if (rootNodeView == null) {
                throw new NullPointerException(" rootNodeView can not be null");
            }
            int rootCx = rootNodeView.getLeft()+rootNodeView.getMeasuredWidth()/2;
            int rootCy = rootNodeView.getTop()+rootNodeView.getMeasuredHeight()/2;
            //divide equally by two
            LinkedList<? extends NodeModel<?>> rootNodeChildNodes = rootNode.getChildNodes();
            Point divideDx = getDivideDx(rootNode, treeViewContainer);
            int centerAx = divideDx.x;
            int centerBx = divideDx.y;
            int divider = rootNodeChildNodes.size()/2;
            int count = 0;
            for (NodeModel<?> node : rootNodeChildNodes) {
                if(count<divider){
                    //move to mid
                    node.traverseIncludeSelf(n -> moveDx(n,treeViewContainer, (rootCx- centerAx)));
                }else{
                    //move to other side
                    node.traverseIncludeSelf(n -> mirrorByCxDy(n,treeViewContainer,rootCy, (rootCx - centerBx)));
                }
                count++;
            }
            onManagerFinishLayoutAllNodes(treeViewContainer);
        }
    }

    private Point getDivideDx(NodeModel<?> rootNode, TreeViewContainer treeViewContainer){
        LinkedList<? extends NodeModel<?>> rootNodeChildNodes = rootNode.getChildNodes();
        int divider = rootNodeChildNodes.size()/2;
        int count = 0;
        int minA,maxA,minB,maxB;
        minA = minB = Integer.MAX_VALUE;
        maxA= maxB = Integer.MIN_VALUE;
        for (NodeModel<?> currentNode : rootNodeChildNodes) {
            TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
            View currentNodeView = currentHolder == null ? null : currentHolder.getView();
            if (currentNodeView == null) {
                throw new NullPointerException(" currentNodeView can not be null");
            }
            int left =currentNodeView.getLeft();
            int top = currentNodeView.getTop();
            int currentHeight = currentNodeView.getMeasuredHeight();
            int currentWidth =  currentNodeView.getMeasuredWidth();
            if(count<divider){
                minA = Math.min(minA,left);
                maxA = Math.max(maxA, left+currentWidth);
            }else{
                minB = Math.min(minB,left);
                maxB = Math.max(maxB, left+currentWidth);
            }
            count++;
        }
        return new Point((maxA+minA)/2,(maxB+minB)/2);
    }

    private void moveDx(NodeModel<?> currentNode, TreeViewContainer treeViewContainer, int deltaX){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView = currentHolder == null ? null : currentHolder.getView();
        if (currentNodeView == null) {
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();
        int left =deltaX+ currentNodeView.getLeft();
        int right = left+currentWidth;
        int top = currentNodeView.getTop();
        int bottom = top+currentHeight;
        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        onManagerLayoutNode(currentNode, currentNodeView, finalLocation, treeViewContainer);
    }

    private void mirrorByCxDy(NodeModel<?> currentNode, TreeViewContainer treeViewContainer,int centerY, int deltaX){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView = currentHolder == null ? null : currentHolder.getView();
        if (currentNodeView == null) {
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();
        int left =deltaX+currentNodeView.getLeft();
        int right = left+currentWidth;
        int top = centerY*2- currentNodeView.getTop()-spaceParentToChild-currentHeight/2;
        int bottom = top+currentHeight;
        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        onManagerLayoutNode(currentNode, currentNodeView, finalLocation, treeViewContainer);
    }

    @Override
    public void onManagerLayoutNode(NodeModel<?> currentNode, View currentNodeView, ViewBox finalLocation, TreeViewContainer treeViewContainer) {
        if(isJustCalculate){
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
            return;
        }
        if (!layoutAnimatePrepare(currentNode, currentNodeView, finalLocation, treeViewContainer)) {
            currentNodeView.layout(finalLocation.left, finalLocation.top, finalLocation.right, finalLocation.bottom);
        }
    }

    @Override
    public void onManagerFinishLayoutAllNodes(TreeViewContainer treeViewContainer) {
        if(!isJustCalculate){
            super.onManagerFinishLayoutAllNodes(treeViewContainer);
        }
    }
}
