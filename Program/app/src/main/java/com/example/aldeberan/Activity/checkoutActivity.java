package com.example.aldeberan.Activity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aldeberan.Adapter.CheckoutAdapter;
import com.example.aldeberan.CartFragment;
import com.example.aldeberan.HomepageFragment;
import com.example.aldeberan.R;
import com.example.aldeberan.models.CartModel;
import com.example.aldeberan.models.ProductModel;
import com.example.aldeberan.storage.OrderStorage;
import com.example.aldeberan.storage.UserStorage;
import com.example.aldeberan.structures.Cart;
import com.example.aldeberan.structures.Product;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;

import java.util.List;
import java.util.Locale;


/*
Leong Kah Ming  1171100884
Chong Wai Hou   1161104445

Leong and Chong are responsible for this feature.
They connected/linked the functions and debug the problems.
*/

public class checkoutActivity extends AppCompatActivity {

    private List<Cart> cartList;
    private UserStorage userStorage;
    private RecyclerView recyclerView;
    private CheckoutAdapter checkoutAdapter;

    private OrderStorage os;
    TextView addressBtn, buttonEditItem;
    TextView selectedAddressLbl;
    TextView price;

    String preProcess;
    double finalPrice;

    ShimmerFrameLayout shimmerCheckoutBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        getSupportActionBar().setTitle("Checkout");

        os = new OrderStorage(this);
        price = findViewById(R.id.price);
        recyclerView = findViewById(R.id.checkoutRecyclerView);
        Button confirmButton = findViewById(R.id.confirmOrder);
        selectedAddressLbl = findViewById(R.id.selectedAddressLbl);
        buttonEditItem = findViewById(R.id.buttonEditItem);
        addressBtn = findViewById(R.id.addressBtn);

        shimmerCheckoutBox = findViewById(R.id.shimmerCheckoutBox);
        shimmerCheckoutBox.startShimmerAnimation();

        if(os.getState().toLowerCase().contains("selangor")){
            price.setText("RM " + String.valueOf(os.getTotal()));
        }else{
            price.setText("RM " + String.valueOf(os.getTotal() + 5)  + " (Included Delivery Fee)");
        }

        String targetPrice = String.valueOf(price.getText());
        if(targetPrice.contains("(Included Delivery Fee)")){
            preProcess = targetPrice.replace(" (Included Delivery Fee)", "");
            preProcess = preProcess.replace("RM ", "");
        }else{
            preProcess = targetPrice.replace("RM ", "");
        }

        //System.out.println(preProcess);
        finalPrice = Double.parseDouble(preProcess);

        if (os.getRecipient() == ""){
            selectedAddressLbl.setText("Please select an address.");
            confirmButton.setEnabled(false);
        }
        else{
            updateSelectedAddress();
            confirmButton.setEnabled(true);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //readResponse();
                    os.saveTotal(finalPrice);
                    finish();
                    Intent payIntent = new Intent(checkoutActivity.this, StripePaymentCheckOut.class);
                    startActivity(payIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addressBtn.setOnClickListener(view -> {
            finish();
            Intent addIntent = new Intent(checkoutActivity.this, AddressSelection.class);
            startActivity(addIntent);
        });

        ConstructRecyclerView();
    }

    private void ConstructRecyclerView(){
        CartModel cm = new CartModel();
        userStorage = new UserStorage(this);
        int quoteID = userStorage.getQuoteID();

        try {
            cm.readQuoteItemByQuote(quoteID, response -> {
                cartList = response;
                PutDataIntoRecyclerView(cartList);
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void PutDataIntoRecyclerView(List<Cart> cartList) throws JSONException {
        checkoutAdapter = new CheckoutAdapter(this, cartList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(checkoutAdapter);
        shimmerCheckoutBox.stopShimmerAnimation();
        shimmerCheckoutBox.setVisibility(View.GONE);
    }

    public void updateSelectedAddress(){
        String data = "Receiver               : " + os.getRecipient()+ "\n" +
                "Contact Number: 60"+ os.getContact() + "\n" + "\n" +
                "Address               :" + "\n" +
                os.getLine1()+", "+os.getLine2()+"\n"+
                os.getCode()+", "+os.getCity()+", "+os.getState();

        selectedAddressLbl.setText(data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}