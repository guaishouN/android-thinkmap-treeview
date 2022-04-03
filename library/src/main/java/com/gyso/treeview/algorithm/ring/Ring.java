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

public class Ring {
    public static final String TAG = Ring.class.getSimpleName();
    private final static Map<TreeModel<?>, Ring> RING_MAP = new HashMap<>();
    private final PointF center = new PointF();
    private final TreeModel<?> model;
    private final Map<NodeModel<?>, PointF> nodeModelPointFMap = new HashMap<>();
    private final Map<NodeModel<?>, Double> nodeModelAngleMap = new HashMap<>();
    private final Map<NodeModel<?>, Double> piePartBaseAngleMap = new HashMap<>();
    protected SparseIntArray floorStart = new SparseIntArray(200);
    private final AtomicBoolean isGenPositionOK = new AtomicBoolean(true);
    private final Map<NodeModel<?>, AtomicInteger> multiMap = new HashMap<>();
    private final AtomicInteger multi = new AtomicInteger(1);
    private Ring(TreeModel<?> model) {
        this.model = model;
    }
    public static Ring getInstance(TreeModel<?> model){
        if(model==null){
            return null;
        }
        Ring ring = RING_MAP.get(model);
        if(ring==null){
            ring = new Ring(model);
            RING_MAP.put(model,ring);
        }
        return ring;
    }
    public Ring setCenter(float x, float y) {
        center.x = x;
        center.y = y;
        TreeViewLog.e(TAG,"center["+x+","+y+"]");
        return this;
    }

    public Ring setFloorStart(SparseIntArray floorStart) {
        this.floorStart = floorStart;
        return this;
    }

    public boolean isGenPositionOK(){
        return isGenPositionOK.get();
    }

    public Map<NodeModel<?>, PointF> genPositions() {
        if (model == null) {
            return null;
        }
        NodeModel<?> rootNode = model.getRootNode();
        nodeModelPointFMap.clear();
        nodeModelPointFMap.put(rootNode, new PointF(center.y, center.x));
        isGenPositionOK.set(true);
        int leafCount = rootNode.leafCount;
        LinkedList<? extends NodeModel<?>> rootNodeChildNodes = rootNode.getChildNodes();
        if(leafCount == 0 || rootNodeChildNodes.isEmpty()){
            return nodeModelPointFMap;
        }

        //keep in pie center
        double deltaAngle = 2f*Math.PI / (leafCount+multi.get());
        double pieAngle = 2f*Math.PI / leafCount;
        double sumAngleDelta = 0;
        double sumAnglePie = 0;
        for (NodeModel<?> rootChild : rootNodeChildNodes) {
            int count = rootChild.leafCount;
            double da ,dp;
            if(count<2){
                da = sumAngleDelta+deltaAngle/2;
                dp = sumAnglePie +pieAngle/2;
                sumAngleDelta += deltaAngle;
                sumAnglePie += pieAngle;
            }else{
                da = sumAngleDelta+deltaAngle*(count-1)/2f;
                dp = sumAnglePie + pieAngle*(count-1)/2f;
                sumAngleDelta += deltaAngle*(count-1);
                sumAnglePie += pieAngle*(count-1);
            }
            piePartBaseAngleMap.put(rootChild,dp-da);
        }

        //doTraversalNodes
        model.doTraversalNodes((ITraversal<NodeModel<?>>) next -> {
            if(next.equals(rootNode)){
                return;
            }
            TreeViewLog.e(TAG,next+" -gyso");
            NodeModel<?> nextParentNode = next.getParentNode();
            if(nextParentNode !=null && !nextParentNode.equals(rootNode)){
                piePartBaseAngleMap.put(next,piePartBaseAngleMap.get(nextParentNode));
            }
            PointF pointF = nodeModelPointFMap.get(next);
            if (pointF == null) {
                pointF = new PointF();
                int deep = next.deep;
                int floor = next.floor;
                double angle = deltaAngle * deep;
                LinkedList<? extends NodeModel<?>> childNodes = next.getChildNodes();
                if(!childNodes.isEmpty()){
                    int count = next.leafCount;
                    if(count>=2){
                        double d = deltaAngle*(count-1)/2f;
                        angle += d;
                    }
                }
                Double centerPieAngle = piePartBaseAngleMap.get(next);
                if(centerPieAngle!=null){
                    angle += centerPieAngle;
                }

                float radius = floorStart.get(floor);
                TreeViewLog.e(TAG,"radius["+radius+"]angle["+angle+"]");
                pointF.x = (float) (radius * Math.sin(angle))+center.y;
                pointF.y = (float) (radius * Math.cos(angle))+center.x;
                nodeModelPointFMap.put(next, pointF);
                nodeModelAngleMap.put(next,angle);

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
                    if(l1 <= p2r){
                        //no good layout request layout
                        isGenPositionOK.set(false);
                        int d = leafCount/6;
                        multi.addAndGet(d==0?1:d);
                        TreeViewLog.e(TAG,"Calculate ring position false!!!");
                    }
                }
            }
        });
        if(isGenPositionOK.get()){
            multi.set(1);
        }
        TreeViewLog.e(TAG,"isGenPositionOK["+isGenPositionOK+"]multi["+multi.get()+"]deltaAngle["+deltaAngle+"]nodeModelPointFMap{"+nodeModelPointFMap+"");
        return nodeModelPointFMap;
    }
}