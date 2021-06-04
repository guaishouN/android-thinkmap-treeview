package com.gyso.gysotreeviewapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gyso.gysotreeviewapplication.base.Animal;
import com.gyso.gysotreeviewapplication.base.AnimalTreeViewAdapter;
import com.gyso.gysotreeviewapplication.databinding.ActivityMainBinding;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.layout.VerticalTreeLayoutManager;
import com.gyso.treeview.line.AngledLine;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    TreeModel<Animal> treeModel;
    AnimalTreeViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init
        initWidgets();

    }

    /**
     * To use a tree view, you should do 4 steps as follows:
     * 1 customs adapter
     * <p>
     * 2 configure layout manager. Space unit is dp.
     * You can custom you line by extends {@link BaseLine}
     * <p>
     * 3 view setting
     * <p>
     * 4 nodes data setting
     */
    private void initWidgets() {
        //1 customs adapter
        adapter = new AnimalTreeViewAdapter();

        //2 configure layout manager; unit dp
        TreeLayoutManager treeLayoutManager = getTreeLayoutManager();

        //3 view setting
        binding.baseTreeView.setAdapter(adapter);
        binding.baseTreeView.setTreeLayoutManager(treeLayoutManager);
        binding.baseTreeView.setOnDragMoveListener((sourceNode, targetNode) -> {
            changeNode(sourceNode, targetNode);
        });

        //4 nodes data setting
        setData(adapter);

    }


    private TreeLayoutManager getTreeLayoutManager() {
        int space_50dp = 50;
        int space_20dp = 20;
        BaseLine line = getLine();
        //return new RightTreeLayoutManager(this,space_50dp,space_20dp,line);
        return new VerticalTreeLayoutManager(this, space_50dp, space_20dp, line);
    }

    private BaseLine getLine() {
//        return new SmoothLine();  //曲线
//        return new StraightLine();    //直线
//        return new PointedLine();   //树杈感
        //return new DashLine(Color.parseColor("#4DB6AC"),8);
        return new AngledLine();  //直角
    }

    private void setData(AnimalTreeViewAdapter adapter) {
        //root
        NodeModel<Animal> root = new NodeModel<>(new Animal(R.drawable.ic_01, "root"));
        treeModel = new TreeModel<>(root);

        //child nodes
        NodeModel<Animal> sub0 = new NodeModel<>(new Animal(R.drawable.ic_02, "sub0"));
        NodeModel<Animal> sub1 = new NodeModel<>(new Animal(R.drawable.ic_03, "sub1"));
        NodeModel<Animal> sub2 = new NodeModel<>(new Animal(R.drawable.ic_04, "sub2"));
        NodeModel<Animal> sub3 = new NodeModel<>(new Animal(R.drawable.ic_05, "sub3"));
        NodeModel<Animal> sub4 = new NodeModel<>(new Animal(R.drawable.ic_06, "sub4"));
        NodeModel<Animal> sub5 = new NodeModel<>(new Animal(R.drawable.ic_07, "sub5"));
        NodeModel<Animal> sub6 = new NodeModel<>(new Animal(R.drawable.ic_08, "sub6"));
        NodeModel<Animal> sub7 = new NodeModel<>(new Animal(R.drawable.ic_09, "sub7"));
        NodeModel<Animal> sub8 = new NodeModel<>(new Animal(R.drawable.ic_10, "sub8"));
        NodeModel<Animal> sub9 = new NodeModel<>(new Animal(R.drawable.ic_11, "sub9"));
        NodeModel<Animal> sub10 = new NodeModel<>(new Animal(R.drawable.ic_12, "sub10\nWhat is this."));
        NodeModel<Animal> sub11 = new NodeModel<>(new Animal(R.drawable.ic_13, "sub11"));
        NodeModel<Animal> sub12 = new NodeModel<>(new Animal(R.drawable.ic_14, "sub12\nThink Map\n"));
        NodeModel<Animal> sub13 = new NodeModel<>(new Animal(R.drawable.ic_15, "sub13"));
        NodeModel<Animal> sub14 = new NodeModel<>(new Animal(R.drawable.ic_13, "sub14"));
        NodeModel<Animal> sub15 = new NodeModel<>(new Animal(R.drawable.ic_14, "sub15\nTree View"));
        NodeModel<Animal> sub16 = new NodeModel<>(new Animal(R.drawable.ic_15, "sub16"));
        NodeModel<Animal> sub17 = new NodeModel<>(new Animal(R.drawable.ic_08, "sub17\nHello World!"));
        NodeModel<Animal> sub18 = new NodeModel<>(new Animal(R.drawable.ic_09, "sub18"));
        NodeModel<Animal> sub19 = new NodeModel<>(new Animal(R.drawable.ic_10, "sub19"));
        NodeModel<Animal> sub20 = new NodeModel<>(new Animal(R.drawable.ic_11, "sub20"));

        //build relationship
        treeModel.addNode(root, sub0, sub1, sub3);
        treeModel.addNode(sub1, sub2);
        treeModel.addNode(sub0, sub4, sub5);
        treeModel.addNode(sub4, sub6);
        treeModel.addNode(sub5, sub7, sub8);
        treeModel.addNode(sub6, sub9, sub10, sub11);
        treeModel.addNode(sub3, sub12, sub13);
        treeModel.addNode(sub11, sub14, sub15, sub16);
        treeModel.addNode(sub8, sub17, sub18, sub19, sub20);
        //set data
        adapter.setTreeModel(treeModel);
    }

    private void changeNode(NodeModel<?> sourceNode, NodeModel<?> targetNode) {
        NodeModel<Animal> s = (NodeModel<Animal>) sourceNode;
        NodeModel<Animal> t = (NodeModel<Animal>) targetNode;
        treeModel.removeNode(s.parentNode, s);
        treeModel.addNode(t, s);
        adapter.setTreeModel(treeModel);
        binding.baseTreeView.focusMidLocation();
    }


}