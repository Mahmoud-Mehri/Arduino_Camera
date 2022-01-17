package arduino.arduino_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ShowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        String FilePath = getIntent().getStringExtra("FileAddr");
        if((FilePath != null) && (!FilePath.equals(""))){
            Uri ImgUri = Uri.parse(FilePath);
            ImageView img = findViewById(R.id.showImg);
            img.setImageURI(ImgUri);
//            BitmapFactory.Options opt = new BitmapFactory.Options();
//            opt.inJustDecodeBounds = true;
//            Bitmap bmp = BitmapFactory.decodeFile(FilePath, opt);
//            img.setImageBitmap(bmp);
        }
    }
}
