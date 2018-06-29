package com.example.slidingtab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ActivityEditPlant extends AppCompatActivity {
    private Button beditsaveplant;
    private EditText eteditplantname;
    private EditText eteditplantfrequency;
    private Button beditchooseimage;
    private ImageView iveditpicture;
    private String intentstringname;
    private String intentstringfrequency;
    private String intentstringpicture;
    private String pickedDate;
    private String sDay;
    private String sMonth;
    private DatePicker dpeditpickeddate;
    private static final int GALLERY_INTENT = 2;
    private Uri pictPath;
    private Context mContext;
    private FirebaseUserClient fbUClient = new FirebaseUserClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editplant);

        mContext = ActivityEditPlant.this;

        beditsaveplant = (Button) findViewById(R.id.BEditSavePlant);
        eteditplantname = (EditText) findViewById(R.id.ETEditPlantname);
        eteditplantfrequency = (EditText) findViewById(R.id.ETEditEnterFrequency);
        beditchooseimage = (Button) findViewById(R.id.BEditChooseImage);
        iveditpicture = (ImageView) findViewById(R.id.IVEditPicture);

        Toolbar toolbar = findViewById(R.id.edittoolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityEditPlant.this.onBackPressed();
            }
        });

        //initialize the DatePicker
        dpeditpickeddate = (DatePicker) findViewById(R.id.DPEditPickeddate);
        dpeditpickeddate.setSpinnersShown(true);
        dpeditpickeddate.setCalendarViewShown(false);

        intentstringname = getIntent().getExtras().getString("name");
        intentstringfrequency = getIntent().getExtras().getString("frequency");

        eteditplantname.setHint(intentstringname);
        eteditplantfrequency.setHint(intentstringfrequency);

        StorageReference stRef = FirebaseStorage.getInstance().getReference();

        StorageReference picRef = stRef.child(intentstringname);
        picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                intentstringpicture = downloadUrl.toString();
                Picasso.get().load(intentstringpicture).placeholder(R.mipmap.ic_launcher).into(iveditpicture);
            }
        });

        beditchooseimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent IEditChoose = new Intent(Intent.ACTION_PICK);
                IEditChoose.setType("image/*");
                startActivityForResult(IEditChoose, GALLERY_INTENT);
            }
        });

        beditsaveplant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int day = dpeditpickeddate.getDayOfMonth();
                int month = dpeditpickeddate.getMonth() +1;
                int year = dpeditpickeddate.getYear();

                //String to align date: "day.month.year"
                if((month < 10) && (day < 10)){
                    sMonth = "0" + month;
                    sDay  = "0" + day ;
                    pickedDate = sDay + "." + sMonth + "." + year;
                }
                else if((day < 10) && (month >= 10)){
                    sDay  = "0" + day ;
                    pickedDate = sDay + "." + month + "." + year;
                }
                else if((day >= 10) && (month < 10)){
                    sMonth = "0" + month;
                    pickedDate = day + "." + sMonth + "." + year;
                }
                else pickedDate = day + "." + month + "." + year;

                String newName = eteditplantname.getText().toString();
                String newDate = pickedDate;
                String newFrequency = eteditplantfrequency.getText().toString();
                String newPicture = "";

                PlantUsercreated p = new PlantUsercreated(newName,newDate,newFrequency,newPicture);
                fbUClient.alertSavePlant(mContext, p, pictPath, intentstringname,
                        intentstringfrequency, eteditplantname, eteditplantfrequency);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==  GALLERY_INTENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pictPath = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pictPath);

                iveditpicture.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
