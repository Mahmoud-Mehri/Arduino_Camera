package arduino.arduino_app;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Arduino_App extends Application {

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/sahel_fd.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
