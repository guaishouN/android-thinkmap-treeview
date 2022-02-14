package com.gyso.treeview.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.algorithm.force.FLink;
import com.gyso.treeview.algorithm.force.FNode;
import com.gyso.treeview.algorithm.force.Force;
import com.gyso.treeview.algorithm.force.ForceListener;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.ViewBox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ForceDirectedTreeLayoutManager extends TreeLayoutManager implements ForceListener {
    private final Force force;
    private FNode node;
    private List<FLink> targetLinks = new ArrayList<>();
    private List<FLink> sourceLinks = new ArrayList<>();
    private List<FNode> selectedNodes = new ArrayList<>();
    private final Handler handler;
    private TreeViewContainer treeViewContainer;
    public ForceDirectedTreeLayoutManager(Context context, int spaceParentToChild, int spacePeerToPeer) {
        super(context, spaceParentToChild, spacePeerToPeer);
        handler = new Handler(Looper.getMainLooper());
        force = new Force(this);
        init();
    }

    private void init() {
        handler.post(() ->
                force.setStrength(0.7f)
                    .setFriction(0.8f)
                    .setDistance(150)
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
            final ArrayList<NodeModel<?>> tmp = new ArrayList<>();
            mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                @Override
                public void next(NodeModel<?> next) {
                    if(!tmp.contains(next)){
                        //deal node
                        FNode fNode = new FNode(next.toString());
                        tmp.add(next);
                        //deal children
                        LinkedList<? extends NodeModel<?>> childNodes = next.getChildNodes();
                        if(!childNodes.isEmpty()){
                            for (NodeModel<?> child:childNodes){
                                if(child!=null){
                                    //buill
                                }
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
    @Override
    public void performMeasure(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        setUpData();
        if(treeViewContainer!=null){
            final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
            if (mTreeModel != null) {
                mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                    @Override
                    public void next(NodeModel<?> next) {

                    }

                    @Override
                    public void finish() {

                    }
                });
            }
        }
    }

    @Override
    public void performLayout(TreeViewContainer treeViewContainer) {
        this.treeViewContainer = treeViewContainer;
        if(treeViewContainer!=null && force!=null){
            int width = treeViewContainer.getWidth();
            int height = treeViewContainer.getHeight();
            if(width<1 || height<1){
                return;
            }
            force.setSize(width,height);
        }
        if(treeViewContainer!=null){
            final TreeModel<?> mTreeModel = treeViewContainer.getTreeModel();
            if (mTreeModel != null) {
                mTreeModel.doTraversalNodes(new ITraversal<NodeModel<?>>() {
                    @Override
                    public void next(NodeModel<?> next) {

                    }

                    @Override
                    public void finish() {

                    }
                });
            }
        }
    }

    @Override
    public void refresh() {
        if(treeViewContainer!=null){
            performLayout(treeViewContainer);
        }
    }
}
