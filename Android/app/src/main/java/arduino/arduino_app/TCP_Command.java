package arduino.arduino_app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class TCP_Command {

    private Context context;
    private String Cmd, response = "";
    private MessageCallback Listener;
    private boolean Running = false;
    private final Handler Hdlr;
    BufferedReader In;
    PrintWriter Out;
    private String TAG = "TCP_Command";
    private int Err = 0;
    private int PicID;
    public static boolean Exit;

    private Message Msg;
    Bundle B;

    private Socket S;

    public static final int MSG_SUCCESS = 1,
            MSG_ERROR_SERVER = 2,
            MSG_ERROR_SEND = 3;

    private int SERVER_PORT = 0;
    private String SERVER_IP = "";

    public TCP_Command(String IP, int Port, Context ctx, int picID, String cmd, MessageCallback MsgCB, Handler H)
    {
        context = ctx;
        PicID = picID;
        Cmd = cmd;
        Listener = MsgCB;
        Hdlr = H;

        SERVER_IP = IP;
        SERVER_PORT = Port;

        Exit = false;
    }

    public void SendMessage(String Msg)
    {
        if(Out != null && !Out.checkError())
        {
            Out.println(Msg);
            Out.flush();
        }
    }

    public void Run()
    {
        Running = true;
        Msg = new Message();
        B = new Bundle();

        try{
//                InetAddress ServerAddr = InetAddress.getByName(SERVER_IP);
//                S = new Socket(ServerAddr, SERVER_PORT);
            S = new Socket();
            S.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 5000);
            Log.d(TAG, "Connecting ...");
            S.setSoTimeout(5000);

            BufferedInputStream BIS = new BufferedInputStream(S.getInputStream());
            Out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(S.getOutputStream())), true);
            In  = new BufferedReader(new InputStreamReader(S.getInputStream()));

            this.SendMessage(Cmd);
            response = In.readLine();
            Log.d(TAG, "Response Recieved : " + response);
            if(!response.equals("1"))
            {
                if(response.equals("2")){
                B.putCharSequence("MSG", "خطا در دریافت اطلاعات در سرور");
                }else{
                B.putCharSequence("MSG", response);
                }

                Msg.setData(B);
                Msg.what = MSG_ERROR_SERVER;
                Hdlr.sendMessage(Msg);

                Err = 1;

                Log.d(TAG, "Error on Server");
            }

            if(Out != null) {
                Out.flush();
                Out.close();
            }

            if(In != null){
                In.close();
            }
            S.close();

        }catch (Exception e)
        {
            B.putCharSequence("MSG", "خطا در دریافت اطلاعات از سرور :" + e.getMessage());
            Msg.setData(B);
            Msg.what = MSG_ERROR_SEND;
            Hdlr.sendMessage(Msg);
            Err = 1;
            Log.d(TAG, "Connect Error : ", e);
        }
        finally{
            Log.d(TAG, "Err Value is : " + Integer.toString(Err));

            File PicFile = new File(context.getFilesDir().getAbsolutePath() + Integer.toString(PicID) + ".jpg");
            if(PicFile.exists()) {
                PicFile.delete();
            }

            if(Err == 0) {
                RealmConfiguration RealmConfig = new RealmConfiguration.Builder()
                        .name("arduino_app.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm realm = Realm.getInstance(RealmConfig);

                if(PicID == 0) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(PictureRecordObject.class).equalTo("ID", PicID).findAll().deleteAllFromRealm();
                        }
                    });
                }else{
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.where(PictureRecordObject.class).equalTo("ID", PicID).findAll().deleteAllFromRealm();
                        }
                    });
                }

                B.putCharSequence("MSG", "عملیات حذف با موفقیت انجام شد");
                B.putInt("Operation", 2);
                B.putInt("PicID", PicID);
                Msg.setData(B);
                Msg.what = MSG_SUCCESS;
                Hdlr.sendMessage(Msg);
            }

            Log.d(TAG, "Sending Ends");
        }
    }


    public interface MessageCallback
    {
        public void callbackMessageReceiver(String msg);
    }

}

