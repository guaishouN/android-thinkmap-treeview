package com.gyso.treeview.model;

import android.util.Log;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * manager the tree data nodes
 * guaishouN 674149099@qq.com
 */
public class TreeModel<T> implements Serializable {
    private static final String TAG = TreeModel.class.getSimpleName();
    /**
     * the root for the tree
     */
    private NodeModel<T> rootNode;
    private SparseArray<LinkedList<NodeModel<T>>> arrayByFloor = new SparseArray<>(10);
    private transient ITraversal<NodeModel<?>> ITraversal;
    public TreeModel(NodeModel<T> rootNode) {
        this.rootNode = rootNode;
    }
    /**
     * add the node in some father node
     */
    @SafeVarargs
    public final void addNode(NodeModel<T> parent, NodeModel<T>... childNodes) {
        if(parent!=null&&childNodes!=null && childNodes.length>0){
            List<NodeModel<T>> nodeModels = Arrays.asList(childNodes);
            parent.addChildNodes(nodeModels);
            List<NodeModel<T>> floorList = getFloorList(nodeModels.get(0).floor);
            floorList.addAll(nodeModels);
        }
    }

    /**
     * remove
     * @param parent p node
     * @param childNode c node
     */
    public void removeNode(NodeModel<T> parent, NodeModel<T> childNode) {
        if(parent!=null&&childNode!=null){
            parent.removeChildNode(childNode);
            List<NodeModel<T>> floorList = getFloorList(childNode.floor);
            floorList.remove(childNode);
        }
    }

    public NodeModel<T> getRootNode() {
        return rootNode;
    }

    /**
     * calculate the deep of all tree nodes
     */
    public void calculateTreeNodesDeep(){
        Stack<NodeModel<T>> stack = new Stack<>();
        NodeModel<T> rootNode = getRootNode();
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
                    cur.deep += (prePeer.leafCount==0?1:prePeer.leafCount);
                }else{
                    cur.deep = parentNode.deep;
                }
            }
            Log.e(TAG,"calculateTreeNodesDeep--->"+cur.toString());
            LinkedList<NodeModel<T>> childNodes = cur.getChildNodes();
            for (int i = childNodes.size()-1; i >=0; i--) {
                stack.add(childNodes.get(i));
            }
        }
    }

    /**
     *child nodes will ergodic in the last
     */
    public void ergodicTreeByQueue() {
        Deque<NodeModel<T>> queue = new ArrayDeque<>();
        NodeModel<T> rootNode = getRootNode();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            rootNode = queue.poll();
            if (ITraversal != null) {
                ITraversal.next(rootNode);
            }
            if(rootNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = rootNode.getChildNodes();
            if (childNodes.size() > 0) {
                queue.addAll(childNodes);
            }
        }
        if (ITraversal != null) {
            ITraversal.finish();
        }
    }

    /**
     * @param floor  level
     * @return all nodes in the same floor
     */
    private List<NodeModel<T>> getFloorList(int floor){
        LinkedList<NodeModel<T>> nodeModels = arrayByFloor.get(floor);
        if(nodeModels==null){
            nodeModels = new LinkedList<>();
            arrayByFloor.put(floor,nodeModels);
        }
        return nodeModels;
    }
    /**
     * when ergodic this tree, it will call back on {@link ITraversal)}
     * @param ITraversal node
     */
    public void doTraversalNodes(ITraversal<NodeModel<?>> ITraversal) {
        this.ITraversal = ITraversal;
        ergodicTreeByQueue();
    }
}
