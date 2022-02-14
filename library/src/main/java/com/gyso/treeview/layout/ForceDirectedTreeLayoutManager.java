package com.gyso.treeview.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.algorithm.force.FLink;
import com.gyso.treeview.algorithm.force.FNode;
import com.gyso.treeview.algorithm.force.Force;
import com.gyso.treeview.algorithm.force.ForceListener;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ForceDirectedTreeLayoutManager extends TreeLayoutManager implements ForceListener {
    private final Force force;
    private FNode node;
    private final Handler handler;
    private TreeViewContainer treeViewContainer;
    final Map<NodeModel<?>,FNode > fNodeCache = new HashMap<>();
    public ForceDirectedTreeLayoutManager(Context context, BaseLine baseline) {
        super(context, 150, 150, baseline);
        handler = new Handler(Looper.getMainLooper());
        force = new Force(this);
        init();
    }

    private void init() {
        handler.post(() ->
                force.setStrength(0.7f)
                    .setFriction(0.8f)
                    .setDistance(50)
                    .setCharge(-320f)
                    .setGravity(0.1f)
                    .setTheta(0.8f)
                    .setAlpha(0.2f));
    }

    @Override
    public ViewBox getTreeLayoutBox() {
        return fixedViewBox;
    }

    @Override
    public int getTreeLayoutType() {
        return LAYOUT_TYPE_FORCE_DIRECTED;
    }

    private void setUpData() {
        if(treeViewContainer==null){
            return;
        }
        final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
        if (mTreeModel != null) {
            final ArrayList<FNode> nodes = new ArrayList<>();
            final ArrayList<FLink> links= new ArrayList<>();
            fNodeCache.clear();
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    float radius = getViewRadius(next);
                    FNode parentFNode = fNodeCache.get(next);
                    if(parentFNode == null){
                        //deal node
                        parentFNode = new FNode(next.toString(),radius,next.floor);
                        fNodeCache.put(next,parentFNode);
                        nodes.add(parentFNode);
                    }
                    //deal children
                    LinkedList<? extends NodeModel<?>> childNodes = next.getChildNodes();
                    if(!childNodes.isEmpty()){
                        for (NodeModel<?> child:childNodes){
                            if(child!=null){
                                FNode childFNode = fNodeCache.get(child);
                                if(childFNode==null){
                                    float r = getViewRadius(child);
                                    childFNode = new FNode(child.toString(),radius,child.floor);
                                    fNodeCache.put(child,childFNode);
                                    nodes.add(childFNode);
                                }
                                links.add(new FLink(parentFNode,childFNode));
                            }
                        }
                    }
                }

                @Override
                public void finish() {
                    force.setNodes(nodes)
                            .setLinks(links)
                            .start();
                }
            });
        }
    }

    private float getViewRadius(@NonNull NodeModel<?> nodeMode){
        TreeViewHolder<?> viewHolder = treeViewContainer.getTreeViewHolder(nodeMode);
        View theView =  viewHolder==null?null:viewHolder.getView();
        assert theView != null;
        float w = theView.getMeasuredWidth()/2f;
        float h = theView.getMeasuredHeight()/2f;
        float radius = (float)Math.sqrt(w * w + h * h);
        return radius;
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        setUpData();
        if(treeViewContainer!=null){
            final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
            if (mTreeModel != null) {
                mContentViewBox.clear();
                fixedViewBox.clear();
                force.setSize(winWidth,winHeight);
                setUpData();
                fixedViewBox.setValues(0,0,winWidth*3,winHeight*3);
                mContentViewBox.setValues(0,0,winWidth*3,winHeight*3);
            }
        }
    }

    @Override
    public void performLayout(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        if(treeViewContainer!=null){
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
    }
    private void layoutNodes(NodeModel<?> currentNode, TreeViewContainer treeViewContainer){
        TreeViewLog.e(TAG,"onLayout: "+currentNode);
        FNode fNode = fNodeCache.get(currentNode);
        if(fNode ==null){
            return;
        }
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }
        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        int top  = (int)fNode.x;
        int left = (int)fNode.y;
        int bottom = top+currentHeight;
        int right = left+currentWidth;
        currentNodeView.layout(left,top,right,bottom);
    }

    @Override
    public void refresh() {
        if(treeViewContainer!=null){
            performLayout(treeViewContainer);
        }
    }
}
