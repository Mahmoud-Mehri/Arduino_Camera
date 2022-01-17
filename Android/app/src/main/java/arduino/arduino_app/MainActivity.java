package arduino.arduino_app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Activity mainActivity;
    TCP_Select SelectClient;
    TCP_Command CommandClient;
    TCP_GetFile GetFileClient;
    Handler handler;

    SelectTask ListSelectTask;
    DeleteTask PicDeleteTask;
    GetFileTask GetPicTask;

    RealmRecyclerViewAdapter adapter;
    RecyclerView RV;

    RealmConfiguration realmConfig;
    Realm realm;

    CustomProgressDialog progressDialog;

    File picFile;

    EditText ipEdit, portEdit;
    String Server_IP;
    int Server_Port;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        ipEdit = findViewById(R.id.ipEdit);
        portEdit = findViewById(R.id.portEdit);

        RealmConfiguration RealmConfig = new RealmConfiguration.Builder()
                .name("arduino_app.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm realm = Realm.getInstance(RealmConfig);

        adapter = new PictureListAdapter(this, realm.where(PictureRecordObject.class).findAll().sort("TS", Sort.DESCENDING), new RecyclerViewClickListener() {
            @Override
            public void onPositionClicked(int position, int picID, int Operation) {
                switch(Operation){
                    case 1:{ // Show
                        picFile = new File(getFilesDir(), "Pictures/" + Integer.toString(picID) + ".jpg");
                        if(picFile.exists()){
                            Intent showActivity = new Intent(MainActivity.this, ShowActivity.class);
                            showActivity.putExtra("FileAddr", picFile.getAbsolutePath());
                            startActivity(showActivity);
                        }else {
                            GetPicTask = new GetFileTask();
                            GetPicTask.execute(picID);
                        }
                    }break;
                    case 2:{ // Delete
                        final int PicID = picID;
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        dialogBuilder.setMessage("تصویر انتخاب شده حذف شود ؟");
                        dialogBuilder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PicDeleteTask = new DeleteTask();
                                PicDeleteTask.execute(Integer.toString(PicID), "DEL_PIC#"+Integer.toString(PicID));

                                dialogInterface.dismiss();
                            }
                        });

                        dialogBuilder.setNegativeButton("خیر", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        dialogBuilder.setCancelable(false);

                        AlertDialog dialog = dialogBuilder.create();
                        dialog.show();
                    }break;
                }
            }

            @Override
            public void onLongClicked(int position) {

            }
        });
        RV = findViewById(R.id.mainRecyclerView);
        RV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RV.setAdapter(adapter);

        progressDialog = new CustomProgressDialog(this);

//        ListSelectTask = new SelectTask();
//        ListSelectTask.execute("GET_LIST");

        ImageButton Btn = findViewById(R.id.refreshBtn);
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListSelectTask = new SelectTask();
                ListSelectTask.execute("GET_LIST");
            }
        });

        Btn = findViewById(R.id.delAllBtn);
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setMessage("تمامی تصاویر حذف شوند ؟");
                dialogBuilder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PicDeleteTask = new DeleteTask();
                        PicDeleteTask.execute("0", "DEL_ALL");

                        dialogInterface.dismiss();
                    }
                });

                dialogBuilder.setNegativeButton("خیر", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                dialogBuilder.setCancelable(false);

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch(message.what){
                    case TCP_Select.MSG_ERROR_SEND:{
                        Snackbar snackBar = Snackbar.make(findViewById(R.id.mainLayout), message.getData().getString("MSG"), Snackbar.LENGTH_LONG);
                        ViewCompat.setLayoutDirection(snackBar.getView(), ViewCompat.LAYOUT_DIRECTION_RTL);
                        TextView sText = (TextView)snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        sText.setTextColor(getResources().getColor(android.R.color.white));
                        snackBar.show();
                    }break;
                    case TCP_Select.MSG_ERROR_SERVER:{
                        Snackbar snackBar = Snackbar.make(findViewById(R.id.mainLayout), message.getData().getString("MSG"), Snackbar.LENGTH_LONG);
                        ViewCompat.setLayoutDirection(snackBar.getView(), ViewCompat.LAYOUT_DIRECTION_RTL);
                        TextView sText = (TextView)snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        sText.setTextColor(getResources().getColor(android.R.color.white));
                        snackBar.show();
                    }break;
                    case TCP_Select.MSG_SUCCESS:{
                        Snackbar snackBar = Snackbar.make(findViewById(R.id.mainLayout), message.getData().getString("MSG"), Snackbar.LENGTH_LONG);
                        ViewCompat.setLayoutDirection(snackBar.getView(), ViewCompat.LAYOUT_DIRECTION_RTL);
                        TextView sText = (TextView)snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        sText.setTextColor(getResources().getColor(android.R.color.white));
                        snackBar.show();

                        Bundle B = message.getData();
                        switch(B.getInt("Operation")){
                            case 1:{ // Select

                            }break;
                            case 2:{ // Delete

                            }break;
                            case 3:{ // GetFile

                            }break;
                        }
                    }break;
                    case TCP_GetFile.MSG_FILE_SIZE:{

                    }break;
                    case TCP_GetFile.MSG_FILE_PROGRESS:{

                    }break;
                }

//                Snackbar snackBar = Snackbar.make(findViewById(R.id.mainLayout), message.getData().getString("MSG"), Snackbar.LENGTH_LONG);
//                ViewCompat.setLayoutDirection(snackBar.getView(), ViewCompat.LAYOUT_DIRECTION_RTL);
//                TextView sText = (TextView)snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
//                sText.setTextColor(getResources().getColor(android.R.color.white));
//                snackBar.show();

                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if((progressDialog != null) && (progressDialog.isShowing())){
            progressDialog.dismiss();
        }
    }

    private class SelectTask extends AsyncTask<String, String, TCP_Select> {
        @Override
        protected void onPreExecute() {
            Server_IP = ipEdit.getText().toString();
            Server_Port = Integer.parseInt(portEdit.getText().toString());

            progressDialog.show();
        }

        protected TCP_Select doInBackground(String... params) {

            SelectClient = new TCP_Select(Server_IP, Server_Port, getApplicationContext(), params[0],null, handler);
            SelectClient.Run();

            return SelectClient;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(TCP_Select result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }

    private class DeleteTask extends AsyncTask<String, String, TCP_Command> {
        @Override
        protected void onPreExecute() {
            Server_IP = ipEdit.getText().toString();
            Server_Port = Integer.parseInt(portEdit.getText().toString());

            progressDialog.show();
        }

        protected TCP_Command doInBackground(String... params) {

            CommandClient = new TCP_Command(Server_IP, Server_Port, getApplicationContext(), Integer.parseInt(params[0]), params[1], null, handler);
            CommandClient.Run();

            return CommandClient;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(TCP_Command result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }

    private class GetFileTask extends AsyncTask<Integer, String, TCP_GetFile> {
        @Override
        protected void onPreExecute() {
            Server_IP = ipEdit.getText().toString();
            Server_Port = Integer.parseInt(portEdit.getText().toString());

            progressDialog.show();
        }

        protected TCP_GetFile doInBackground(Integer... params) {

            GetFileClient = new TCP_GetFile(Server_IP, Server_Port, getApplicationContext(), params[0], handler);
            GetFileClient.Run();

            return GetFileClient;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(TCP_GetFile result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            Intent showActivity = new Intent(MainActivity.this, ShowActivity.class);
            showActivity.putExtra("FileAddr", picFile.getAbsolutePath());
            startActivity(showActivity);
        }
    }
}
