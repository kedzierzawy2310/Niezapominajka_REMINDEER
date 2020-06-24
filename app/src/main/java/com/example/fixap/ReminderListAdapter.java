package com.example.fixap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import static com.example.fixap.BeaconApplication.activeReminderList;
import static com.example.fixap.BeaconApplication.availableBeacons;
import static com.example.fixap.MainActivity.activityFunctions;

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ViewHolder>{

    private final List<Reminder> reminderList;
    private final ReminderListFragment.OnReminderListFragmentInteractionListener mListener;
    private NotificationManagerCompat managerCompat;

    ReminderListAdapter(List<Reminder> reminderList, ReminderListFragment.OnReminderListFragmentInteractionListener mListener){
        this.reminderList = reminderList;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        holder.reminderDetailsCardView.setVisibility(View.GONE);
        holder.mItem = reminderList.get(position);

        if(!holder.mItem.getType().equals("OTHERS")) holder.textView.setText(holder.mItem.getType());
        else holder.textView.setText(holder.mItem.getOtherTypeName());

        if(!holder.mItem.isAlertActive()){
            holder.reminderLinearLayout.setBackgroundResource(R.drawable.not_active_button_design);
            holder.textView.setTextColor(Color.parseColor("#000000"));
            switch (holder.mItem.getType()){
                case "BACKPACK":
                    holder.imageView.setImageResource(R.drawable.backpack_bw);
                    break;
                case "KEYS":
                    holder.imageView.setImageResource(R.drawable.key_bw);
                    break;
                case "JACKET":
                    holder.imageView.setImageResource(R.drawable.jacket_bw);
                    break;
                case "WALLET":
                    holder.imageView.setImageResource(R.drawable.wallet_bw);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.question_mark_bw);
                    break;
            }
            holder.typeTextView.setTextColor(Color.parseColor("#000000"));
            holder.dateOfWorkingTextView.setTextColor(Color.parseColor("#000000"));
            holder.dateOfMadeTextView.setTextColor(Color.parseColor("#000000"));
            holder.locationTextView.setTextColor(Color.parseColor("#000000"));
            holder.assignedBeaconTextView.setTextColor(Color.parseColor("#000000"));
            holder.typeTitleTextView.setTextColor(Color.parseColor("#000000"));
            holder.dateOfWorkingTitleTextView.setTextColor(Color.parseColor("#000000"));
            holder.dateOfMadeTitleTextView.setTextColor(Color.parseColor("#000000"));
            holder.locationTitleTextView.setTextColor(Color.parseColor("#000000"));
            holder.assignedBeaconTitleTextView.setTextColor(Color.parseColor("#000000"));
            holder.photoButton.setTextColor(Color.parseColor("#000000"));
            holder.photoButton.setBackgroundResource(R.drawable.not_active_button_design_vol_2);
        }
        else {
            if(holder.mItem.isLost()){
                holder.reminderLinearLayout.setBackgroundResource(R.drawable.alert_button_design);
                holder.textView.setTextColor(Color.parseColor("#000000"));
                holder.aSwitch.setClickable(false);
            }
            else {
                holder.reminderLinearLayout.setBackgroundResource(R.drawable.button_design_vol_2);
                holder.textView.setTextColor(Color.parseColor("#272759"));
                holder.aSwitch.setClickable(true);
            }
            switch (reminderList.get(position).getType()){
                case "BACKPACK":
                    holder.imageView.setImageResource(R.drawable.backpack);
                    break;
                case "KEYS":
                    holder.imageView.setImageResource(R.drawable.key);
                    break;
                case "JACKET":
                    holder.imageView.setImageResource(R.drawable.jacket);
                    break;
                case "WALLET":
                    holder.imageView.setImageResource(R.drawable.wallet);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.question_mark);
                    break;
            }
            holder.typeTextView.setTextColor(Color.parseColor("#272759"));
            holder.dateOfWorkingTextView.setTextColor(Color.parseColor("#272759"));
            holder.dateOfMadeTextView.setTextColor(Color.parseColor("#272759"));
            holder.locationTextView.setTextColor(Color.parseColor("#272759"));
            holder.assignedBeaconTextView.setTextColor(Color.parseColor("#272759"));
            holder.typeTitleTextView.setTextColor(Color.parseColor("#272759"));
            holder.dateOfWorkingTitleTextView.setTextColor(Color.parseColor("#272759"));
            holder.dateOfMadeTitleTextView.setTextColor(Color.parseColor("#272759"));
            holder.locationTitleTextView.setTextColor(Color.parseColor("#272759"));
            holder.assignedBeaconTitleTextView.setTextColor(Color.parseColor("#272759"));
            holder.photoButton.setTextColor(Color.parseColor("#272759"));
            holder.photoButton.setBackgroundResource(R.drawable.button_design_vol_2);
        }

        holder.dateOfWorkingTextView.setText(holder.mItem.getStartDate() + " - " + holder.mItem.getEndDate());
        holder.dateOfMadeTextView.setText(holder.mItem.getMadeDate());
        if(holder.mItem.isWorkType()) {
            holder.typeTextView.setText("ALL TIME");
            holder.dateOfWorkingRelativeLayout.setVisibility(View.GONE);
        }
        else {
            holder.typeTextView.setText("FOR A WHILE");
            holder.dateOfWorkingRelativeLayout.setVisibility(View.VISIBLE);
        }
        holder.assignedBeaconTextView.setText(holder.mItem.getBeaconRegion());
        holder.locationTextView.setText(holder.mItem.getLocation());
        if(holder.mItem.getLocation().equals("0")) holder.locationRelativeLayout.setVisibility(View.GONE);
        else holder.locationRelativeLayout.setVisibility(View.VISIBLE);

        if(!mListener.checkIsDeleting()){
            holder.aSwitch.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
            holder.mItem.setCheckedToDelete(false);
        }
        else {
            holder.aSwitch.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(holder.mItem.isCheckedToDelete());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.mItem.setCheckedToDelete(isChecked);
                if(holder.mItem.isCheckedToDelete()) {
                    mListener.putChecked(holder.mItem);
                }
                else {
                    mListener.pullChecked(holder.mItem);
                }
            }
        });

        holder.aSwitch.setOnCheckedChangeListener(null);
        holder.aSwitch.setChecked(holder.mItem.isAlertActive());
        if(!holder.mItem.isWasAutoActivated()) {
            holder.aSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "CANNOT ACTIVATE THIS ITEM", Snackbar.LENGTH_SHORT).show();
                    holder.aSwitch.setChecked(false);
                    notifyDataSetChanged();
                }
            });
            holder.aSwitch.setOnCheckedChangeListener(null);
        } else if(!bluetoothAdapter.isEnabled()){
            holder.aSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "TURN ON BLUETOOTH", Snackbar.LENGTH_SHORT).show();
                    holder.aSwitch.setChecked(false);
                    notifyDataSetChanged();
                }
            });
            holder.aSwitch.setOnCheckedChangeListener(null);
        } else {
            holder.aSwitch.setOnClickListener(null);
            holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    holder.mItem.setAlertActive(isChecked);
                    if (!holder.mItem.isAlertActive()) {
                        holder.reminderLinearLayout.setBackgroundResource(R.drawable.not_active_button_design);
                        holder.textView.setTextColor(Color.parseColor("#000000"));
                        switch (holder.mItem.getType()) {
                            case "BACKPACK":
                                holder.imageView.setImageResource(R.drawable.backpack_bw);
                                break;
                            case "KEYS":
                                holder.imageView.setImageResource(R.drawable.key_bw);
                                break;
                            case "JACKET":
                                holder.imageView.setImageResource(R.drawable.jacket_bw);
                                break;
                            case "WALLET":
                                holder.imageView.setImageResource(R.drawable.wallet_bw);
                                break;
                            default:
                                holder.imageView.setImageResource(R.drawable.question_mark_bw);
                                break;
                        }
                        holder.typeTextView.setTextColor(Color.parseColor("#000000"));
                        holder.dateOfWorkingTextView.setTextColor(Color.parseColor("#000000"));
                        holder.dateOfMadeTextView.setTextColor(Color.parseColor("#000000"));
                        holder.locationTextView.setTextColor(Color.parseColor("#000000"));
                        holder.assignedBeaconTextView.setTextColor(Color.parseColor("#000000"));
                        holder.typeTitleTextView.setTextColor(Color.parseColor("#000000"));
                        holder.dateOfWorkingTitleTextView.setTextColor(Color.parseColor("#000000"));
                        holder.dateOfMadeTitleTextView.setTextColor(Color.parseColor("#000000"));
                        holder.locationTitleTextView.setTextColor(Color.parseColor("#000000"));
                        holder.assignedBeaconTitleTextView.setTextColor(Color.parseColor("#000000"));
                        holder.photoButton.setTextColor(Color.parseColor("#000000"));
                        holder.photoButton.setBackgroundResource(R.drawable.not_active_button_design_vol_2);
                        activeReminderList.remove(holder.mItem);
                        Intent intent = new Intent(holder.mView.getContext(), MyBeaconService.class);
                        holder.mView.getContext().stopService(intent);
                        availableBeacons.clear();
                        if (!activeReminderList.isEmpty())
                            ContextCompat.startForegroundService(holder.mView.getContext(), intent);
                        else {
                            managerCompat = NotificationManagerCompat.from(holder.mView.getContext());
                            managerCompat.cancelAll();
                        }
                    } else {
                        holder.reminderLinearLayout.setBackgroundResource(R.drawable.button_design_vol_2);
                        holder.textView.setTextColor(Color.parseColor("#272759"));
                        switch (reminderList.get(position).getType()) {
                            case "BACKPACK":
                                holder.imageView.setImageResource(R.drawable.backpack);
                                break;
                            case "KEYS":
                                holder.imageView.setImageResource(R.drawable.key);
                                break;
                            case "JACKET":
                                holder.imageView.setImageResource(R.drawable.jacket);
                                break;
                            case "WALLET":
                                holder.imageView.setImageResource(R.drawable.wallet);
                                break;
                            default:
                                holder.imageView.setImageResource(R.drawable.question_mark);
                                break;
                        }
                        holder.typeTextView.setTextColor(Color.parseColor("#272759"));
                        holder.dateOfWorkingTextView.setTextColor(Color.parseColor("#272759"));
                        holder.dateOfMadeTextView.setTextColor(Color.parseColor("#272759"));
                        holder.locationTextView.setTextColor(Color.parseColor("#272759"));
                        holder.assignedBeaconTextView.setTextColor(Color.parseColor("#272759"));
                        holder.typeTitleTextView.setTextColor(Color.parseColor("#272759"));
                        holder.dateOfWorkingTitleTextView.setTextColor(Color.parseColor("#272759"));
                        holder.dateOfMadeTitleTextView.setTextColor(Color.parseColor("#272759"));
                        holder.locationTitleTextView.setTextColor(Color.parseColor("#272759"));
                        holder.assignedBeaconTitleTextView.setTextColor(Color.parseColor("#272759"));
                        holder.photoButton.setTextColor(Color.parseColor("#272759"));
                        holder.photoButton.setBackgroundResource(R.drawable.button_design_vol_2);
                        activeReminderList.add(holder.mItem);
                        Intent intent = new Intent(holder.mView.getContext(), MyBeaconService.class);
                        holder.mView.getContext().stopService(intent);
                        availableBeacons.clear();
                        ContextCompat.startForegroundService(holder.mView.getContext(), intent);
                    }
                }
            });
        }

        if(holder.mItem.isLost()) holder.aSwitch.setClickable(false);
        else holder.aSwitch.setClickable(true);

        if(holder.mItem.getImage().isEmpty() || holder.mItem.getImage().equals("0")){
            holder.photoButton.setText("ADD PHOTO");
        }
        else holder.photoButton.setText("SHOW PHOTO");

        holder.photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext().getApplicationContext(), R.anim.cardview_click_animation));
                if(holder.mItem.getImage().equals("0")){
                    mListener.getItemToTakeAPhoto(holder.mItem);
                    activityFunctions.dispatchTakePictureIntent(holder.mView.getContext(), holder.mItem);
                }
                else {
                    Intent intent = new Intent(holder.mView.getContext(), PhotoActivity.class);
                    intent.putExtra("reminderImage", holder.mItem);
                    holder.mView.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        final ImageView imageView;
        final TextView textView;
        final TextView typeTextView;
        final TextView dateOfWorkingTextView;
        final TextView dateOfMadeTextView;
        final TextView locationTextView;
        final TextView assignedBeaconTextView;
        final TextView typeTitleTextView;
        final TextView dateOfWorkingTitleTextView;
        final TextView dateOfMadeTitleTextView;
        final TextView locationTitleTextView;
        final TextView assignedBeaconTitleTextView;
        final Switch aSwitch;
        final CheckBox checkBox;
        final CardView reminderCardView;
        final CardView reminderDetailsCardView;
        final RelativeLayout dateOfWorkingRelativeLayout;
        final RelativeLayout locationRelativeLayout;
        final LinearLayout reminderLinearLayout;
        final Button photoButton;
        Reminder mItem;

        ViewHolder(View view){
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.reminderImage);
            textView = view.findViewById(R.id.reminderText);
            typeTextView = view.findViewById(R.id.typeOfWorkInfo);
            typeTitleTextView = view.findViewById(R.id.typeOfWorkTitle);
            dateOfWorkingTextView = view.findViewById(R.id.dateOfWorkingInfo);
            dateOfWorkingTitleTextView = view.findViewById(R.id.dateOfWorkingTitle);
            dateOfMadeTextView = view.findViewById(R.id.dateOfMadeInfo);
            dateOfMadeTitleTextView = view.findViewById(R.id.dateOfMadeTitle);
            locationTextView = view.findViewById(R.id.addLocationInfo);
            locationTitleTextView = view.findViewById(R.id.addLocationTitle);
            assignedBeaconTextView = view.findViewById(R.id.assignedBeaconInfo);
            assignedBeaconTitleTextView = view.findViewById(R.id.assignedBeaconTitle);
            aSwitch = view.findViewById(R.id.switch1);
            checkBox = view.findViewById(R.id.checkbox);
            reminderCardView = view.findViewById(R.id.reminderCardView);
            reminderDetailsCardView = view.findViewById(R.id.reminderDetailsCardView);
            dateOfWorkingRelativeLayout = view.findViewById(R.id.dateOfWorking);
            locationRelativeLayout = view.findViewById(R.id.addLocation);
            reminderLinearLayout = view.findViewById(R.id.reminderLinearLayout);
            photoButton = view.findViewById(R.id.showPhotoButton);
            reminderDetailsCardView.setVisibility(View.GONE);
            checkBox.setChecked(false);
            aSwitch.setChecked(true);


            reminderCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.cardview_click_animation));
                    if(!mItem.isLost()) {
                        if (reminderDetailsCardView.getVisibility() == View.VISIBLE) {
                            reminderDetailsCardView.animate().translationY(-1).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    reminderDetailsCardView.setVisibility(View.GONE);
                                }
                            }).setStartDelay(250);
                        } else {
                            reminderDetailsCardView.setVisibility(View.VISIBLE);
                            reminderDetailsCardView.setAlpha(0.0f);
                            reminderDetailsCardView.animate().translationY(0).alpha(1.0f).setListener(null).setDuration(250);
                        }
                    }
                    else{
                        Intent intent = new Intent(mView.getContext(), ReminderAlert.class);
                        intent.putExtra("alert", mItem);
                        mView.getContext().startActivity(intent);
                    }
                }
            });
        }
    }
}
