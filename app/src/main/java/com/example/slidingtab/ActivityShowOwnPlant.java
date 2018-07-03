package com.example.slidingtab;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ActivityShowOwnPlant extends AppCompatActivity {

    public String stringname;
    public String stringdate;
    public String stringfrequency;
    private String stringpicture;
    private TextView tvshowownplanttoolbar;
    private TextView tvshowownplantname;
    private TextView tvshowownplantdate;
    private TextView tvshowownplantfrequency;
    private ImageView ivshowownplantpicture;
    private Button bdeleteplant;
    private Context mContext;
    private DatabaseReference dbRef;
    private StorageReference stRef;
    private String DATEWITHFREQ_FILE = "PlantDateWithFreq.txt";
    private String NAMEWITHFREQ_FILE = "PlantNameWithFreq.txt";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showownplant);

        mContext = ActivityShowOwnPlant.this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityShowOwnPlant.this.onBackPressed();
            }
        });

        tvshowownplanttoolbar = findViewById(R.id.TVShowOwnPlantToolbar);
        tvshowownplantname = findViewById(R.id.TVShowOwnPlantName);
        tvshowownplantdate = findViewById(R.id.TVShowOwnPlantDate);
        tvshowownplantfrequency = findViewById(R.id.TVShowOwnPlantFrequency);
        ivshowownplantpicture = findViewById(R.id.IVShowownPlantPicture);
        bdeleteplant = findViewById(R.id.BDeletePlant);
        dbRef = FirebaseDatabase.getInstance().getReference("Plants");
        stRef = FirebaseStorage.getInstance().getReference();

        stringname = getIntent().getExtras().getString("name");
        stringdate = getIntent().getExtras().getString("date");
        stringfrequency = getIntent().getExtras().getString("frequency");

        tvshowownplanttoolbar.setText(stringname);
        tvshowownplanttoolbar.setTextColor(getResources().getColor(R.color.C_white));
        tvshowownplantname.setText(stringname);
        tvshowownplantdate.setText(stringdate);
        tvshowownplantfrequency.setText(stringfrequency);

        StorageReference picRef = stRef.child(stringname);
        picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                stringpicture = downloadUrl.toString();
                Picasso.get().load(stringpicture).into(ivshowownplantpicture);
                }
        });


        bdeleteplant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ActivityShowOwnPlant.this);

                // set title
                alertDialogBuilder.setTitle("Pflanze löschen");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Willst du diese Pflanze wirklich löschen?")
                        .setCancelable(false)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //if clicked delete plant and exit activity
                                dbRef.child(stringname).removeValue();
                                stRef.child(stringname).delete();
                                delete(stringname);
                                Toast.makeText(mContext, "Pflanze erfolgreich gelöscht",Toast.LENGTH_SHORT).show();
                                ActivityShowOwnPlant.this.onBackPressed();
                            }
                        })
                        .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

    }

    private void delete (String name){

        FileInputStream fInName = null;
        FileInputStream fInDate = null;
        FileOutputStream fOutClearWriteName = null;
        FileOutputStream fOutClearWriteDate = null;

        try{
            //initialize files, readers and arraylists
            fInName = mContext.openFileInput(NAMEWITHFREQ_FILE);
            fInDate = mContext.openFileInput(DATEWITHFREQ_FILE);

            BufferedReader readerName = new BufferedReader(new InputStreamReader(fInName));
            BufferedReader readerDate = new BufferedReader(new InputStreamReader(fInDate));

            ArrayList<String> listName = new ArrayList<>();
            ArrayList<String> listDate = new ArrayList<>();

            //write all data from files into arraylists
            String lineName;
            while((lineName = readerName.readLine()) != null){
                listName.add(lineName);
            }
            String lineDate;
            while((lineDate = readerDate.readLine())!= null){
                listDate.add(lineDate);
            }

            //look through all elemnts in namelist; if the name you want to delete exists remove
            //this position in all lists
            int counterOfDeletedElems = 0;
            for (int i = 0; i < listName.size(); i++) {
                if ((listName.get(i).equals(name)) || (listName.get(i).equals(" "))) {
                    listName.remove(i-counterOfDeletedElems);
                    listDate.remove(i-counterOfDeletedElems);
                    counterOfDeletedElems++;
                }else continue;
            }

            fInName.close();
            fInDate.close();

            //clear all data initially written in NAMEWITHFREQ_FILE/DATEWITHFREQ_FILE and then write back all
            //data to the initial file (NAME, DATE)
            fOutClearWriteName = mContext.openFileOutput(NAMEWITHFREQ_FILE, Context.MODE_PRIVATE);
            fOutClearWriteName.close();
            fOutClearWriteDate = mContext.openFileOutput(DATEWITHFREQ_FILE, Context.MODE_PRIVATE);
            fOutClearWriteDate.close();

            //write back all data (all but the line that included the data you wanted to delete)
            fOutClearWriteName = mContext.openFileOutput(NAMEWITHFREQ_FILE, Context.MODE_APPEND);
            fOutClearWriteDate = mContext.openFileOutput(DATEWITHFREQ_FILE, Context.MODE_APPEND);
            for(int i = 0;i<listDate.size();i++) {
                fOutClearWriteName.write((listName.get(i) + "\n").getBytes());
                fOutClearWriteDate.write((listDate.get(i) + "\n").getBytes());
            }
            fOutClearWriteName.close();
            fOutClearWriteDate.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}



