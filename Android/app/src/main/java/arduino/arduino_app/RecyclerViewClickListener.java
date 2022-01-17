package arduino.arduino_app;

public interface RecyclerViewClickListener {

    void onPositionClicked(int position, int picID, int operation);

    void onLongClicked(int position);
}

