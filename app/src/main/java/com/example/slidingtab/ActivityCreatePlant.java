package com.example.slidingtab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ActivityCreatePlant extends AppCompatActivity{

    //the user has to create a new plant with a name (etplantname),
    //a date on which the user waters his plant the first/next time (dppickeddate)
    //and a frequency (how many days until next watering) (etenterfrequency)
    private EditText etplantname;
    private EditText etenterfrequency;
    private Button bchooseimage;
    private ImageView ivcreatepicture;
    private String sDay;
    private String sMonth;
    private Uri picPath;
    private DatePicker dpeditpickeddate;
    private MainFragmentCalendar mate= new MainFragmentCalendar();

    private static final int GALLERY_INTENT = 111;

    private FirebaseUserClient fbUClient = new FirebaseUserClient();
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createplant);

        mContext = ActivityCreatePlant.this;

        bchooseimage = (Button) findViewById(R.id.BChooseImage);
        ivcreatepicture = (ImageView) findViewById(R.id.IVCreatePicture);

        //initialize the DatePicker
        dpeditpickeddate = (DatePicker) findViewById(R.id.DPEditPickeddate);
        dpeditpickeddate.setSpinnersShown(true);
        dpeditpickeddate.setCalendarViewShown(false);

        //Backarrow on the top left of the screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCreatePlant.this.onBackPressed();
            }
        });

        bchooseimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent IChoose = new Intent(Intent.ACTION_PICK);
                IChoose.setType("image/*");
                startActivityForResult(IChoose, GALLERY_INTENT);
            }
        });

        //Read data from EditTexts and DatePicker, save as new plant and passed to savePlant()
        final AppCompatButton bsaveplant = (AppCompatButton) findViewById(R.id.BSavePlant);
        bsaveplant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etplantname = (EditText) findViewById(R.id.ETPlantname);
                etenterfrequency = (EditText) findViewById(R.id.ETEnterFrequency);

                int day = dpeditpickeddate.getDayOfMonth();
                int month = dpeditpickeddate.getMonth() + 1;
                int year = dpeditpickeddate.getYear();
                String pickedDate;

                //String to align date: "day.month.year"
                if ((month < 10) && (day < 10)) {
                    sMonth = "0" + month;
                    sDay = "0" + day;
                    pickedDate = sDay + "." + sMonth + "." + year;
                } else if ((day < 10) && (month >= 10)) {
                    sDay = "0" + day;
                    pickedDate = sDay + "." + month + "." + year;
                } else if ((day >= 10) && (month < 10)) {
                    sMonth = "0" + month;
                    pickedDate = day + "." + sMonth + "." + year;
                } else {
                    pickedDate = sDay + "." + sMonth + "." + year;
                }
                String pPicture = "";

                //Variables of a plant: Name(pName), date(pDate), frequency (pFrequency)
                String pName = etplantname.getText().toString();
                String pFrequency = etenterfrequency.getText().toString();
                String pDate = pickedDate;

                PlantUsercreated p = new PlantUsercreated(pName, pDate, pFrequency, pPicture);
                fbUClient.plantToDatabase(mContext, p, picPath, etplantname, etenterfrequency);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==  GALLERY_INTENT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            picPath = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picPath);

                ivcreatepicture.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}


