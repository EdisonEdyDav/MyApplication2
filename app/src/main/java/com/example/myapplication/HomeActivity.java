package com.example.myapplication;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.Fragments.HomeFragment;
import com.example.myapplication.Fragments.ShopingFragment;
import com.example.myapplication.Model.User;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);


        //Init

        userRef= FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        //check intent, if is login = true , enable full access
        //if is login= false, just let user aroun shopping to view

        if(getIntent()!=null){
            boolean isLogin=getIntent().getBooleanExtra(Commons.IS_LOGIN,false);
            if(isLogin){
                dialog.show();
                //check if user is exists
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {

                        if(account != null){
                            DocumentReference currentUser=userRef.document(account.getPhoneNumber().toString());
                            currentUser.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){

                                                DocumentSnapshot userSnapShot=task.getResult();
                                                if(!userSnapShot.exists())
                                                    showUpdateDialog(account.getPhoneNumber().toString());

                                            }

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                        Toast.makeText(HomeActivity.this,""+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });



            }

        }

        // view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment=null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.action_home)
                    fragment=new HomeFragment();
                else if(menuItem.getItemId() == R.id.action_shopping)
                    fragment= new ShopingFragment();


                return loadFragment(fragment);
            }
        });


    }

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .commit();
            return true;

        }
        return false;


    }

    private void showUpdateDialog(final String telefono){

        if(dialog.isShowing())
            dialog.dismiss();

        //Init dialog

        bottomSheetDialog =  new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("Un paso mas!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_iformation,null);

        Button btn_guardar =(Button)sheetView.findViewById(R.id.btn_guardar);
        final TextInputEditText edt_nombre =(TextInputEditText)sheetView.findViewById(R.id.edt_nombre);
        final TextInputEditText edt_direccion =(TextInputEditText)sheetView.findViewById(R.id.edt_direccion);

        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User(edt_nombre.getText().toString(),
                        edt_direccion.getText().toString(),
                        telefono);
                userRef.document(telefono)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                bottomSheetDialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Gracias",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "+e.getMessage()",Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });


        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();


    }


}
