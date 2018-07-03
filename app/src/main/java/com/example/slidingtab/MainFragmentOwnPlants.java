package com.example.slidingtab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragmentOwnPlants.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragmentOwnPlants#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragmentOwnPlants extends Fragment {
    ListView lvownplantsusercreated;
    FirebaseListAdapter listAdapter;

    private OnFragmentInteractionListener mListener;

    public MainFragmentOwnPlants() {
        // Required empty public constructor
    }

    public static MainFragmentOwnPlants newInstance(String param1, String param2) {
        MainFragmentOwnPlants fragment = new MainFragmentOwnPlants();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        listAdapter.stopListening();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view =  inflater.inflate(R.layout.fragment_own_plants,
                container, false);

        lvownplantsusercreated = (ListView) view.findViewById(R.id.LVownplantsUserCreated);

        //When you press the button you start the other activtity CreatePlantActivity
        final Button bcreate = (Button) view.findViewById(R.id.BCreatePlant);
        bcreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Icreate = new Intent(getActivity(), ActivityCreatePlant.class);
                startActivity(Icreate);
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("Plants");
        query.orderByKey();

        final FirebaseListOptions<PlantUsercreated> plants = new FirebaseListOptions.Builder<PlantUsercreated>()
                .setLayout(R.layout.list_layout_ownplantsusercreated)
                .setLifecycleOwner(MainFragmentOwnPlants.this)
                .setQuery(query, PlantUsercreated.class)
                .build();


        listAdapter = new FirebaseListAdapter(plants) {
            @Override
            protected void populateView(View view, Object model, final int position) {
                TextView tvlayoutpname = (TextView) view.findViewById(R.id.TVlayoutpName);
                TextView tvlayoutpdate =  (TextView) view.findViewById(R.id.TVlayoutpDate);
                TextView tvlayoutpfrequency = (TextView) view.findViewById(R.id.TVlayoutpFrequency);
                Button blayouteditplant = (Button) view.findViewById(R.id.BLayoutEditPlant);

                final PlantUsercreated plantUsercreated = (PlantUsercreated) model;
                tvlayoutpname.setText(plantUsercreated.getName());
                tvlayoutpdate.setText(plantUsercreated.getDate());
                tvlayoutpfrequency.setText(plantUsercreated.getFrequency());

                blayouteditplant.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent IEdit = new Intent(v.getContext(), ActivityEditPlant.class);
                        IEdit.putExtra("name", plantUsercreated.getName());
                        IEdit.putExtra("frequency", plantUsercreated.getFrequency());
                        startActivity(IEdit);
                    }
                });
            }
        };

        lvownplantsusercreated.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvName = view.findViewById(R.id.TVlayoutpName);
                TextView tvDate = view.findViewById(R.id.TVlayoutpDate);
                TextView tvFrequency = view.findViewById(R.id.TVlayoutpFrequency);

                String stringname = tvName.getText().toString();
                String stringdate = tvDate.getText().toString();
                String stringfrequency = tvFrequency.getText().toString();

                Intent intent = new Intent(view.getContext(), ActivityShowOwnPlant.class);
                intent.putExtra("name", stringname);
                intent.putExtra("date", stringdate);
                intent.putExtra("frequency", stringfrequency);

                startActivity(intent);
            }
        });
        lvownplantsusercreated.setAdapter(listAdapter);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
