package com.example.prj_qrcode;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private Button btn_scan;


    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("clientes");

        btn_scan = findViewById(R.id.btn_scan1);

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scanner QRCode");
                integrator.setOrientationLocked(true);

                integrator.setBeepEnabled(true);
                integrator.setCameraId(0); // 0 = CAMERA TRASEIRA | FRONTAL = 1
                integrator.initiateScan();
                //
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result.getContents()!= null){

            try {
                String conteudoFull = result.getContents();

                byte[] descontosToByte = Base64.getDecoder().decode(conteudoFull.getBytes());

                String decodedDesconto = new String(descontosToByte);

                // Código para converter de JSon para a model
                Gson gson = new Gson();
                final Model qrCode = gson.fromJson(decodedDesconto, Model.class);
                String json = gson.toJson(qrCode);
                String encoded = Base64.getEncoder().encodeToString(json.getBytes());

                int i = 10;

                final ModelCliente cliente = new ModelCliente();

                myRef.child(qrCode.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        int i = 10;

                            cliente.setId(dataSnapshot.child("id").getValue().toString());
                            cliente.setSaldo(dataSnapshot.child("saldo").getValue().toString());
                            cliente.setNome(dataSnapshot.child("nome").getValue().toString());


                        if( Integer.parseInt(qrCode.getDesconto()) <= Integer.parseInt(cliente.getSaldo())){
                            int novoSaldo = Integer.parseInt(cliente.getSaldo()) - Integer.parseInt(qrCode.getDesconto());

                            ModelCliente clienteComDesconto = new ModelCliente();

                            clienteComDesconto.setNome(cliente.getNome());
                            clienteComDesconto.setId(cliente.getId());
                            clienteComDesconto.setSaldo(String.valueOf(novoSaldo));

                            myRef.child(qrCode.getId()).setValue(clienteComDesconto).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    alert("Compra realizada com sucesso!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    alert("Falha ao finalizar a compra. " + e);
                                }
                            });

                        }
                        else{
                            alert("Saldo insuficiente.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            catch (Exception el) {
                alert("QR Code inválido. " + el);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void alert(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
