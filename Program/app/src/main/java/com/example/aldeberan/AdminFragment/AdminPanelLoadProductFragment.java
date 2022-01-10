package com.example.aldeberan.AdminFragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.aldeberan.Activity.AddressSelectionToBook;
import com.example.aldeberan.Activity.AdminPanelActivity;
import com.example.aldeberan.Adapter.ProductAdapter;
import com.example.aldeberan.R;
import com.example.aldeberan.models.ProductModel;
import com.example.aldeberan.structures.Product;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/*
Ong Shuoh Chwen 1171102212

Ong is reponsible for this feature.
*/

public class AdminPanelLoadProductFragment extends Fragment {
    private View myFragmentView;
    public List<Product> productList;
    public RecyclerView recyclerView;
    public ProductAdapter adapter;
    public TextView noProdLbl;
    public ProgressBar onLoadThrobber;
    public Handler mHandler;
    public AlphaAnimation alphaAnimation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.activity_admin_panel_load_product, container, false);
        //((home_product) getActivity()).setActionBarTitle("Manage Bread(s)");
        productList = new ArrayList<>();
        recyclerView = myFragmentView.findViewById(R.id.recyclerView);
        noProdLbl = myFragmentView.findViewById(R.id.noProdLbl);
        onLoadThrobber = myFragmentView.findViewById(R.id.onLoadThrobber);
        mHandler = new Handler();

        ConstructRecyclerView();
        SwipeRefreshLayout pullToRefresh = myFragmentView.findViewById(R.id.pullToRefreshProduct);

        pullToRefresh.setOnRefreshListener(() -> {
            ConstructRecyclerView();
            adapter.notifyDataSetChanged();
            pullToRefresh.setRefreshing(false);
        });

        return myFragmentView;
    }

    private void onLoadThrobber() {
        //On load throbber fade out
        alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(2000);
        onLoadThrobber.startAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onLoadThrobber.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onLoadThrobber.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void ConstructRecyclerView(){
        ProductModel pm = new ProductModel();
        onLoadThrobber();
        pm.readProductAll((response) -> {
            productList = response;
            if (productList.isEmpty()) {
                noProdLbl.setVisibility(View.VISIBLE);
            } else {
                noProdLbl.setVisibility(View.GONE);
                PutDataIntoRecyclerView(response);
            }
        });
    }

    ProductAdapter.FragmentCommunication communication= (prodName, prodID, prodSKU, prodImg, prodStock, prodAvail, prodPrice) -> {
        AdminPanelUpdateProductFragment updateProductFragment = new AdminPanelUpdateProductFragment();
        Bundle bundle = new Bundle();
        bundle.putString("prodName", prodName);
        bundle.putString("prodID", prodID);
        bundle.putString("prodSKU", prodSKU);
        bundle.putString("prodImg", prodImg);
        bundle.putString("prodStock", prodStock);
        bundle.putString("prodAvail", prodAvail);
        bundle.putString("prodPrice", prodPrice);
        updateProductFragment.setArguments(bundle);

        //Redirect to update product fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminFragmentView, updateProductFragment)
                .addToBackStack(null)
                .commit();
        ((AdminPanelActivity) getActivity()).setTitleBar("Update Product");
    };

    private void PutDataIntoRecyclerView(List<Product> productList){
        adapter = new ProductAdapter(getContext(), productList, communication);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        Log.i("PLOPE", String.valueOf(productList));
    }
}
