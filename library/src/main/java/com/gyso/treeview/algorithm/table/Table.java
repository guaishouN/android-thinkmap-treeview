package com.gyso.treeview.algorithm.table;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.UiThread;

import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.TreeViewLog;

import java.lang.annotation.Retention;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

public class Table {
    public static final String TAG = Table.class.getSimpleName();
    /**
     * default
     */
    public static final int LOOSE_TABLE = 0;
    public static final int COMPACT_TABLE = 1;
    private int maxDeep =0;
    private int minDeep =0;

    @Retention(SOURCE)
    @IntDef({LOOSE_TABLE,COMPACT_TABLE})
    public @interface TableLayoutAlgorithmType {}
    public<T> void reconstruction(TreeModel<T> treeModel, @TableLayoutAlgorithmType int tableLayoutAlgorithm){
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
            TreeViewLog.e(TAG,"calculateTreeNodesDeepLoose--->"+cur.toString());
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
            cur.deep = deepSum.get(cur.floor,0);
            recordDeep(cur);
            deepSum.put(cur.floor,cur.deep+1);
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        compactTable(treeModel);
        TreeViewLog.e(TAG,"calculateTreeNodesDeepCompact end");
    }

    private<T> void compactTable(TreeModel<T> treeModel){
        //calculate final deep
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        LinkedList<NodeModel<T>> hasDealNode  = new LinkedList<>();
        NodeModel<T> rootNode = treeModel.getRootNode();
        SparseArray<LinkedList<NodeModel<T>>> arrayByFloor = treeModel.getArrayByFloor();
        deque.add(rootNode);
        SparseIntArray deepSum = new SparseIntArray();
        while (!deque.isEmpty()) {
            NodeModel<T> cur = deque.poll();
            if(cur == null){
                continue;
            }
            int peerMidDeep;
            int parentDeep;

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
                                next.deep +=d;
                                recordDeep(next);
                                //next's parent fit center
                                NodeModel<?> np = next.getParentNode();
                                int nSum=0;
                                if(np!=null){
                                    LinkedList<? extends NodeModel<?>> npChildNodes = np.getChildNodes();
                                    for(NodeModel<?> nPeer:npChildNodes ){
                                        nSum +=nPeer.deep;
                                    }
                                    np.deep = nSum/npChildNodes.size();
                                    recordDeep(np);
                                }
                            });
                        }else{
                            final int d = parentDeep - peerMidDeep;
                            //peers' mid move to parent
                            LinkedList<NodeModel<T>> nodeModels = arrayByFloor.get(cur.floor);
                            int md = cur.deep;
                            for (NodeModel<T> afterMe:nodeModels) {
                                if(afterMe.deep >= md){
                                    afterMe.deep += d;
                                    recordDeep(afterMe);
                                }
                            }
                        }
                    }
                }
            }
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            recordDeep(cur);
            deepSum.put(cur.floor,cur.deep+1);
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
    }

    private<T> void fromRootToMyUpRight(TreeModel<T> treeModel,final NodeModel<T> node, ITraversal<NodeModel<T>> traversal){
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
            int markDeep= modelSparseArray.get(tmpNode.floor);
            if(markDeep>tmpNode.deep){
                continue;
            }
            if(tmpNode.floor>=node.floor){
                break;
            }
            if (traversal != null) {
                traversal.next(tmpNode);
            }
            if(tmpNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = tmpNode.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        if (traversal != null) {
            traversal.finish();
        }
    }

    public<T> void recordDeep(NodeModel<T> node) {
        if(node==null){
            return;
        }
        maxDeep = Math.max(node.deep, maxDeep);
        minDeep = Math.min(node.deep, minDeep);
    }

    public int getMinDeep(){
        return minDeep;
    }

    public int getMaxDeep(){
        return maxDeep;
    }
}
