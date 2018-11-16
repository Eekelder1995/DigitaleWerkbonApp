package eekelder.digitalewerkbon;

public class WorkingHours {

    public int dayOfYearYear;
    public int dayOfYear;
    public int startTime;
    public int endTime;
    public int pauseTime;
    public int travelTime;

    public WorkingHours(){
    }

    public int getDayOfYearYear() {
        return dayOfYearYear;
    }

    public void setDayOfYearYear(int dayOfYearYear) {
        this.dayOfYearYear = dayOfYearYear;
    }

    public int getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {

        if (endTime < getStartTime()){
            endTime = endTime + 24*60;
        }
        
        this.endTime = endTime;
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(int pauseTime) {
        this.pauseTime = pauseTime;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(int travelTime) {
        this.travelTime = travelTime;
    }


}
