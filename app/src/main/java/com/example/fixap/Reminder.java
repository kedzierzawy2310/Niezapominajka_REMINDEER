package com.example.fixap;

import android.os.Parcel;
import android.os.Parcelable;

public class Reminder implements Parcelable {

    private String type;
    private String otherTypeName;
    private String startDate;
    private String endDate;
    private String madeDate;
    private String location;
    private String lostLocation;
    private String image;
    private String beaconRegion;
    private int yearStart;
    private int monthStart;
    private int dayStart;
    private int hourStart;
    private int minuteStart;
    private int yearEnd;
    private int monthEnd;
    private int dayEnd;
    private int hourEnd;
    private int minuteEnd;
    private int index;
    private int id;
    private boolean workType;
    private boolean alertActive;
    private boolean wasAutoActivated;
    private boolean checkedToDelete;
    private boolean lost;
    private boolean withLostLocation;

    Reminder(String type, int id){
        this.type = type;
        this.image = "0";
        this.beaconRegion = "0";
        this.lostLocation = "0";
        this.yearStart = 0;
        this.monthStart = 0;
        this.dayStart = 0;
        this.hourStart = -1;
        this.minuteStart = -1;
        this.yearEnd = 0;
        this.monthEnd = 0;
        this.dayEnd = 0;
        this.hourEnd = -1;
        this.minuteEnd = -1;
        this.id = id;
        this.alertActive = false;
        this.wasAutoActivated = false;
        this.lost = false;
        this.withLostLocation = false;
    }

    private Reminder(Parcel in){
        type = in.readString();
        otherTypeName = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        madeDate = in.readString();
        location = in.readString();
        lostLocation = in.readString();
        image = in.readString();
        beaconRegion = in.readString();
        yearStart = in.readInt();
        monthStart = in.readInt();
        dayStart = in.readInt();
        hourStart = in.readInt();
        minuteStart = in.readInt();
        yearEnd = in.readInt();
        monthEnd = in.readInt();
        dayEnd = in.readInt();
        hourEnd = in.readInt();
        minuteEnd = in.readInt();
        index = in.readInt();
        id = in.readInt();
        workType = in.readByte() == 1;
        alertActive = in.readByte() == 1;
        wasAutoActivated = in.readByte() == 1;
        lost = in.readByte() == 1;
        withLostLocation = in.readByte() == 1;
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(otherTypeName);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(madeDate);
        dest.writeString(location);
        dest.writeString(lostLocation);
        dest.writeString(image);
        dest.writeString(beaconRegion);
        dest.writeInt(yearStart);
        dest.writeInt(monthStart);
        dest.writeInt(dayStart);
        dest.writeInt(hourStart);
        dest.writeInt(minuteStart);
        dest.writeInt(yearEnd);
        dest.writeInt(monthEnd);
        dest.writeInt(dayEnd);
        dest.writeInt(hourEnd);
        dest.writeInt(minuteEnd);
        dest.writeInt(index);
        dest.writeInt(id);
        dest.writeByte((byte) (workType ? 1 : 0));
        dest.writeByte((byte) (alertActive ? 1 : 0));
        dest.writeByte((byte) (wasAutoActivated ? 1 : 0));
        dest.writeByte((byte) (lost ? 1 : 0));
        dest.writeByte((byte) (withLostLocation ? 1 : 0));
    }

    void setType(int i)
    {
        switch (i){
            case 1:
                this.type = "BACKPACK";
                break;
            case 2:
                this.type = "KEYS";
                break;
            case 3:
                this.type = "WALLET";
                break;
            case 4:
                this.type = "JACKET";
                break;
            default:
                    this.type = "OTHERS";
                    break;

        }
    }

    String getType(){
        return this.type;
    }

    int getYearStart() {
        return yearStart;
    }

    void setYearStart(int yearStart) {
        this.yearStart = yearStart;
    }

    int getMonthStart() {
        return monthStart;
    }

    void setMonthStart(int monthStart) {
        this.monthStart = monthStart;
    }

    int getDayStart() {
        return dayStart;
    }

    void setDayStart(int dayStart) {
        this.dayStart = dayStart;
    }

    int getHourStart() {
        return hourStart;
    }

    void setHourStart(int hourStart) {
        this.hourStart = hourStart;
    }

    int getMinuteStart() {
        return minuteStart;
    }

    void setMinuteStart(int minuteStart) {
        this.minuteStart = minuteStart;
    }

    int getYearEnd() {
        return yearEnd;
    }

    void setYearEnd(int yearEnd) {
        this.yearEnd = yearEnd;
    }

    int getMonthEnd() {
        return monthEnd;
    }

    void setMonthEnd(int monthEnd) {
        this.monthEnd = monthEnd;
    }

    int getDayEnd() {
        return dayEnd;
    }

    void setDayEnd(int dayEnd) {
        this.dayEnd = dayEnd;
    }

    int getHourEnd() {
        return hourEnd;
    }

    void setHourEnd(int hourEnd) {
        this.hourEnd = hourEnd;
    }

    int getMinuteEnd() {
        return minuteEnd;
    }

    void setMinuteEnd(int minuteEnd) {
        this.minuteEnd = minuteEnd;
    }

    String getOtherTypeName() {
        return otherTypeName;
    }

    void setOtherTypeName(String otherTypeName) {
        this.otherTypeName = otherTypeName;
    }

    String getStartDate() {
        return startDate;
    }

    void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    String getEndDate() {
        return endDate;
    }

    void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    String getMadeDate() {
        return madeDate;
    }

    void setMadeDate(String madeDate) {
        this.madeDate = madeDate;
    }

    boolean isWorkType() {
        return workType;
    }

    void setWorkType(boolean workType) {
        this.workType = workType;
    }

    String getLocation() {
        return location;
    }

    void setLocation(String location) {
        this.location = location;
    }

    String getImage() {
        return image;
    }

    void setImage(String image) {
        this.image = image;
    }

    String getBeaconRegion() {
        return beaconRegion;
    }

    void setBeaconRegion(String beaconRegion) {
        this.beaconRegion = beaconRegion;
    }

    boolean isAlertActive() {
        return alertActive;
    }

    void setAlertActive(boolean alertActive) {
        this.alertActive = alertActive;
    }

    boolean isCheckedToDelete() {
        return checkedToDelete;
    }

    void setCheckedToDelete(boolean checkedToDelete) {
        this.checkedToDelete = checkedToDelete;
    }

    boolean isLost() {
        return lost;
    }

    void setLost(boolean lost) {
        this.lost = lost;
    }

    int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    String getLostLocation() {
        return lostLocation;
    }

    void setLostLocation(String lostLocation) {
        this.lostLocation = lostLocation;
    }

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    boolean isWasAutoActivated() {
        return wasAutoActivated;
    }

    void setWasAutoActivated(boolean wasAutoActivated) {
        this.wasAutoActivated = wasAutoActivated;
    }

    boolean isWithLostLocation() {
        return withLostLocation;
    }

    void setWithLostLocation(boolean withLostLocation) {
        this.withLostLocation = withLostLocation;
    }
}
