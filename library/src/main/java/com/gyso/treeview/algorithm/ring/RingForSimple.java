package com.gyso.treeview.algorithm.ring;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.SparseIntArray;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.TreeViewLog;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RingForSimple  {
    public static final String TAG = RingForSimple.class.getSimpleName();
    private final static Map<TreeModel<?>, RingForSimple> RING_MAP = new HashMap<>();
    private final PointF center = new PointF();
    private final TreeModel<?> model;
    private final Map<NodeModel<?>, PointF> nodeModelPointFMap = new HashMap<>();
    private final Map<NodeModel<?>, Double> nodeModelAngleMap = new HashMap<>();
    protected SparseIntArray floorStart = new SparseIntArray(200);
    private final AtomicInteger multi = new AtomicInteger(1);
    private int maxDeep;
    private final Map<NodeModel<?>, Point> nodeWidthMap = new HashMap<>();
    private RingForSimple(TreeModel<?> model) {
        this.model = model;
    }
    public static RingForSimple getInstance(TreeModel<?> model){
        if(model==null){
            return null;
        }
        RingForSimple ring = RING_MAP.get(model);
        if(ring==null){
            ring = new RingForSimple(model);
            RING_MAP.put(model,ring);
        }
        return ring;
    }
    public RingForSimple setCenter(float x, float y) {
        center.x = x;
        center.y = y;
        TreeViewLog.e(TAG,"center["+x+","+y+"]");
        return this;
    }

    public RingForSimple setFloorStart(SparseIntArray floorStart) {
        this.floorStart = floorStart;
        return this;
    }

    public void reconstruction(TreeModel<?> mTreeModel) {
        TreeViewLog.e(TAG,"reconstruction start");
        nodeWidthMap.clear();
        Deque<NodeModel> deque = new ArrayDeque<>();
        NodeModel rootNode = mTreeModel.getRootNode();
        deque.add(rootNode);
        SparseIntArray deepSum = new SparseIntArray();
        //calculate base deep
        while (!deque.isEmpty()) {
            NodeModel cur = deque.poll();
            if(cur==null){
                return;
            }
            cur.deep = deepSum.get(cur.floor,0);
            record(cur);
            NodeModel tmp = cur.parentNode;
            while (!tmp.equals(rootNode)){
                record(tmp);
                tmp = tmp.parentNode;
            }
            deepSum.put(cur.floor, cur.deep+1);
            LinkedList childNodes = cur.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
    }

    private <T> void record(NodeModel<T> node) {
        if (node == null) {
            return;
        }
        Point point = nodeWidthMap.get(node);
        if(point==null){
            point = new Point();
            nodeWidthMap.put(node, point);
        }
        maxDeep = Math.max(node.deep, maxDeep);
        point.x= Math.max(node.deep, point.x);
        point.y = Math.min(node.deep, point.y);
    }

    public Map<NodeModel<?>, PointF> genPositions() {
        if (model == null) {
            return null;
        }
        NodeModel<?> rootNode = model.getRootNode();
        nodeModelPointFMap.clear();
        nodeModelPointFMap.put(rootNode, new PointF(center.y, center.x));
        int leafCount = maxDeep;
        LinkedList<? extends NodeModel<?>> rootNodeChildNodes = rootNode.getChildNodes();
        if(leafCount == 0 || rootNodeChildNodes.isEmpty()){
            return nodeModelPointFMap;
        }

        //keep in pie center
        double pieAngle = 2f*Math.PI / leafCount;

        //doTraversalNodes
        model.doTraversalNodes((ITraversal<NodeModel<?>>) next -> {
            if(next.equals(rootNode)){
                return;
            }
            PointF pointF = new PointF();
            int deep = next.deep;
            int floor = next.floor;
            double angle = pieAngle * deep;
            LinkedList<? extends NodeModel<?>> childNodes = next.getChildNodes();
            if(!childNodes.isEmpty()){
                Point point = nodeWidthMap.get(next);
                int count = point.x-point.y;
                if(count>=2){
                    double d = pieAngle*(count-1)/2f;
                    angle += d;
                }
            }
            float radius = floorStart.get(floor);
            TreeViewLog.e(TAG,"radius["+radius+"]angle["+angle+"]");
            pointF.x = (float) (radius * Math.sin(angle))+center.y;
            pointF.y = (float) (radius * Math.cos(angle))+center.x;
            nodeModelPointFMap.put(next, pointF);
            nodeModelAngleMap.put(next,angle);
        });
        return nodeModelPointFMap;
    }
}