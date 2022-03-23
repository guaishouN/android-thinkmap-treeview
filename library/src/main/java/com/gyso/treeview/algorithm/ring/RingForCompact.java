package com.gyso.treeview.algorithm.ring;

import android.graphics.PointF;
import android.util.SparseIntArray;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.TreeViewLog;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RingForCompact {
    public static final String TAG = RingForCompact.class.getSimpleName();
    private final static Map<TreeModel<?>, RingForCompact> RING_MAP = new HashMap<>();
    private final PointF center = new PointF();
    private final TreeModel<?> model;
    private final Map<NodeModel<?>, PointF> nodeModelPointFMap = new HashMap<>();
    private final Map<NodeModel<?>, Double> nodeModelAngleMap = new HashMap<>();
    private final Map<NodeModel<?>, Double> nodeModelNewRadius = new HashMap<>();
    private final Map<NodeModel<?>, Integer> nodeModelNewDeep = new HashMap<>();
    protected SparseIntArray floorStart = new SparseIntArray(200);
    private double minAngle = 0;
    private double maxAngle = 0;
    private int minDeep = 0;
    private int maxDeep = 0;

    private RingForCompact(TreeModel<?> model) {
        this.model = model;
    }
    public static RingForCompact getInstance(TreeModel<?> model){
        if(model==null){
            return null;
        }
        RingForCompact ring = RING_MAP.get(model);
        if(ring==null){
            ring = new RingForCompact(model);
            RING_MAP.put(model,ring);
        }
        return ring;
    }
    public RingForCompact setCenter(float x, float y) {
        center.x = x;
        center.y = y;
        TreeViewLog.e(TAG,"center["+x+","+y+"]");
        return this;
    }

    public RingForCompact setFloorStart(SparseIntArray floorStart) {
        this.floorStart = floorStart;
        return this;
    }


    public Map<NodeModel<?>, PointF> genPositions() {
        if (model == null) {
            return null;
        }
        NodeModel<?> rootNode = model.getRootNode();
        nodeModelPointFMap.clear();
        nodeModelAngleMap.clear();
        nodeModelNewRadius.clear();
        nodeModelNewDeep.clear();
        minAngle =2f*Math.PI;
        maxAngle = 0;
        minDeep = 0;
        maxDeep =0;
        nodeModelPointFMap.put(rootNode, new PointF(center.y, center.x));
        int pieCount = model.getMaxDeep()- model.getMinDeep();
        LinkedList<? extends NodeModel<?>> rootNodeChildNodes = rootNode.getChildNodes();
        if(pieCount == 0 || rootNodeChildNodes.isEmpty()){
            return nodeModelPointFMap;
        }
        //keep in pie center
        double pieAngle = 2f*Math.PI / pieCount;
        //doTraversalNodes
        model.doTraversalNodes((ITraversal<NodeModel<?>>) next -> {
            if(next.equals(rootNode)){
                return;
            }
            TreeViewLog.e(TAG,next+" -gyso");
            NodeModel<?> nextParentNode = next.getParentNode();
            PointF pointF = nodeModelPointFMap.get(next);
            if (pointF == null) {
                pointF = new PointF();
                int deep = next.deep;
                int floor = next.floor;
                double angle = pieAngle * deep;
                float radius = floorStart.get(floor);
                TreeViewLog.e(TAG,"radius["+radius+"]angle["+angle+"]");
                pointF.x = (float) (radius * Math.sin(angle))+center.y;
                pointF.y = (float) (radius * Math.cos(angle))+center.x;
                nodeModelPointFMap.put(next, pointF);
                nodeModelAngleMap.put(next,angle);
                nodeModelNewRadius.put(next, (double)radius);
                recordMinMaxAngle(next);
                //keep acute angle for parent
                if(nextParentNode !=null && !nextParentNode.equals(rootNode)){
                    PointF parentPosition = nodeModelPointFMap.get(nextParentNode);
                    PointF rootPosition = nodeModelPointFMap.get(rootNode);
                    PointF currentPosition = nodeModelPointFMap.get(next);
                    double p2r = Math.hypot(parentPosition.x-rootPosition.x,parentPosition.y-rootPosition.y);
                    double c2r = Math.hypot(currentPosition.x-rootPosition.x,currentPosition.y-rootPosition.y);
                    double dAngle = Math.abs(nodeModelAngleMap.get(nextParentNode)-nodeModelAngleMap.get(next));
                    double l1 = c2r*Math.abs(Math.cos(dAngle));
                    TreeViewLog.e(TAG,"p2r["+p2r+"]c2r["+c2r+"]dAngle["+dAngle+"]Math.sin(dAngle)["+Math.sin(dAngle)+"]l1["+l1+"]l1 <= p2r["+(l1 <= p2r)+"]");
                    //if(false){
                    if(l1 < p2r){
                        TreeViewLog.e(TAG,"no acute children layout, should compact to acute!!");
                        //Math.PI for layout all children
                        LinkedList<? extends NodeModel<?>> childNodes = nextParentNode.getChildNodes();
                        double childAngle = Math.PI / childNodes.size();
                        float childRadius = (radius -floorStart.get(nextParentNode.floor))*2;
                        double h,w;
                        int count =0;
                        for (NodeModel<?> child :childNodes) {
                            double sumAngle = Math.PI/2-(count+1/2f)*childAngle;
                            h =  childRadius * Math.sin(sumAngle);
                            w = childRadius * Math.cos(sumAngle)+floorStart.get(nextParentNode.floor);
                            double de = Math.atan2(h,w)+nodeModelAngleMap.get(nextParentNode);
                            double nR = Math.sqrt(h*h+w*w);
                            PointF nP = new PointF();
                            nP.x = (float) (nR * Math.sin(de))+center.y;
                            nP.y = (float) (nR * Math.cos(de))+center.x;
                            nodeModelPointFMap.put(child, nP);
                            nodeModelAngleMap.put(child, de);
                            nodeModelNewRadius.put(child, (double)nR);
                            nodeModelNewDeep.put(child,nextParentNode.deep);
                            recordMinMaxAngle(child);
                            count++;
                        }
                    }
                }
            }
        });
        double spaceAngle = 2*Math.PI-(maxAngle-minAngle);
        double deepSpace = maxDeep - minDeep;
        //   if(false){
        if(spaceAngle>(Math.PI/8)){
            double s = spaceAngle/pieCount;
            double d = deepSpace/pieCount;
            model.doTraversalNodes((ITraversal<NodeModel<?>>) next -> {
                if(!next.equals(rootNode)){
                    nodeModelPointFMap.get(next);
                    PointF pointF = new PointF();
                    int deep = nodeModelNewDeep.get(next)==null?next.deep:nodeModelNewDeep.get(next);
                    double angle = nodeModelAngleMap.get(next)+s* deep*(1+d);
                    double radius = nodeModelNewRadius.get(next);
                    pointF.x = (float) (radius * Math.sin(angle))+center.y;
                    pointF.y = (float) (radius * Math.cos(angle))+center.x;
                    nodeModelPointFMap.put(next, pointF);
                    nodeModelAngleMap.put(next,angle);
                }
            });
        }
        return nodeModelPointFMap;
    }

    public void recordMinMaxAngle(NodeModel<?> node){
        Double angle = nodeModelAngleMap.get(node);
        minAngle = Math.min(minAngle,angle);
        maxAngle = Math.max(maxAngle,angle);
        minDeep = Math.min(minDeep,node.deep);
        maxDeep = Math.max(maxDeep,node.deep);
    }
}