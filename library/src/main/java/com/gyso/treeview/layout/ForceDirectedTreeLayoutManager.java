package com.gyso.treeview.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import com.gyso.treeview.R;
import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.algorithm.force.FLink;
import com.gyso.treeview.algorithm.force.FNode;
import com.gyso.treeview.algorithm.force.Force;
import com.gyso.treeview.algorithm.force.ForceListener;
import com.gyso.treeview.algorithm.table.Table;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.TreeViewLog;
import com.gyso.treeview.util.ViewBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ForceDirectedTreeLayoutManager extends DownTreeLayoutManager implements ForceListener {
    private final static String TAG = ForceDirectedTreeLayoutManager.class.getSimpleName();
    private Force force;
    private final Handler handler;
    private TreeViewContainer treeViewContainer;
    final Map<NodeModel<?>,FNode > fNodeCache = new HashMap<>();
    public ForceDirectedTreeLayoutManager(Context context, BaseLine baseline) {
        super(context, 150, 150, baseline);
        handler = new Handler(Looper.getMainLooper());
        force = new Force(this);
        init();
    }

    @Override
    public void calculateByLayoutAlgorithm(TreeModel<?> mTreeModel) {
        new Table().reconstruction(mTreeModel,Table.COMPACT_TABLE);
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
                        setFNodePosition(parentFNode,next);
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
                                    childFNode = new FNode(child.toString(),r,child.floor);
                                    setFNodePosition(childFNode,child);
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

    private void setFNodePosition(FNode fNode, @NonNull NodeModel<?> nodeMode){
        TreeViewHolder<?> viewHolder = treeViewContainer.getTreeViewHolder(nodeMode);
        View theView =  viewHolder==null?null:viewHolder.getView();
        assert theView != null;
        fNode.x = theView.getTop();
        fNode.y = theView.getLeft();
        TreeViewLog.d(TAG, nodeMode.toString()+" ["+fNode.x+","+fNode.y+"] ");
    }

    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        if(treeViewContainer!=null){
            performMeasureAndListen(treeViewContainer, new TreeLayoutManager.MeasureListener(){
                @Override
                public void onMeasureChild(NodeModel<?> next) {
                    firstLayoutAfterMeasure(next);
                }

                @Override
                public void onMeasureFinished() {
                    setUpData();
                }
            });
        }
    }

    private void firstLayoutAfterMeasure(NodeModel<?> currentNode){
        TreeViewHolder<?> currentHolder = treeViewContainer.getTreeViewHolder(currentNode);
        View currentNodeView =  currentHolder==null?null:currentHolder.getView();
        int deep = currentNode.deep;
        int floor = currentNode.floor;
        int leafCount = currentNode.leafCount;

        if(currentNodeView==null){
            throw new NullPointerException(" currentNodeView can not be null");
        }

        int currentWidth = currentNodeView.getMeasuredWidth();
        int currentHeight = currentNodeView.getMeasuredHeight();

        int verticalCenterFix = Math.abs(currentWidth - deepMax.get(deep))/2;

        int deltaWidth = 0;
        if(leafCount>1){
            deltaWidth = (deepStart.get(deep + leafCount) - deepStart.get(deep)-currentWidth)/2-verticalCenterFix;
            deltaWidth -= spacePeerToPeer/2;
        }

        int top = floorStart.get(floor);
        int left  = deepStart.get(deep)+verticalCenterFix+deltaWidth;
        int bottom = top+currentHeight;
        int right = left+currentWidth;

        ViewBox finalLocation = new ViewBox(top, left, bottom, right);
        if(!layoutAnimatePrepare(currentNode,currentNodeView,finalLocation,treeViewContainer)){
            currentNodeView.layout(left,top,right,bottom);
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
                        layoutNodesByForceDirected(next, treeViewContainer);
                    }

                    @Override
                    public void finish() {

                    }
                });
            }
        }
    }

    private void layoutNodesByForceDirected(NodeModel<?> currentNode, TreeViewContainer treeViewContainer){
        //TreeViewLog.e(TAG,"onLayout: "+currentNode);
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
