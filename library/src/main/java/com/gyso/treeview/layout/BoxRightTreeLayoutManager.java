package com.gyso.treeview.layout;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.algorithm.table.Table;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * guaishouN xw 674149099@qq.com
 */
public class BoxRightTreeLayoutManager extends TreeLayoutManager {
    private static final String TAG = BoxRightTreeLayoutManager.class.getSimpleName();
    private final Deque<NodeModel<?>> parentsStack = new ArrayDeque<>();
    private final Set<NodeModel<?>> childrenSet = new HashSet<>();
    private final Map<NodeModel<?>,ViewBox> nodeToBoxMap = new HashMap<>();

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
    public void performMeasure(TreeViewContainer treeViewContainer) {
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            mContentViewBox.clear();
            nodeToBoxMap.clear();
            parentsStack.clear();
            childrenSet.clear();
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    Log.d(TAG, "performMeasure:"+next);
                    if(!next.childNodes.isEmpty()){
                        parentsStack.add(next);
                    }else{
                        childrenSet.add(next);
                    }
                    measure(next, treeViewContainer);
                }

                @Override
                public void finish() {
                    while (!parentsStack.isEmpty()){
                        NodeModel<?> oneParent = parentsStack.pollFirst();
                        if(childrenSet.containsAll(oneParent.childNodes)){
                            layoutByBox(oneParent,treeViewContainer);
                            childrenSet.add(oneParent);
                            childrenSet.removeAll(oneParent.childNodes);
                        }else{
                            parentsStack.addLast(oneParent);
                        }
                    }
                    ViewBox rootBox = nodeToBoxMap.get(mTreeModel.getRootNode());
                    mContentViewBox.setValues(rootBox);
                    onManagerFinishMeasureAllNodes(treeViewContainer);
                }
            };
            //deep traversal
            mTreeModel.doTraversalNodes(traversal,false);
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
        mFixedDx = paddingBox.top+(fixedViewBox.getWidth()-mContentViewBox.getWidth())/2;
        mFixedDy = paddingBox.left+(fixedViewBox.getHeight()-mContentViewBox.getHeight())/2;
    }

    /**
     * set the padding box
     * @param treeViewContainer tree view
     */
    protected void getPadding(TreeViewContainer treeViewContainer) {
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
        int curW = currentNodeView.getMeasuredWidth();
        int curH = currentNodeView.getMeasuredHeight();
        ViewBox viewBox = nodeToBoxMap.get(node);
        if(viewBox==null){
            viewBox=new ViewBox(0, 0, curH, curW);
            nodeToBoxMap.put(node,viewBox);
        }
        viewBox.clear();
        viewBox.right = curW;
        viewBox.bottom = curH;
    }

    @Override
    public void performLayout(final TreeViewContainer treeViewContainer) {

        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            ITraversal<NodeModel<?>> traversal = new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    layoutNodes(next, treeViewContainer);
                }

                @Override
                public void finish() {
                    onManagerFinishLayoutAllNodes(treeViewContainer);
                }
            };
            mTreeModel.doTraversalNodes(traversal,false);
        }
    }

    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    private void layoutByBox(NodeModel<?> parentNode, TreeViewContainer treeViewContainer){
        TreeViewHolder<?> parentHolder = treeViewContainer.getTreeViewHolder(parentNode);
        View parentNodeView =  parentHolder==null?null:parentHolder.getView();
        if(parentNodeView==null){
            throw new NullPointerException(" parentNodeView can not be null");
        }
        ViewBox parentLocationBox = nodeToBoxMap.get(parentNode);
        int maxChildWidth = 0;
        int sumHeight = 0;
        for(NodeModel<?> childNode:parentNode.childNodes){
            ViewBox childLocationBox = nodeToBoxMap.get(childNode);
            maxChildWidth = Math.max(maxChildWidth,childLocationBox.getWidth());
            int childWidth = childLocationBox.getWidth();
            int childHeight = childLocationBox.getHeight();
            childLocationBox.top = sumHeight;
            childLocationBox.left = spaceParentToChild*2 + parentLocationBox.getWidth();
            childLocationBox.bottom = childLocationBox.top+childHeight;
            childLocationBox.right = childLocationBox.left+childWidth;
            nodeToBoxMap.put(childNode,childLocationBox);
            sumHeight += childLocationBox.getHeight()+spacePeerToPeer;
        }
        sumHeight -= spacePeerToPeer;
        int delta = (parentLocationBox.getHeight()-sumHeight)/2;
        if(delta>0){
             ViewBox deltaBox = new ViewBox(delta,0,delta,0);
            for(NodeModel<?> childNode:parentNode.childNodes){
                ViewBox childLocationBox = nodeToBoxMap.get(childNode);
                ViewBox newLocation = childLocationBox.add(deltaBox);
                nodeToBoxMap.put(childNode,newLocation);
            }
        }
        parentLocationBox.right = spaceParentToChild*2 + parentLocationBox.getWidth() + maxChildWidth;
        parentLocationBox.bottom = Math.max(parentLocationBox.getHeight(),sumHeight);
        nodeToBoxMap.put(parentNode,parentLocationBox);
    }

    private void layoutNodes(NodeModel<?> currentNode, TreeViewContainer treeViewContainer){
         TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();

        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        ViewBox viewBox = nodeToBoxMap.get(currentNode);
        int centerFix = Math.max(viewBox.getHeight(),currentHeight)-currentHeight;
        int top  = mFixedDy+viewBox.top+centerFix/2;
        int left = mFixedDx+viewBox.left;
        NodeModel<?> pNode = currentNode.parentNode;
        while(pNode!=null){
            ViewBox upViewBox = nodeToBoxMap.get(pNode);
            top += upViewBox.top;
            left += upViewBox.left;
            pNode = pNode.parentNode;
        }

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
