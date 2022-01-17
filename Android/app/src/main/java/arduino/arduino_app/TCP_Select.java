package arduino.arduino_app;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class TCP_Select {

    private String recordDelimiter = "Ï";
    private String fieldDelimiter = "Ä";

    private Context context;
    private String Cmd, response = "";
    private MessageCallback Listener;
    private boolean Running = false;
    private final Handler Hdlr;
    BufferedReader In;
    PrintWriter Out;
    private String TAG = "TCPClient";
    private int Err = 0;
    public static boolean Exit;

    private Message Msg;
    Bundle B;

    private String[] RecordList, ValueList;

    private Socket S;

    public static final int MSG_SUCCESS = 1,
            MSG_ERROR_SERVER = 2,
            MSG_ERROR_SEND = 3;

    private int SERVER_PORT = 0;
    private String SERVER_IP = "";

    public TCP_Select(String IP, int Port, Context ctx, String CMD, MessageCallback MsgCB, Handler H)
    {
        context = ctx;
        Cmd = CMD;
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
            if(response.equals("1"))
            {
                Log.d(TAG, "Preparing for Getting Info");
                response = In.readLine();
                response = response.replace("\\n", "\n");
                Log.d(TAG, "Info Recieved : " + response);

                RealmConfiguration RealmConfig = new RealmConfiguration.Builder()
                        .name("arduino_app.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm realm = Realm.getInstance(RealmConfig);

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(PictureRecordObject.class).findAll().deleteAllFromRealm();
                    }
                });

                if(!response.equals("3")){
                    RecordList = response.split(recordDelimiter);
                    for (int i = 0; i < RecordList.length; i++) {
                        ValueList = RecordList[i].split(fieldDelimiter);

                        if (Exit) {
                            return;
                        }

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                PictureRecordObject Obj = realm.createObject(PictureRecordObject.class);
                                Obj.setID(Integer.parseInt(ValueList[0]));
                                Obj.setFileSize(Integer.parseInt(ValueList[1]));
                                Obj.setHDate(ValueList[2]);
                                Obj.setTime(ValueList[3]);
//                                Obj.setTS(Integer.parseInt(ValueList[4]));
                            }
                        });
                    }
                }
            }
            else
            {
                if(response.equals("2")){
                    B.putCharSequence("MSG", "خطا در دریافت اطلاعات در سمت سرور");
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
            if(Err == 0)
            {
                B.putCharSequence("MSG", "اطلاعات با موفقیت دریافت شد");
                B.putInt("Operation", 1);
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

