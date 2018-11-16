package eekelder.digitalewerkbon;

import java.io.Serializable;

public class EmployeeTimes implements Serializable { //under task--> document task --> new collection eqmployeetimes --> document THIS.CLASS

    public int startTime;
    public int endTime;
    public int pauseTime;
    public int pauseStartTime;
    public long taskNumber;
    public String remarks;
    public String outputs;
    public String employee;
    public int dayNumber;

    public int effectiveWorkTime;

    public EmployeeTimes(){
    }

    public int getEffectiveWorkTime() {
        int time;
        if (effectiveWorkTime==0) {
             time = getEndTime() - getStartTime() - getPauseTime();
        } else {
            time = effectiveWorkTime;
        }
        return time;
    }

    public void setEffectiveWorkTime(int effectiveWorkTime) {
        this.effectiveWorkTime = effectiveWorkTime;
    }

    public int getPauseStartTime() {
        return pauseStartTime;
    }

    public void setPauseStartTime(int pauseStartTime) {
        this.pauseStartTime = pauseStartTime;
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
        this.effectiveWorkTime = 0;
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(int pauseTime) {
        this.pauseTime = pauseTime;
    }

    public long getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(long taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getOutputs() {
        return outputs;
    }

    public void setOutputs(String outputs) {
        this.outputs = outputs;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDocumentName() {

        String documentName = String.valueOf(dayNumber) + employee;

        return documentName;
    }



}
