package com.example.slidingtab;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FirebaseUserClient{

    private static final Integer waterOften = 500;
    private static final Integer waterMedium = 200;
    private static final Integer waterRarely = 80;

    private DatabaseReference dbRef;
    private StorageReference stRef;
    private ProgressDialog progressDialog;
    private String picPicture;
    private String DATEWITHFREQ_FILE = "PlantDateWithFreq.txt";
    private String NAMEWITHFREQ_FILE = "PlantNameWithFreq.txt";

    public FirebaseUserClient() {
        dbRef = FirebaseDatabase.getInstance().getReference("Plants");
        stRef = FirebaseStorage.getInstance().getReference();
    }

    public void plantToDatabase(final Context mContext,
                                final PlantUsercreated plantUsercreated,
                                final Uri checkUri,
                                final TextView etplantname,
                                final TextView etenterfrequency) {

        final String pName = plantUsercreated.getName();
        final String pDate = plantUsercreated.getDate();
        final String pFrequency = plantUsercreated.getFrequency();

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Hochladen...");

        if ((checkUri == null) || (etplantname.getText().length() == 0) || (etenterfrequency.getText().length() == 0)) {

            if (checkUri == null) {
                Toast.makeText(mContext, "Bild auswählen", Toast.LENGTH_LONG).show();
            }
            if (etplantname.getText().length() == 0) {
                etplantname.setError("Name eingeben");
            }
            if (etenterfrequency.getText().length() == 0) {
                etenterfrequency.setError("Frequenz eingeben");
            }

        } else {
            picPicture = checkUri.toString();
            DatabaseReference pNameRef = dbRef.child(pName);
            pNameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //if data changed
                    if (dataSnapshot.exists()) {
                        //empty
                    } else {
                        dbRef.child(pName).child("Name").setValue(pName);
                        dbRef.child(pName).child("Date").setValue(pDate);
                        dbRef.child(pName).child("Frequency").setValue(pFrequency);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //empty
                }
            });

            stRef.child(pName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //File found -> already exists -> do not overwrite current picture
                    Toast.makeText(mContext, "Pflanze bereits vorhanden", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // File not found
                    progressDialog.show();

                    StorageReference childRef = stRef.child(pName);

                    //uploading the image
                    final UploadTask uploadTask = childRef.putFile(checkUri);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            StorageReference picRef = stRef.child(pName);
                            picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    picPicture = downloadUrl.toString();
                                    //String pU.picture is the link to the storage pic now
                                    plantUsercreated.setPicture(picPicture);
                                    dbRef.child(pName).child("Picture").setValue(picPicture);
                                    calculateNewDates(mContext, pFrequency, pDate, pName);
                                    Toast.makeText(mContext, "Pflanze gespeichert", Toast.LENGTH_SHORT).show();
                                    ((ActivityCreatePlant)mContext).onBackPressed();
                                }
                            });
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, "Fehlgeschlagen ->" + e, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    public void alertSavePlant(final Context mContext,
                               final PlantUsercreated pU,
                               final Uri picPath,
                               final String intentstringname,
                               final String intentstringfrequency,
                               final TextView etplantname,
                               final TextView etplantfrequency) {
        {
            progressDialog = new ProgressDialog(mContext);

            final String alertName;
            final String alertDate = pU.getDate();
            final String alertFrequency;

            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Hochladen...");

            if (picPath != null) {
                if (etplantname.getText().length() == 0) {
                    alertName = intentstringname;
                } else {
                    alertName = pU.getName();
                }
                if (etplantfrequency.getText().length() == 0) {
                    alertFrequency = intentstringfrequency;
                } else {
                    alertFrequency = pU.getFrequency();
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);

                // set title
                alertDialogBuilder.setTitle("Pflanze bearbeiten");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Willst du diese Pflanze wirklich bearbeiten?")
                        .setCancelable(false)
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //update everything but the picture cause no new picture is selected
                                final DatabaseReference alertNameRef = dbRef.child(alertName);
                                alertNameRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //if element with this name already exists
                                        if (dataSnapshot.exists()) {    //if name stays the same update rest
                                            if (alertName.equals(intentstringname)) {
                                                dbRef.child(alertName).child("Date").setValue(alertDate);
                                                dbRef.child(alertName).child("Frequency").setValue(alertFrequency);
                                            } else {
                                                //another plant with the edited name exist
                                            }
                                        } else {
                                            dbRef.child(alertName).child("Name").setValue(alertName);
                                            dbRef.child(alertName).child("Date").setValue(alertDate);
                                            dbRef.child(alertName).child("Frequency").setValue(alertFrequency);
                                            //calculateNewDates(mContext, alertFrequency, alertDate, alertName);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        //empty
                                    }
                                });
                                stRef.child(alertName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //File found -> already exists
                                        if (alertName.equals(intentstringname)) {
                                            progressDialog.show();
                                            StorageReference childRef = stRef.child(alertName);

                                            //uploading the image
                                            final UploadTask uploadTask = childRef.putFile(picPath);

                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    progressDialog.dismiss();
                                                    deleteOldDate(mContext, intentstringname);
                                                    StorageReference picRef = stRef.child(alertName);
                                                    picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri downloadUrl) {

                                                            picPicture = downloadUrl.toString();
                                                            pU.setPicture(picPicture);
                                                            dbRef.child(alertName).child("Picture").setValue(picPicture);
                                                            calculateNewDates(mContext, alertFrequency, alertDate, alertName);
                                                            Toast.makeText(mContext, "Pflanze erfolgreich bearbeitet", Toast.LENGTH_SHORT).show();
                                                            ((ActivityEditPlant) mContext).onBackPressed();
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(mContext, "Fehlgeschlagen ->" + e, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(mContext, "Andere Pflanze mit diesem Namen bereits vorhanden", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // File not found
                                        progressDialog.show();
                                        //save all data with the new name
                                        StorageReference childRef = stRef.child(alertName);

                                        //uploading the image
                                        final UploadTask uploadTask = childRef.putFile(picPath);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                progressDialog.dismiss();

                                                StorageReference picRef = stRef.child(alertName);
                                                picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri downloadUrl) {
                                                        //delete old data saved with the name you edited
                                                        stRef.child(intentstringname).delete();
                                                        dbRef.child(intentstringname).removeValue();
                                                        deleteOldDate(mContext, intentstringname);
                                                        picPicture = downloadUrl.toString();
                                                        dbRef.child(alertName).child("Picture").setValue(picPicture);
                                                        updateValues(mContext, alertName, alertDate, alertFrequency);
                                                        Toast.makeText(mContext, "Pflanze erfolgreich bearbeitet", Toast.LENGTH_SHORT).show();
                                                        ((ActivityEditPlant) mContext).onBackPressed();
                                                    }
                                                });
                                            }

                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(mContext, "Fehlgeschlagen ->" + e, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
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
            } else {
                Toast.makeText(mContext, "Neues Bild auswählen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteOldDate(Context mContext, String name) {
        FileInputStream fInName = null;
        FileInputStream fInDate = null;
        FileOutputStream fOutClearWriteName = null;
        FileOutputStream fOutClearWriteDate = null;

        try {
            //initialize files, readers and arraylists
            fInName = mContext.openFileInput(NAMEWITHFREQ_FILE);
            fInDate = mContext.openFileInput(DATEWITHFREQ_FILE);

            BufferedReader readerName = new BufferedReader(new InputStreamReader(fInName));
            BufferedReader readerDate = new BufferedReader(new InputStreamReader(fInDate));

            ArrayList<String> listName = new ArrayList<>();
            ArrayList<String> listDate = new ArrayList<>();

            //write all data from files into arraylists
            String lineName;
            while ((lineName = readerName.readLine()) != null) {
                listName.add(lineName);
            }
            String lineDate;
            while ((lineDate = readerDate.readLine()) != null) {
                listDate.add(lineDate);
            }

            //look through all elemnts in namelist; if the name you want to delete exists remove
            //this position in all lists
            int counterOfDeletedElems = 0;
            for (int i = 0; i < listName.size(); i++) {
                if ((listName.get(i).equals(name))) {
                    listName.remove(i - counterOfDeletedElems);
                    listDate.remove(i - counterOfDeletedElems);
                    counterOfDeletedElems++;
                } else continue;
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
            for (int i = 0; i < listDate.size(); i++) {
                fOutClearWriteName.write((listName.get(i) + "\n").getBytes());
                fOutClearWriteDate.write((listDate.get(i) + "\n").getBytes());
            }
            fOutClearWriteName.close();
            fOutClearWriteDate.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateValues(Context mContext, String name, String date, String frequency) {
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

            fInName.close();
            fInDate.close();

            //look through all elements in namelist; if the name you want to delete exists remove
            //this position in all lists
            int counterOfDeletedElems = 0;
            for (int i = 0; i < listName.size(); i++) {
                if ((listName.get(i).equals(name))) {
                    listName.remove(i-counterOfDeletedElems);
                    listDate.remove(i-counterOfDeletedElems);
                    counterOfDeletedElems++;
                }else continue;
            }

            //clear all data initially written in NAMEWITHFREQ_FILE/DATEWITHFREQ_FILE and then write back all
            //data to the initial file (NAME, DATE)
            fOutClearWriteName = mContext.openFileOutput(NAMEWITHFREQ_FILE, Context.MODE_PRIVATE);
            fOutClearWriteName.close();
            fOutClearWriteDate = mContext.openFileOutput(DATEWITHFREQ_FILE, Context.MODE_PRIVATE);
            fOutClearWriteDate.close();

            //write back all data (edited data instead of pre-edit data)
            fOutClearWriteName = mContext.openFileOutput(NAMEWITHFREQ_FILE, Context.MODE_APPEND);
            fOutClearWriteDate = mContext.openFileOutput(DATEWITHFREQ_FILE, Context.MODE_APPEND);
            for(int i = 0;i<listDate.size();i++) {
                fOutClearWriteName.write((listName.get(i) + "\n").getBytes());
                fOutClearWriteDate.write((listDate.get(i) + "\n").getBytes());
            }

            fOutClearWriteName.close();
            fOutClearWriteDate.close();

            //add the edited data to the back of the file
            calculateNewDates(mContext, frequency, date, name);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void calculateNewDates(Context mContext, String frequency, String initDate, String name){
        Integer iFrequency = Integer.parseInt(frequency);

        if(iFrequency <= 5){
            calcDates(mContext, waterOften, iFrequency, initDate, name);
        }
        else if(iFrequency <=15){
            calcDates(mContext, waterMedium, iFrequency, initDate, name);
        }else{
            calcDates(mContext, waterRarely, iFrequency, initDate, name);
        }
    }

    private void calcDates(Context mContext, int quant, int freq, String initDate, String name){
        List<String> dates = new ArrayList<>();
        List<String> names = new ArrayList<>();
        SimpleDateFormat sdfInitDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        long oneDay = 86400000; //milliseconds
        long freqToAdd = freq*oneDay;
        long millis;

        GregorianCalendar gConvertCal = new GregorianCalendar();

        GregorianCalendar gCal = new GregorianCalendar();
        try {
            Date date = sdfInitDate.parse(initDate);
            gCal.setTime(date);
        }catch (Exception e){
            e.printStackTrace();
        }

        for(int i=0; i<quant;i++){
            String stringDate = null;
            millis = gCal.getTimeInMillis();

            long millisToConvert = millis+(i*freqToAdd);

            gConvertCal.setTimeInMillis(millisToConvert);
            stringDate = sdfInitDate.format(gConvertCal.getTime());
            dates.add(stringDate);
            names.add(name);
        }
        saveStuff(mContext, dates, names);
    }

    private void saveStuff(Context mContext, List<String> dates, List<String> names){
        FileOutputStream fOut = null;
        try {
            fOut=mContext.openFileOutput(DATEWITHFREQ_FILE, Context.MODE_APPEND);
            for(int i=0;i<dates.size();i++) {
                fOut.write((dates.get(i) + "\n").getBytes());
            }
            fOut.close();

            fOut=mContext.openFileOutput(NAMEWITHFREQ_FILE, Context.MODE_APPEND);
            for(int i=0;i<names.size();i++) {
                fOut.write((names.get(i) + "\n").getBytes());
            }
            fOut.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

