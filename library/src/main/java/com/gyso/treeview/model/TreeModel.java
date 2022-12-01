package com.gyso.treeview.model;

import android.util.SparseArray;

import com.gyso.treeview.TreeViewContainer;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.util.ViewBox;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * manager the tree data nodes
 * guaishouN 674149099@qq.com
 */
public class TreeModel<T> extends NodeModel<T> implements Serializable {
    private static final String TAG = TreeModel.class.getSimpleName();
    /**
     * the root for the tree
     */
    private NodeModel<T> rootNode;
    private SparseArray<LinkedList<NodeModel>> arrayByFloor = new SparseArray<>(10);
    private transient ITraversal<NodeModel<?>> iTraversal;
    private final HashSet<NodeModel<T>> rootNodeSet = new HashSet<>();
    private int maxDeep =0;
    private int minDeep =0;

    private boolean finishTraversal = false;

    /**
     * add the node in some father node
     * @param parent
     * @param childNodes
     */
    @SafeVarargs
    public final void addNode(NodeModel<?> parent, NodeModel<?>... childNodes) {
        if(parent!=null&&childNodes!=null && childNodes.length>0){
            parent.treeModel = this;
            List<NodeModel<T>> nodeModels = new LinkedList<>();
            for (int i = 0; i < childNodes.length; i++) {
                nodeModels.add((NodeModel<T>)childNodes[i]);
                childNodes[i].treeModel = this;
            }
            ((NodeModel<T>)parent).addChildNodes(nodeModels);
            for(NodeModel<?> child:childNodes){
                child.traverseIncludeSelf(next->{
                    next.floor = next.parentNode.floor+1;
                    List<NodeModel> floorList = getFloorList(next.floor);
                    floorList.add(next);
                });
            }
        }
    }

    /**
     * remove
     * @param parent p node
     * @param childNode c node
     */
    public void removeNode(NodeModel<?> parent, NodeModel<?> childNode) {
        if(parent!=null&&childNode!=null){
            parent.removeChildNode(childNode);
            childNode.traverseIncludeSelf(next->{
                List<NodeModel> nf = getFloorList(next.floor);
                nf.remove(next);
                next.floor = 0;
            });
        }
    }

    public NodeModel<T> getRootNode() {
        return rootNode;
    }


    /**
     *child nodes will ergodic in the last
     * 广度遍历
     * breadth search
     * For every floor , child nodes  has been  sort 0--->n;
     * And node will been display one by one floor.
     */
    private void ergodicTreeByFloor() {
        Deque<NodeModel<T>> deque = new ArrayDeque<>();
        NodeModel<T> rootNode = getRootNode();
        deque.add(rootNode);
        while (!deque.isEmpty()) {
            rootNode = deque.poll();
            if (iTraversal != null) {
                iTraversal.next(rootNode);
            }
            if(this.finishTraversal){
                break;
            }
            if(rootNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = rootNode.getChildNodes();
            if (childNodes.size() > 0) {
                deque.addAll(childNodes);
            }
        }
        if (iTraversal != null) {
            iTraversal.finish();
            this.finishTraversal = false;
        }
    }

    public SparseArray<LinkedList<NodeModel>> getArrayByFloor() {
        return arrayByFloor;
    }

    /**
     * @param floor  level
     * @return all nodes in the same floor
     */
    public List<NodeModel> getFloorList(int floor){
        LinkedList<NodeModel> nodeModels = arrayByFloor.get(floor);
        if(nodeModels==null){
            nodeModels = new LinkedList<>();
            arrayByFloor.put(floor,nodeModels);
        }
        return nodeModels;
    }

    public void setFinishTraversal(boolean finishTraversal) {
        this.finishTraversal = finishTraversal;
    }

    public void doTraversalNodes(ITraversal<NodeModel<?>> ITraversal){
        doTraversalNodes(ITraversal,true);
    }

    /**
     * when ergodic this tree, it will call back on {@link ITraversal)}
     * @param ITraversal node
     */
    public void doTraversalNodes(ITraversal<NodeModel<?>> ITraversal, boolean isOrderByFloor) {
        this.iTraversal = ITraversal;
        this.finishTraversal = false;
        if(isOrderByFloor){
            ergodicTreeByFloor();
        }else{
            ergodicTreeByDeep();
        }
    }

    /**
     *child nodes will ergodic by deep
     * 深度遍历
     * depth search
     * For every  child node list  has been  sort 0--->n;
     * And node will been display one then the children by deep until end.
     */
    private void ergodicTreeByDeep(){
        Stack<NodeModel<T>> stack = new Stack<>();
        NodeModel<T> rootNode = getRootNode();
        stack.add(rootNode);
        while (!stack.isEmpty()) {
            rootNode = stack.pop();
            if (iTraversal != null) {
                iTraversal.next(rootNode);
            }
            if(this.finishTraversal){
                break;
            }
            if(rootNode==null){
                continue;
            }
            LinkedList<NodeModel<T>> childNodes = rootNode.getChildNodes();
            if (childNodes.size() > 0) {
                stack.addAll(childNodes);
            }
        }
        if (iTraversal != null) {
            iTraversal.finish();
            this.finishTraversal = false;
        }
    }

    public int getMaxDeep() {
        return maxDeep;
    }

    public void setMaxDeep(int maxDeep) {
        this.maxDeep = maxDeep;
    }

    public int getMinDeep() {
        return minDeep;
    }

    public void setMinDeep(int minDeep) {
        this.minDeep = minDeep;
    }
}
