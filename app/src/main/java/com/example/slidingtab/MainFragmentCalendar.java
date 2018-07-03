package com.example.slidingtab;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragmentCalendar.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragmentCalendar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragmentCalendar extends Fragment{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    int uniqueID = 45612;
    android.support.v7.app.NotificationCompat.Builder notBuilder;

    private String DATEWITHFREQ_FILE = "PlantDateWithFreq.txt";
    private String NAMEWITHFREQ_FILE = "PlantNameWithFreq.txt";

    private List<Event> eventList;
    private List<String> dateWithFreqList;
    private List<String> nameWithFreqList;

    private CompactCalendarView cvcalendar;
    private TextView tvcalendarmonth;
    private TextView tvcalendarclickeddate;
    private TextView tvplantstowater;
    private SimpleDateFormat sdfHead = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat sdfDateBot = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private OnFragmentInteractionListener mListener;

    public MainFragmentCalendar() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragmentCalendar.
     */
    public static MainFragmentCalendar newInstance(String param1, String param2) {
        MainFragmentCalendar fragment = new MainFragmentCalendar();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        eventList = new ArrayList<>();
        dateWithFreqList = new ArrayList<>();
        nameWithFreqList = new ArrayList<>();

        cvcalendar.removeAllEvents();

        dateWithFreqList = getDateWithFreq();
        nameWithFreqList = getNameWithFreq();

        String currTime = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());

        if(dateWithFreqList.contains(currTime)) {
            //if yes create a string with all plantnames that have to be watered that day
            long pattern[] = {100,10,100};
            StringBuilder sb1 = new StringBuilder();
            for(int i = 0; i < dateWithFreqList.size(); i++) {
                if (dateWithFreqList.get(i).equals(currTime)) {
                    sb1.append(nameWithFreqList.get(i)).append(" ");
                    if (sb1.length() == 0) {
                        tvplantstowater.setText("Keine Pflanzen vorhanden");
                    } else {
                        tvplantstowater.setText(sb1.toString());
                    }
                }
            }
            String plants = sb1.toString();
            //and then create the notification which includes this string
            notBuilder = new android.support.v7.app.NotificationCompat.Builder(getContext());
            notBuilder.setTicker("Ticker");
            notBuilder.setWhen(System.currentTimeMillis());
            notBuilder.setContentTitle("Vergiss nicht zu Bewässern");
            notBuilder.setContentText(plants);
            notBuilder.setSmallIcon(R.drawable.ic_notification_launcher);
            notBuilder.setVibrate(pattern);

            Intent intent = new Intent(getContext(), ActivityMain.class);
            PendingIntent pIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notBuilder.setContentIntent(pIntent);

            NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            try {
                nm.notify(uniqueID, notBuilder.build());
            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }

        for(int i=0;i<dateWithFreqList.size();i++) {
            GregorianCalendar gCal = new GregorianCalendar();
            try {
                Date date = sdfDateBot.parse(dateWithFreqList.get(i));
                gCal.setTime(date);
                Event event = new Event(Color.DKGRAY, gCal.getTimeInMillis(), nameWithFreqList.get(i));
                eventList.add(event);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        cvcalendar.addEvents(eventList);

        final Date date = Calendar.getInstance().getTime();
        tvcalendarmonth.setText(sdfHead.format(date));

        cvcalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                String cmp = sdfDateBot.format(dateClicked);

                if(dateWithFreqList.isEmpty()){
                    tvplantstowater.setText("Keine Pflanzen vorhanden");
                }
                else{
                    if(dateWithFreqList.contains(cmp)) {
                        StringBuilder sb1 = new StringBuilder();
                        for (int i = 0; i < dateWithFreqList.size(); i++) {
                            if (dateWithFreqList.get(i).equals(cmp)) {
                                sb1.append(nameWithFreqList.get(i)).append("\n");
                                tvplantstowater.setText(sb1.toString());
                            }
                        }
                    }
                    else{
                        tvplantstowater.setText("--Nichts--");
                    }
                }
                tvcalendarclickeddate.setText(sdfDateBot.format(dateClicked));
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                tvcalendarmonth.setText(sdfHead.format(firstDayOfNewMonth));
            }
        });

        //check if the current time equals a date given in the datelist (set plants)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_calendar,
                container, false);

        /*
        //clear files
        FileOutputStream fOut = null;
        try {
            fOut = getContext().openFileOutput(NAMEWITHFREQ_FILE, Context.MODE_PRIVATE);
            fOut.close();
            fOut = getContext().openFileOutput(DATEWITHFREQ_FILE, Context.MODE_PRIVATE);
            fOut.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        */

        cvcalendar = (CompactCalendarView) view.findViewById(R.id.CVCalendar);
        tvcalendarmonth = (TextView) view.findViewById(R.id.TVCalendarMonth);
        tvcalendarclickeddate = (TextView) view.findViewById(R.id.TVCalendarClickedDate);
        tvplantstowater = (TextView) view.findViewById(R.id.TVPlantsToWater);

        cvcalendar.setUseThreeLetterAbbreviation(true);

        // Inflate the layout for this fragment
        return view;
    }

    private ArrayList<String> getDateWithFreq(){
        FileInputStream fIn = null;
        ArrayList<String> returnList = new ArrayList<>();
        try {
            fIn = getContext().openFileInput(DATEWITHFREQ_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fIn));
            ArrayList<String> list = new ArrayList<>();

            String text;

            while((text = reader.readLine()) != null){
                list.add(text);
                list.add(" ");
            }

            returnList = list;

            fIn.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnList;
    }

    private ArrayList<String> getNameWithFreq(){
        FileInputStream fIn = null;
        ArrayList<String> returnList = new ArrayList<>();
        try {
            fIn = getContext().openFileInput(NAMEWITHFREQ_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fIn));
            ArrayList<String> list = new ArrayList<>();

            String text;

            while((text = reader.readLine()) != null){
                list.add(text);
                list.add(" ");
            }

            returnList = list;

            fIn.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

