package tn.erikaserquina.remindme.classes;

import java.sql.Struct;
import java.util.Date;

public class Reminder {
    String message,email,id;
    String remindDate;
    public Reminder(){

    }

    public Reminder(String message, String email, String id, String remindDate) {
        this.message = message;
        this.email = email;
        this.id = id;
        this.remindDate = remindDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemindDate() {
        return remindDate;
    }

    public void setRemindDate(String remindDate) {
        this.remindDate = remindDate;
    }
}
