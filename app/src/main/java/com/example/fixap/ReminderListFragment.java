package com.example.fixap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class ReminderListFragment extends Fragment {

    private OnReminderListFragmentInteractionListener mListener;

    @SuppressLint("StaticFieldLeak")
    private static RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.reminder_list_layout, container, false);

        if(view instanceof RecyclerView){
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new ReminderListAdapter(BeaconApplication.reminderList, mListener));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnReminderListFragmentInteractionListener){
            mListener = (OnReminderListFragmentInteractionListener) context;
        }
        else{
            throw new RuntimeException(context.toString() + "OnReminderListFragmentInteractionListener is not implemented");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyDataSetChanged();
    }

    public static void notifyDataSetChanged(){
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
    }

    public interface OnReminderListFragmentInteractionListener{
        void putChecked(Reminder reminder);
        void pullChecked(Reminder reminder);
        void getItemToTakeAPhoto(Reminder reminder);
        void deleteChecked ();
        void setDeleting(boolean b);
        boolean checkIsDeleting();
    }
}