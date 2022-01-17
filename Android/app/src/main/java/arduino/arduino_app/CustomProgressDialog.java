package arduino.arduino_app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class CustomProgressDialog extends Dialog {

    Context mContext;
    ImageView progressImg;
    AnimationDrawable progressAnimation;

    public CustomProgressDialog(Context context){
        super(context);
        mContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progressdialoglayout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        progressImg = findViewById(R.id.progressDialogImg);
        progressAnimation = (AnimationDrawable)progressImg.getBackground();

    }

    @Override
    protected void onStart() {
        super.onStart();

        progressAnimation.start();
    }

    @Override
    protected void onStop() {
        super.onStop();

        progressAnimation.stop();
    }
}
