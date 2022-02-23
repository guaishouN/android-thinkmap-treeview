package com.gyso.gysotreeviewapplication;

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
import com.gyso.treeview.layout.CompactVerticalTreeLayoutManager;
import com.gyso.treeview.layout.ForceDirectedTreeLayoutManager;
import com.gyso.treeview.layout.RightTreeLayoutManager;
import com.gyso.treeview.layout.RingTreeLayoutManager;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.layout.VerticalTreeLayoutManager;
import com.gyso.treeview.line.AngledLine;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.line.StraightLine;
import com.gyso.treeview.listener.TreeViewControlListener;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private final Stack<NodeModel<Animal>> removeCache = new Stack();
    private NodeModel<Animal> targetNode;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private Handler handler = new Handler();
    private NodeModel<Animal> parentToRemoveChildren = null;
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
        setData(adapter);

        //5 get an editor. Note: an adapter must set before get an editor.
        final TreeViewEditor editor = binding.baseTreeView.getEditor();

        //6 you own others jobs
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
            if(targetNode == null){
                Toast.makeText(this,"Ohs, your targetNode is null", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> a = new NodeModel<>(new Animal(R.drawable.ic_10,"add-"+atomicInteger.getAndIncrement()));
            NodeModel<Animal> b = new NodeModel<>(new Animal(R.drawable.ic_11,"add-"+atomicInteger.getAndIncrement()));
            NodeModel<Animal> c = new NodeModel<>(new Animal(R.drawable.ic_14,"add-"+atomicInteger.getAndIncrement()));
            editor.addChildNodes(targetNode,a,b,c);


            //add to remove demo cache
            removeCache.push(targetNode);
            targetNode = b;
        });

        //remove node
        binding.removeNodeBt.setOnClickListener(v->{
            if(removeCache.isEmpty()){
                Toast.makeText(this,"Ohs, demo removeCache is empty now!! Try to add some nodes firstly!!", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> toRemoveNode = removeCache.pop();
            targetNode = toRemoveNode.getParentNode();
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
                Log.e(TAG, "onDragMoveNodesHit: draging["+draggingNode+"]hittingNode["+hittingNode+"]");

            }
        });
    }

    private TreeLayoutManager getTreeLayoutManager() {
        int space_50dp = 30;
        int space_20dp = 20;
        BaseLine line = getLine();
        //return new RightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new VerticalTreeLayoutManager(this,space_50dp,space_20dp,line);
        return new CompactVerticalTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new RingTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new ForceDirectedTreeLayoutManager(this,line);
    }

    private BaseLine getLine() {
        //return new SmoothLine();
         return new StraightLine();
        //return new PointedLine();
        //return new DashLine(Color.parseColor("#4DB6AC"),8);
        //return new AngledLine();
    }

    private void setData(AnimalTreeViewAdapter adapter){
        //root
        NodeModel<Animal> root = new NodeModel<>(new Animal(R.drawable.ic_01,"-root-"));
        TreeModel<Animal> treeModel = new TreeModel<>(root);

        //child nodes
        NodeModel<Animal> sub0 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub00"));
        NodeModel<Animal> sub1 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub01"));
        NodeModel<Animal> sub2 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub02"));
        NodeModel<Animal> sub3 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub03"));
        NodeModel<Animal> sub4 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub04"));
        NodeModel<Animal> sub5 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub05"));
        NodeModel<Animal> sub6 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub06"));
        NodeModel<Animal> sub7 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub07"));
        NodeModel<Animal> sub8 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub08"));
        NodeModel<Animal> sub9 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub09"));
        NodeModel<Animal> sub10 = new NodeModel<>(new Animal(R.drawable.ic_12,"sub10"));
        NodeModel<Animal> sub11 = new NodeModel<>(new Animal(R.drawable.ic_13,"sub11"));
        NodeModel<Animal> sub12 = new NodeModel<>(new Animal(R.drawable.ic_14,"sub12"));
        NodeModel<Animal> sub13 = new NodeModel<>(new Animal(R.drawable.ic_15,"sub13"));
        NodeModel<Animal> sub14 = new NodeModel<>(new Animal(R.drawable.ic_13,"sub14"));
        NodeModel<Animal> sub15 = new NodeModel<>(new Animal(R.drawable.ic_14,"sub15"));
        NodeModel<Animal> sub16 = new NodeModel<>(new Animal(R.drawable.ic_15,"sub16"));
        NodeModel<Animal> sub17 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub17"));
        NodeModel<Animal> sub18 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub18"));
        NodeModel<Animal> sub19 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub19"));
        NodeModel<Animal> sub20 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub20"));
        NodeModel<Animal> sub21 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub21"));
        NodeModel<Animal> sub22 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub22"));
        NodeModel<Animal> sub23 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub23"));
        NodeModel<Animal> sub24 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub24"));
        NodeModel<Animal> sub25 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub25"));
        NodeModel<Animal> sub26 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub26"));
        NodeModel<Animal> sub27 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub27"));
        NodeModel<Animal> sub28 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub28"));
        NodeModel<Animal> sub29 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub29"));
        NodeModel<Animal> sub30 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub30"));
        NodeModel<Animal> sub31 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub31"));
        NodeModel<Animal> sub32 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub32"));
        NodeModel<Animal> sub33 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub33"));
        NodeModel<Animal> sub34 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub34"));
        NodeModel<Animal> sub35 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub35"));
        NodeModel<Animal> sub36 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub36"));
        NodeModel<Animal> sub37 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub37"));
        NodeModel<Animal> sub38 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub38"));
        NodeModel<Animal> sub39 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub39"));
        NodeModel<Animal> sub40 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub40"));
        NodeModel<Animal> sub41 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub41"));
        NodeModel<Animal> sub42 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub42"));
        NodeModel<Animal> sub43 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub43"));
        NodeModel<Animal> sub44 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub44"));
        NodeModel<Animal> sub45 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub45"));
        NodeModel<Animal> sub46 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub46"));
        NodeModel<Animal> sub47 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub47"));
        NodeModel<Animal> sub48 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub48"));
        NodeModel<Animal> sub49 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub49"));

        //build relationship
        treeModel.addNode(root,sub0,sub1,sub3);
        treeModel.addNode(sub3,sub12,sub13);
        treeModel.addNode(sub1,sub2);
        treeModel.addNode(sub0,sub4,sub5);
        treeModel.addNode(sub4,sub6);
        treeModel.addNode(sub5,sub7,sub8);
        treeModel.addNode(sub6,sub9,sub10,sub11);
        treeModel.addNode(sub11,sub14,sub15);
        treeModel.addNode(sub10,sub16);
        treeModel.addNode(sub8,sub17,sub18,sub19,sub20,sub21,sub22,sub23);
        treeModel.addNode(sub23,sub24,sub25,sub26,sub27,sub28,sub29,sub30);

        //mark
        parentToRemoveChildren = sub0;
        targetNode = sub1;

        //set data
        adapter.setTreeModel(treeModel);
    }
}