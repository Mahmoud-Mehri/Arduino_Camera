package arduino.arduino_app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class TCP_GetFile {

    private Context context;
    private String Cmd, response = "";
    private boolean Running = false;
    private final Handler Hdlr;
    BufferedReader In;
    PrintWriter Out;
    FileOutputStream FOS;
    BufferedOutputStream BOS;
    private String TAG = "TCP_GETFILE";
    private int Err = 0;
    public static boolean Exit;

    int PicID;

    private Message Msg;
    Bundle B;

    private Socket S;

    public static final int MSG_SUCCESS = 1,
            MSG_ERROR_SERVER = 2,
            MSG_ERROR_SEND = 3,
            MSG_FILE_SIZE = 4,
            MSG_FILE_PROGRESS = 5;

    private int SERVER_PORT = 0;
    private String SERVER_IP = "";

    public TCP_GetFile(String IP, int Port, Context ctx, int picID, Handler H)
    {
        context = ctx;
        Hdlr = H;
        PicID = picID;
        Cmd = "GET_FILE#" + Integer.toString(PicID);
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
                response = In.readLine();
                int FileSize = Integer.parseInt(response);
                if (context.getFilesDir().getFreeSpace() < 2 * FileSize) {
                    B.clear();
                    B.putCharSequence("MSG", "فضای کافی برای ذخیره فایل وجود ندارد");
                    Msg.setData(B);
                    Msg.what = MSG_ERROR_SERVER;
                    Hdlr.sendMessage(Msg);
                } else {
                    B.putInt("FileSize", FileSize);
                    Msg.what = MSG_FILE_SIZE;
                    Msg.setData(B);
                    Hdlr.dispatchMessage(Msg);

                    try {
                        File MainDir, PicFile;

                        MainDir = new File(context.getFilesDir(), "Pictures");
                        if(!MainDir.exists())
                        {
                            MainDir.mkdir();
                            Log.d("DOWNLOAD", "Pictures Folder Created : " + MainDir.getAbsolutePath());
                        }
                        PicFile = new File(MainDir, Integer.toString(PicID) + ".jpg");

                        SendMessage("1");

                        byte[] FBytes = new byte[2048];
                        FOS = new FileOutputStream(PicFile);
                        BOS = new BufferedOutputStream(FOS);
                        int BytesRead = 0;
                        int CurrProgress = 0;

                        while (CurrProgress < FileSize){
                            Log.d("DOWNLOAD", "Available : " + Integer.toString(BIS.available()));
                            BytesRead = BIS.read(FBytes, 0, FBytes.length);
                            BOS.write(FBytes, 0, BytesRead);

                            if (BytesRead > 0)
                                CurrProgress += BytesRead;

                            B.clear();
                            B.putInt("Progress", CurrProgress);
                            Msg.what = MSG_FILE_PROGRESS;
                            Hdlr.dispatchMessage(Msg);

                            if(Exit){
                                break;
                            }

                            Log.d("DOWNLOAD", "BytesRead = " + Integer.toString(BytesRead));
                        }

                        Log.d(TAG, "Download Loop Finished");
                        if (CurrProgress != FileSize) {
                            BOS.close();
                            Log.d(TAG, "File Size Problem");
                        } else {
                            BOS.close();
                            Log.d(TAG, "File Downloaded successfully");
                        }
                    } catch (Exception E) {
                        B.putCharSequence("MSG", "خطا در دریافت اطلاعات از سرور :" + E.getMessage());
                        Msg.setData(B);
                        Msg.what = MSG_ERROR_SEND;
                        Hdlr.sendMessage(Msg);
                        Err = 1;
                        Log.d(TAG, "Get File Error : ", E);
                    }finally {
                        if(BOS != null){
                            BOS.close();
                        }
                    }
                }
            }else
            {
                B.clear();
                if(response.equals("2")) {
                    B.putCharSequence("MSG", "خطا در دریافت اطلاعات در سمت سرور");
                }else if(response.equals("3")){
                    B.putCharSequence("MSG", "فایل موردنظر در سرور موجود نیست");
                }else{
                    B.putCharSequence("MSG", response);
                }

                Msg.setData(B);
                Msg.what = MSG_ERROR_SERVER;
                Hdlr.sendMessage(Msg);

                Err = 1;
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
                B.putCharSequence("MSG", "فایل با موفقیت ذخیره شد");
                B.putInt("Operation", 3);
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

