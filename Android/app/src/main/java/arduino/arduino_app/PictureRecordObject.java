package arduino.arduino_app;

import io.realm.RealmObject;

public class PictureRecordObject extends RealmObject {
    int ID;
    int FileSize;
    String HDate;
    String Time;
    int TS;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getFileSize() {
        return FileSize;
    }

    public void setFileSize(int fileSize) {
        FileSize = fileSize;
    }

    public String getHDate() {
        return HDate;
    }

    public void setHDate(String HDate) {
        this.HDate = HDate;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getTS() {
        return TS;
    }

    public void setTS(int TS) {
        this.TS = TS;
    }
}
