package com.gyso.gysotreeviewapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import com.gyso.gysotreeviewapplication.base.Animal;
import com.gyso.gysotreeviewapplication.base.GysoTreeViewAdapter;
import com.gyso.gysotreeviewapplication.databinding.ActivityMainBinding;
import com.gyso.treeview.layout.RightITreeLayoutManager;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;
import com.gyso.treeview.util.DensityUtils;

public class MainActivity extends AppCompatActivity {
    private GysoTreeViewAdapter adapter;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init
        initWidgets();
    }

    private void initWidgets() {
        int dx = DensityUtils.dp2px(this, 50);
        int dy = DensityUtils.dp2px(this, 20);
        adapter = new GysoTreeViewAdapter();
        binding.baseTreeView.setAdapter(adapter);
        binding.baseTreeView.setTreeLayoutManager(new RightITreeLayoutManager(dx, dy));
        setData();
    }

    private void setData(){
        //root
        NodeModel<Animal> root = new NodeModel<>(new Animal(R.drawable.ic_01,"root"));
        TreeModel<Animal> treeModel = new TreeModel<>(root);

        //child nodes
        NodeModel<Animal> sub0 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub0"));
        NodeModel<Animal> sub1 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub1"));
        NodeModel<Animal> sub2 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub2"));
        NodeModel<Animal> sub3 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub3"));
        NodeModel<Animal> sub4 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub4"));
        NodeModel<Animal> sub5 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub5"));
        NodeModel<Animal> sub6 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub6"));
        NodeModel<Animal> sub7 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub7"));
        NodeModel<Animal> sub8 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub8"));
        NodeModel<Animal> sub9 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub9"));
        NodeModel<Animal> sub10 = new NodeModel<>(new Animal(R.drawable.ic_12,"sub10"));
        NodeModel<Animal> sub11 = new NodeModel<>(new Animal(R.drawable.ic_13,"sub11"));
        NodeModel<Animal> sub12 = new NodeModel<>(new Animal(R.drawable.ic_14,"sub12"));
        NodeModel<Animal> sub13 = new NodeModel<>(new Animal(R.drawable.ic_15,"sub13"));

        //build relationship
        treeModel.addNode(root,sub0,sub1,sub2,sub3);
        treeModel.addNode(sub0,sub4,sub5);
        treeModel.addNode(sub4,sub6);
        treeModel.addNode(sub5,sub7,sub8);
        treeModel.addNode(sub6,sub9,sub10,sub11);
        treeModel.addNode(sub3,sub12,sub13);

        //set data
        adapter.setTreeModel(treeModel);
    }
}