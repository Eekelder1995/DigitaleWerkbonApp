package eekelder.digitalewerkbon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Task  implements Serializable {

    public String description;
    public String remarks;
    public String client;
    public int startTime;
    public int endTime;
    public ArrayList<String> employees = new ArrayList<>();
    public List<String> equipment = new ArrayList<>();

    public int dayNumber;
    public int dayNumberYear;

    public long documentName;

    public int status; // 1 = not started, 2 = busy, 3 = finished;

    public Task(){
    }


    public long getDocumentName() {
        return documentName;
    }

    public void setDocumentName(long i) {
        this.documentName = i;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getDayNumberYear() {
        return dayNumberYear;
    }

    public void setDayNumberYear(int dayNumberYear) {
        this.dayNumberYear = dayNumberYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public ArrayList<String> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<String> employees) {
        this.employees = employees;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
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
        this.endTime = endTime;
    }

}
