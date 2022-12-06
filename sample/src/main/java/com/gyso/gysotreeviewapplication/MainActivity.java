package com.gyso.gysotreeviewapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.gyso.gysotreeviewapplication.base.Animal;
import com.gyso.gysotreeviewapplication.base.AnimalTreeViewAdapter;
import com.gyso.gysotreeviewapplication.databinding.ActivityMainBinding;
import com.gyso.treeview.TreeViewEditor;
import com.gyso.treeview.layout.BoxRightTreeLayoutManager;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.listener.TreeViewControlListener;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private final Stack<NodeModel<Animal>> removeCache = new Stack();
    private NodeModel<Animal> parentToEditChildren;
    private final AtomicInteger atomicInteger = new AtomicInteger();
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //demo init
        initWidgets();
    }

    /**
     * To use a tree view, you should do 6 steps as follows:
     *      1 customs adapter
     *
     *      2 configure layout manager. Space unit is dp.
     *      You can custom you line by extends {@link BaseLine}
     *
     *      3 view setting
     *
     *      4 nodes data setting
     *
     *      5 if you want to edit the map, then get and use and tree view editor
     *
     *      6 you own others jobs
     */
    private void initWidgets() {
        //1 customs adapter
        AnimalTreeViewAdapter adapter = new AnimalTreeViewAdapter();

        //2 configure layout manager; unit dp
        TreeLayoutManager treeLayoutManager = getTreeLayoutManager();

        //3 view setting
        binding.baseTreeView.setAdapter(adapter);
        binding.baseTreeView.setTreeLayoutManager(treeLayoutManager);

        //4 nodes data setting
        TreeModel<Animal> treeModel= setData();
        adapter.setTreeModel(treeModel);

        //5 get an editor. Note: an adapter must set before get an editor.
        final TreeViewEditor editor = binding.baseTreeView.getEditor();

        //6 you own others jobs by editor
        doYourOwnJobs(editor, adapter);
    }

    void doYourOwnJobs(TreeViewEditor editor, AnimalTreeViewAdapter adapter){
        //drag to move node
        binding.dragEditModeRd.setOnCheckedChangeListener((v, isChecked)->{
            editor.requestMoveNodeByDragging(isChecked);
        });

        //focus, means that tree view fill center in your window viewport
        binding.viewCenterBt.setOnClickListener(v->editor.focusMidLocation());

        //add some nodes
        binding.addNodesBt.setOnClickListener(v->{
            if(parentToEditChildren == null){
                Toast.makeText(this,"Ohs, your targetNode is null", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> a = parentToEditChildren.treeModel.createNode(new Animal(R.drawable.ic_10,"add-"+atomicInteger.getAndIncrement()));
            NodeModel<Animal> b = parentToEditChildren.treeModel.createNode(new Animal(R.drawable.ic_11,"add-"+atomicInteger.getAndIncrement()));
            NodeModel<Animal> c = parentToEditChildren.treeModel.createNode(new Animal(R.drawable.ic_14,"add-"+atomicInteger.getAndIncrement()));
            editor.addChildNodes(parentToEditChildren,a,b,c);

            //add to remove demo cache
            removeCache.push(parentToEditChildren);
            parentToEditChildren = b;
        });

        //remove node
        binding.removeNodeBt.setOnClickListener(v->{
            if(removeCache.isEmpty()){
                Toast.makeText(this,"Ohs, demo removeCache is empty now!! Try to add some nodes firstly!!", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> toRemoveNode = removeCache.pop();
            parentToEditChildren = toRemoveNode.getParentNode();
            editor.removeNode(toRemoveNode);
        });

        adapter.setOnItemListener((item, node)-> {
            Animal animal = node.getValue();
            Toast.makeText(this,"you click the head of "+animal,Toast.LENGTH_SHORT).show();
        });


        //treeView control listener
        final Object token = new Object();
        Runnable dismissRun = ()->{
            binding.scalePercent.setVisibility(View.GONE);
        };

        binding.baseTreeView.setTreeViewControlListener(new TreeViewControlListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onScaling(int state, int percent) {
                Log.e(TAG, "onScaling: "+state+"  "+percent);
                binding.scalePercent.setVisibility(View.VISIBLE);
                if(state == TreeViewControlListener.MAX_SCALE){
                    binding.scalePercent.setText("MAX");
                }else if(state == TreeViewControlListener.MIN_SCALE){
                    binding.scalePercent.setText("MIN");
                }else{
                    binding.scalePercent.setText(percent+"%");
                }
                handler.removeCallbacksAndMessages(token);
                handler.postAtTime(dismissRun,token,SystemClock.uptimeMillis()+2000);
            }

            @Override
            public void onDragMoveNodesHit(NodeModel<?> draggingNode, NodeModel<?> hittingNode, View draggingView, View hittingView) {
                Log.e(TAG, "onDragMoveNodesHit: dragging["+draggingNode+"]hittingNode["+hittingNode+"]");
            }
        });
    }

    /**
     * Box[XXX]TreeLayoutManagers are recommend for your project for they are running stably. Others treeLayoutManagers are developing.
     * @return layout manager
     */
    private TreeLayoutManager getTreeLayoutManager() {
        int space_50dp = 30;
        int space_20dp = 20;
        BaseLine line = getLine();
        return new BoxRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);


        //TODO !!!!! the layoutManagers below are just for test don't use in your projects. Just for test now
        //return new TableRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);

        //return new CompactRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);

        //return new CompactRingTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new ForceDirectedTreeLayoutManager(this,line);
    }

    private BaseLine getLine() {
        return new SmoothLine();
        //return new StraightLine(Color.parseColor("#055287"),2);
        //return new DashLine(Color.parseColor("#F1286C"),3);
        //return new AngledLine();
    }

    private TreeModel<Animal> setData(){
        TreeModel<Animal> treeModel = new TreeModel<>();
        //root
        NodeModel<Animal> node_root = treeModel.createNode(new Animal(R.drawable.ic_01,"-root-\n%%%%%%%%%%%%%%%%\n%%%%%%%%%%\n%%%%%%%%%%\n%%%%%%%%%%\n%%%%%%%%%%"));
        NodeModel<Animal> node0 = treeModel.createNode(new Animal(R.drawable.ic_02,"node00"));
        NodeModel<Animal> node1 = treeModel.createNode(new Animal(R.drawable.ic_03,"node01"));
        NodeModel<Animal> node2 = treeModel.createNode(new Animal(R.drawable.ic_04,"node02"));
        NodeModel<Animal> node3 = treeModel.createNode(new Animal(R.drawable.ic_05,"node03===\n=====\n======\n===\n=====\n=====\n===\n====\n=======\n=====\n======\n=====\n======\n========\n=====\n=====\n===\n======="));
        NodeModel<Animal> node4 = treeModel.createNode(new Animal(R.drawable.ic_06,"node04"));
        NodeModel<Animal> node5 = treeModel.createNode(new Animal(R.drawable.ic_07,"node05****************************************************"));
        NodeModel<Animal> node6 = treeModel.createNode(new Animal(R.drawable.ic_08,"node06666\n666666\n6666666\n666666\n666666666\n66666\n6666\n66666666\n66666666666\n666\n666666666\n666555\n5555\n55555555"));
        NodeModel<Animal> node7 = treeModel.createNode(new Animal(R.drawable.ic_09,"node07"));
        NodeModel<Animal> node8 = treeModel.createNode(new Animal(R.drawable.ic_10,"node08"));
        NodeModel<Animal> node9 = treeModel.createNode(new Animal(R.drawable.ic_11,"node09"));
        NodeModel<Animal> node10 = treeModel.createNode(new Animal(R.drawable.ic_12,"node10"));
        NodeModel<Animal> node11 = treeModel.createNode(new Animal(R.drawable.ic_13,"node11"));
        NodeModel<Animal> node12 = treeModel.createNode(new Animal(R.drawable.ic_14,"node12"));
        NodeModel<Animal> node13 = treeModel.createNode(new Animal(R.drawable.ic_15,"node13"));
        NodeModel<Animal> node14 = treeModel.createNode(new Animal(R.drawable.ic_13,"node14"));
        NodeModel<Animal> node15 = treeModel.createNode(new Animal(R.drawable.ic_14,"node15"));
        NodeModel<Animal> node16 = treeModel.createNode(new Animal(R.drawable.ic_15,"node16"));
        NodeModel<Animal> node17 = treeModel.createNode(new Animal(R.drawable.ic_08,"node17"));
        NodeModel<Animal> node18 = treeModel.createNode(new Animal(R.drawable.ic_09,"node18"));
        NodeModel<Animal> node19 = treeModel.createNode(new Animal(R.drawable.ic_10,"node19"));
        NodeModel<Animal> node20 = treeModel.createNode(new Animal(R.drawable.ic_02,"node20"));
        NodeModel<Animal> node21 = treeModel.createNode(new Animal(R.drawable.ic_03,"node21"));
        NodeModel<Animal> node22 = treeModel.createNode(new Animal(R.drawable.ic_04,"node22"));
        NodeModel<Animal> node23 = treeModel.createNode(new Animal(R.drawable.ic_05,"node23"));
        NodeModel<Animal> node24 = treeModel.createNode(new Animal(R.drawable.ic_06,"node24"));
        NodeModel<Animal> node25 = treeModel.createNode(new Animal(R.drawable.ic_07,"node25"));
        NodeModel<Animal> node26 = treeModel.createNode(new Animal(R.drawable.ic_08,"node26"));
        NodeModel<Animal> node27 = treeModel.createNode(new Animal(R.drawable.ic_09,"node27"));
        NodeModel<Animal> node28 = treeModel.createNode(new Animal(R.drawable.ic_10,"node28"));
        NodeModel<Animal> node29 = treeModel.createNode(new Animal(R.drawable.ic_11,"node29"));
        NodeModel<Animal> node30 = treeModel.createNode(new Animal(R.drawable.ic_02,"node30"));
        NodeModel<Animal> node31 = treeModel.createNode(new Animal(R.drawable.ic_03,"node31"));
        NodeModel<Animal> node32 = treeModel.createNode(new Animal(R.drawable.ic_04,"node32"));
        NodeModel<Animal> node33 = treeModel.createNode(new Animal(R.drawable.ic_05,"node33"));
        NodeModel<Animal> node34 = treeModel.createNode(new Animal(R.drawable.ic_06,"node34"));
        NodeModel<Animal> node35 = treeModel.createNode(new Animal(R.drawable.ic_07,"node35"));
        NodeModel<Animal> node36 = treeModel.createNode(new Animal(R.drawable.ic_08,"node36"));
        NodeModel<Animal> node37 = treeModel.createNode(new Animal(R.drawable.ic_09,"node37"));
        NodeModel<Animal> node38 = treeModel.createNode(new Animal(R.drawable.ic_10,"node38"));
        NodeModel<Animal> node39 = treeModel.createNode(new Animal(R.drawable.ic_11,"node39"));
        NodeModel<Animal> node40 = treeModel.createNode(new Animal(R.drawable.ic_02,"node40&&\n&&&\n&&&&\n&&&\n&&&\n&&&&\n&&&\n&&&\n&&&&\n&&&\n&&&\n&&&&\n&&&"));
        NodeModel<Animal> node41 = treeModel.createNode(new Animal(R.drawable.ic_03,"node41"));
        NodeModel<Animal> node42 = treeModel.createNode(new Animal(R.drawable.ic_04,"node42"));
        NodeModel<Animal> node43 = treeModel.createNode(new Animal(R.drawable.ic_05,"node43"));
        NodeModel<Animal> node44 = treeModel.createNode(new Animal(R.drawable.ic_06,"node44"));
        NodeModel<Animal> node45 = treeModel.createNode(new Animal(R.drawable.ic_07,"node45"));
        NodeModel<Animal> node46 = treeModel.createNode(new Animal(R.drawable.ic_08,"node46"));
        NodeModel<Animal> node47 = treeModel.createNode(new Animal(R.drawable.ic_09,"node47"));
        NodeModel<Animal> node48 = treeModel.createNode(new Animal(R.drawable.ic_10,"node48"));
        NodeModel<Animal> node49 = treeModel.createNode(new Animal(R.drawable.ic_11,"node49"));
        NodeModel<Animal> node50 = treeModel.createNode(new Animal(R.drawable.ic_05,"node50"));
        NodeModel<Animal> node51 = treeModel.createNode(new Animal(R.drawable.ic_07,"node51"));
        NodeModel<Animal> node52 = treeModel.createNode(new Animal(R.drawable.ic_07,"node52"));
        NodeModel<Animal> node53 = treeModel.createNode(new Animal(R.drawable.ic_07,"node53"));

        //build relationship
        node_root.addChildNodes(node0,node1,node3,node4);
        node3.addChildNodes(node12,node13);
        node1.addChildNodes(node2);
        parentToEditChildren = node1;
        node0.addChildNodes(node34,node5,node38,node39);
        node4.addChildNodes(node6);
        node5.addChildNodes(node7,node8);
        node6.addChildNodes(node9,node10,node11);
        node11.addChildNodes(node14,node15);
        node10.addChildNodes(node40);
        node40.addChildNodes(node16);
        node8.addChildNodes(node17,node18,node19,node20,node21,node22,node23,node41,node42,node43,node44);
        node9.addChildNodes(node47,node48);
        node16.addChildNodes(node24,node25,node26,node27,node28,node29,node30,node46,node45);
        node47.addChildNodes(node49);
        node12.addChildNodes(node37);
        node0.addChildNodes(node36);
        node39.addChildNodes(node52,node53);
        node37.addChildNodes(node41);
        node41.addChildNodes(node42);
        node42.addChildNodes(node43);

        //set data
        return treeModel;
    }
}