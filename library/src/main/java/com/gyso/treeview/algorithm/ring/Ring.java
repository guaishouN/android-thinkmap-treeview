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
    protected SparseIntArray floorStart = new SparseIntArray(200);
    private final AtomicBoolean isGenPositionOK = new AtomicBoolean(true);
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
        double deltaAngle = 2f*Math.PI / (leafCount+multi.get());
        model.doTraversalNodes((ITraversal<NodeModel<?>>) next -> {
            if(next.equals(rootNode)){
                return;
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
                    if(count>2){
                        double d = count%2 == 0?(deltaAngle*(count-1)/2f):(deltaAngle*(count-deep)/2f);
                        angle += d;
                    }
                }
                //double angle = deltaAngle * (deep);
                float radius = floorStart.get(floor);
                TreeViewLog.e(TAG,"radius["+radius+"]angle["+angle+"]");
                pointF.x = (float) (radius * Math.sin(angle))+center.y;
                pointF.y = (float) (radius * Math.cos(angle))+center.x;
                nodeModelPointFMap.put(next, pointF);
                nodeModelAngleMap.put(next,angle);
                //keep acute angle for parent
                NodeModel<?> parentNode = next.getParentNode();
                if(parentNode!=null && !parentNode.equals(rootNode)){
                    PointF parentPosition = nodeModelPointFMap.get(parentNode);
                    PointF rootPosition = nodeModelPointFMap.get(rootNode);
                    PointF currentPosition = nodeModelPointFMap.get(next);
                    double p2r = Math.hypot(parentPosition.x-rootPosition.x,parentPosition.y-rootPosition.y);
                    double c2r = Math.hypot(currentPosition.x-rootPosition.x,currentPosition.y-rootPosition.y);
                    double dAngle = Math.abs(nodeModelAngleMap.get(parentNode)-nodeModelAngleMap.get(next));
                    double l1 = c2r*Math.abs(Math.cos(dAngle));
                    TreeViewLog.e(TAG,"p2r["+p2r+"]c2r["+c2r+"]dAngle["+dAngle+"]Math.sin(dAngle)["+Math.sin(dAngle)+"]l1["+l1+"]l1 <= p2r["+(l1 <= p2r)+"]");
                    if(l1 <= p2r){
                        //no good layout request layout
                        isGenPositionOK.set(false);
                        int d = leafCount/3;
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