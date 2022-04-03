package com.gyso.treeview.algorithm.table;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import androidx.annotation.IntDef;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.TreeViewLog;

import java.lang.annotation.Retention;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Table {
    public static final String TAG = Table.class.getSimpleName();
    /**
     * default
     */
    public static final int LOOSE_TABLE = 0;
    public static final int COMPACT_TABLE = 1;
    private int maxDeep =0;
    private int minDeep =0;
    private final Map<TableKey,NodeModel<?>> tableRecordMap = new ConcurrentHashMap<>();
    private final LinkedList<NodeModel<?>>  shouldCheckNodes = new LinkedList<>();
    private final SparseArray<NodeModel<?>> floorRightLast = new SparseArray<>();
    @Retention(SOURCE)
    @IntDef({LOOSE_TABLE,COMPACT_TABLE})
    public @interface TableLayoutAlgorithmType {}
    public<T> void reconstruction(TreeModel<T> treeModel, @TableLayoutAlgorithmType int tableLayoutAlgorithm){
        tableRecordMap.clear();
        floorRightLast.clear();
        shouldCheckNodes.clear();
        maxDeep = Integer.MIN_VALUE;
        minDeep = Integer.MAX_VALUE;
        if(tableLayoutAlgorithm==COMPACT_TABLE){
            calculateTreeNodesDeepCompact(treeModel);
        }else{
            calculateTreeNodesDeepLoose(treeModel);
        }
        treeModel.setMinDeep(minDeep);
        treeModel.setMaxDeep(maxDeep);
    }
    /**
     * calculate the deep of all tree nodes  by loose
     * deep has sort 0--->n
     * Fastest
     * 所有节点位置不大于同辈所需位置
     */
    private<T> void calculateTreeNodesDeepLoose(TreeModel<T> treeModel){
        TreeViewLog.e(TAG,"calculateTreeNodesDeepLoose start");
        Stack<NodeModel<T>> stack = new Stack<>();
        NodeModel<T> rootNode = treeModel.getRootNode();
        stack.add(rootNode);
        while (!stack.isEmpty()) {
            NodeModel<T> cur = stack.pop();
            NodeModel<T> parentNode = cur.getParentNode();
            cur.deep = 0;
            //if has peer, peer.deep+peer.leafCount+1/*peer self*/
            //if has no peer, parentNode.deep;
            if(parentNode!=null){
                int indexOfCur = parentNode.childNodes.indexOf(cur);
                if(indexOfCur>0){
                    NodeModel<T> prePeer = parentNode.childNodes.get(indexOfCur - 1);
                    cur.deep += prePeer.deep;
                    cur.deep += (prePeer.leafCount ==0?1:prePeer.leafCount);
                }else{
                    cur.deep = parentNode.deep;
                }
            }
            record(cur);
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            for (int i = childNodes.size()-1; i >=0; i--) {
                stack.add(childNodes.get(i));
            }
        }
        TreeViewLog.e(TAG,"calculateTreeNodesDeepLoose end");
    }



    /**
     * calculate the deep of all tree nodes  by compact way
     * deep has sort 0--->n
     * **********移动节点步骤
     * 1 按floor自上而下紧密排列
     * 2 按deep自左向右紧密排列
     * 3 按floor，deep遍历
     * 4 如果是父节点位置小于所有子节点中心位置，中心点到到根节点的分支都右移
     * 5 如果是所有子节点中心位置小于父节点位置，则所有子节点中心点移动到父节点位置，并把子节点的右侧所有节点都移动同样距离
     *
     * *******删除空白格子的步骤
     * 1 记录Table(floor,deep) 并且 Map
     * 2 整棵树的最右侧叶子分支不处理
     * 3 每个最右侧叶子的同辈叶子不处理
     * 4 同辈的叶子仅仅处理最右侧的叶子分支
     * 5 判断是不是左右侧叶子的同辈都是叶子，是则选为目标叶子
     * 6 目标叶子的右侧必需有空格
     * 7 每次移动节点都要计算父节点的位置
     * 8 分支的所有节点向右移动
     * 9 遇到分支移动父节点时冲突则停止移动
     */
    private <T> void calculateTreeNodesDeepCompact(TreeModel<T> treeModel){
        TreeViewLog.e(TAG,"calculateTreeNodesDeepCompact start");
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> rootNode = treeModel.getRootNode();
        deque.add(rootNode);
        SparseIntArray deepSum = new SparseIntArray();
        //calculate base deep
        while (!deque.isEmpty()) {
            NodeModel<T> cur = deque.poll();
            if(cur==null){
                return;
            }
            cur.deep = deepSum.get(cur.floor,0);
            record(cur);
            deepSum.put(cur.floor,cur.deep+1);
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        compactTable(treeModel);
        //rootNode.traverseIncludeSelf(node -> tableRecordMap.put(new TableKey(node.floor,node.deep),node));
        TreeViewLog.e(TAG,"calculateTreeNodesDeepCompact end");
    }

    private<T> void compactTable(TreeModel<T> treeModel){
        //calculate final deep
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> rootNode = treeModel.getRootNode();
        SparseArray<LinkedList<NodeModel>> arrayByFloor = treeModel.getArrayByFloor();
        deque.add(rootNode);
        SparseIntArray deepSum = new SparseIntArray();
        while (!deque.isEmpty()) {
            NodeModel<T> cur = deque.poll();
            if(cur == null){
                continue;
            }
            int peerMidDeep;
            int parentDeep;
            if(cur.toString().contains("04")){
                Log.d(TAG, "compactTable: ");
            }
            //deal parent and me
            NodeModel<T> parentNode = cur.getParentNode();
            if(parentNode!=null){
                LinkedList<NodeModel<T>> peers = parentNode.getChildNodes();
                if(!peers.isEmpty()){
                    int sum=0;
                    for(NodeModel<T> peer: peers){
                        sum +=peer.deep;
                    }
                    peerMidDeep=sum/peers.size();
                    parentDeep = parentNode.deep;
                    if(parentDeep!=peerMidDeep){
                        if (parentDeep<peerMidDeep){
                            //parent all left move to peers' mid
                            final int d = peerMidDeep-parentDeep;
                            fromRootToMyUpRight(treeModel,cur, next -> {
                                Log.d(TAG, "compactTable: ---"+next);
                                if(next.toString().contains("04")){
                                    Log.d(TAG, "compactTable: ");
                                }
                                next.deep +=d;
                                record(next);

                                //next's parent fit center
                                NodeModel<T> np = next.getParentNode();
                                if(np!=null){
                                    np.traverseBranch(nn -> {
                                        int nSum = 0;
                                        LinkedList<? extends NodeModel<?>> nnChildNodes = nn.getChildNodes();
                                        for(NodeModel<?> nPeer:nnChildNodes ){
                                            nSum = nSum +nPeer.deep;
                                        }
                                        nn.deep = nSum /nnChildNodes.size();
                                        if(nn.toString().contains("04")){
                                            Log.d(TAG, "compactTable: ");
                                        }
                                        record(nn);
                                    });
                                }
                            });
                        }else{
                            final int d = parentDeep - peerMidDeep;
                            //peers' mid move to parent
                            LinkedList<NodeModel> nodeModels = arrayByFloor.get(cur.floor);
                            if(nodeModels!=null){
                                int md = cur.deep;
                                for (NodeModel<T> afterMe:nodeModels) {
                                    if(afterMe.deep >= md){
                                        afterMe.deep += d;
                                        if(afterMe.toString().contains("04")){
                                            Log.d(TAG, "compactTable: ");
                                        }
                                        record(afterMe);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            record(cur);
            deepSum.put(cur.floor,cur.deep+1);
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
    }

    private<T> void fromRootToMyUpRight(TreeModel<T> treeModel,final NodeModel<T> node, ITraversal<NodeModel<T>> traversal){
        if(node==null){
            return;
        }
        //mark line
        NodeModel<?> markNode = node;
        SparseIntArray modelSparseArray = new SparseIntArray();
        while (markNode!=null){
            modelSparseArray.put(markNode.floor,markNode.deep);
            markNode = markNode.parentNode;
        }
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> tmpNode = treeModel.getRootNode();
        deque.add(tmpNode);
        while (!deque.isEmpty()) {
            tmpNode = deque.poll();
            if(tmpNode==null ){
                continue;
            }
            if(tmpNode.floor>=node.floor){
                break;
            }
            int markDeep= modelSparseArray.get(tmpNode.floor);
            if(tmpNode.deep>=markDeep || tmpNode.deep>= node.deep){
                if (traversal != null) {
                    traversal.next(tmpNode);
                }
                LinkedList<NodeModel<T>> childNodes = tmpNode.getChildNodes();
                if (childNodes.size() > 0) {
                    deque.addAll(childNodes);
                }
            }
        }
        if (traversal != null) {
            traversal.finish();
        }
    }

    private <T> void record(NodeModel<T> node) {
        if(node==null){
            return;
        }
        maxDeep = Math.max(node.deep, maxDeep);
        minDeep = Math.min(node.deep, minDeep);
        NodeModel<?> model = floorRightLast.get(node.floor);
        if(model==null){
            floorRightLast.put(node.floor,node);
        }else{
            if(model.deep <= node.deep){
                floorRightLast.put(node.floor,node);
            }
        }
        if(node.getChildNodes().isEmpty()&& !shouldCheckNodes.contains(node)){
            shouldCheckNodes.add(node);
            NodeModel<?> nodeModel = floorRightLast.get(node.floor);
            if(nodeModel!=null && node.deep == nodeModel.deep){
                shouldCheckNodes.remove(node);
            }
        }
    }

    private boolean nextDeepCellIsEmpty(NodeModel<?> node){
        NodeModel<?> nodeModel = tableRecordMap.get(new TableKey(node.floor, node.deep+1));
        if(node.equals(nodeModel)){
            return true;
        }
        return nodeModel==null;
    }

    private boolean isFloorImpact(NodeModel<?> node){
        NodeModel<?> nodeModel = tableRecordMap.get(new TableKey(node.floor+1, node.deep));
        if(node.equals(nodeModel)){
            return false;
        }
        return nodeModel!=null;
    }
}
