unit MainUnit;

interface

uses
  Winapi.Windows, Winapi.Messages, System.SysUtils, System.Variants, System.Classes, Vcl.Graphics,
  Vcl.Controls, Vcl.Forms, Vcl.Dialogs, IdBaseComponent, IdComponent, IdStack,
  IdCustomTCPServer, IdTCPServer, IdCmdTCPServer, IdCommandHandlers, Data.DB,
  DBAccess, MyAccess, IdGlobal, AsistantUnit, Vcl.StdCtrls, LogUnit, IdContext;

type
  TMainFrm = class(TForm)
    TCPServer: TIdCmdTCPServer;
    LogMemo: TMemo;
    Label1: TLabel;
    IPLabel: TLabel;
    procedure TCPServerCommandHandlers0Command(ASender: TIdCommand);
    procedure TCPServerCommandHandlers1Command(ASender: TIdCommand);
    procedure TCPServerCommandHandlers2Command(ASender: TIdCommand);
    procedure TCPServerCommandHandlers3Command(ASender: TIdCommand);
    procedure FormCreate(Sender: TObject);
    procedure TCPServerConnect(AContext: TIdContext);
    procedure TCPServerCommandHandlers4Command(ASender: TIdCommand);
  private
    { Private declarations }
  public
    { Public declarations }
   procedure UpdateLog(var Msg : TMessage); message MSG_LOG_UPDATE;
  end;

var
  MainFrm: TMainFrm;
  Log : TLog;

implementation

{$R *.dfm}

procedure TMainFrm.FormCreate(Sender: TObject);
var
 IPs : TStringList;
 IP : String;
 I : Integer;
 Err : Boolean;
begin
 TCPServer.Greeting.Clear;

 IPs := TStringList.Create;
 try
  GStack.AddLocalAddressesToList(IPs);
  for I := 0 to IPs.Count-1 do
   begin
    IP := IPs[I];

    IPv4ToDWord(IP, Err);
    if not Err then
      Break;

    IP := '';
   end;
 finally
  IPs.Free;
 end;

 if IP <> '' then
  begin
   IPLabel.Caption := IP;
   TCPServer.Bindings.Items[0].IP := IP;
  end;

 TCPServer.DefaultPort := 1717;
 TCPServer.Bindings.Items[0].Port := 1717;
 try
  TCPServer.Active := True;
 except
  on E:Exception do
   begin
    MessageBox(Handle, PChar('خطا در فعالسازی سرور :' + #13 + E.Message), '', MB_OK+MB_ICONEXCLAMATION);
   end;
 end;

 LogFileName := ExtractFilePath(Application.ExeName) + 'Log.txt';
 Log := TLog.Create(LogFileName);
 Log.Start;
end;

procedure TMainFrm.TCPServerCommandHandlers0Command(ASender: TIdCommand);
var
 Connection : TMyConnection;
 SP : TMyStoredProc;
 IP, FileName, PicDir : String;
 FileSize, I : Integer;
 B : Boolean;
 MS : TMemoryStream;
begin
 { ADD_PIC }

 B := True;

 ASender.Context.Connection.IOHandler.DefStringEncoding := IndyTextEncoding_UTF8;
 ASender.PerformReply := False;

 IP := ASender.Context.Connection.Socket.Binding.PeerIP;
 try
  FileSize := StrToInt(ASender.Params.Strings[0]);
 except
  B := False;
  ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
 end;

 if B = False then
  Exit;

 Connection := TMyConnection.Create(nil);
 Connection.ConnectString := ConnectString;
 Connection.Options.UseUnicode := True;
 try
  Connection.Connect;
 except
  on E:Exception do
   begin
    B := False;
    Log.WriteLog('{ADD_PIC - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Connection Error : ' + E.Message);

    Connection.Free;
   end;
 end;

 if B = False then
  begin
   ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

   Exit;
  end;

 try
  ASender.Context.Connection.IOHandler.WriteLn('1', IndyTextEncoding_UTF8);
 except
  on E:Exception do
   begin
    FileName := E.Message;
   end;
 end;

 MS := TMemoryStream.Create;
 try
  try
   ASender.Context.Connection.IOHandler.ReadStream(MS, FileSize, False);

    PicDir := ExtractFilePath(Application.ExeName) + 'Pictures';
 if Not DirectoryExists(PicDir) then
  CreateDir(PicDir);

 for I := 1 to 10000 do
  begin
   FileName := PicDir + '\Pic' + I.ToString + '.jpg';
   if not FileExists(FileName) then
    Break;
  end;

   MS.Position := 0;
   MS.SaveToFile(FileName);
  except
   on E:Exception do
    begin
     B := False;

     Connection.Close;
    end;
  end;
 finally
  MS.Free;
 end;

 if not B then
  begin
   ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

   Exit;
  end;

 SP := TMyStoredProc.Create(nil);
 SP.Connection := Connection;
 SP.StoredProcName :=  'add_pic';
 try
  try
   SP.PrepareSQL;

   SP.Params.ParamByName('fFileSize').AsInteger := FileSize;
   SP.Params.ParamByName('fFileName').AsString := FileName;

   SP.ExecProc;
  except
   on E:Exception do
    begin
     Log.WriteLog('{ADD_PIC - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Execute Error : ' + E.Message);

     SP.Free;
     Connection.Close;

     B := False;

     ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
    end;
  end;
 finally
  if B = True then
   begin
    case SP.ParamByName('fResult').AsInteger of
     1 : begin
          ASender.Context.Connection.IOHandler.WriteLn('1', IndyTextEncoding_UTF8);
         end;
     2 : begin
          ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

          B := False;
         end;
//     3 : begin
//          ASender.Context.Connection.IOHandler.WriteLn(SP.ParamByName('fMsg').AsString, IndyTextEncoding_UTF8);
//          Log.WriteLog(SP.ParamByName('fErrMsg').AsString);
//
//          B := False;
//         end;
    end;

    SP.Free;

    Connection.Close;
   end;
 end;
end;

procedure TMainFrm.TCPServerCommandHandlers1Command(ASender: TIdCommand);
var
 Connection : TMyConnection;
 SP : TMyStoredProc;
 IP, FileName : String;
 PicID : Integer;
 B : Boolean;
begin
 { DEL_PIC }

 B := True;

 ASender.Context.Connection.IOHandler.DefStringEncoding := IndyTextEncoding_UTF8;
 ASender.PerformReply := False;

 IP := ASender.Context.Connection.Socket.Binding.PeerIP;
 try
  PicID := StrToInt(ASender.Params.Strings[0]);
 except
  B := False;
  ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
 end;

 if B = False then
  Exit;

 Connection := TMyConnection.Create(nil);
 Connection.ConnectString := ConnectString;
 Connection.Options.UseUnicode := True;
 try
  Connection.Connect;
 except
  on E:Exception do
   begin
    B := False;
    Log.WriteLog('{DEL_PIC - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Connection Error : ' + E.Message);

    Connection.Free;
   end;
 end;

 if B = False then
  begin
   ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

   Exit;
  end;

 SP := TMyStoredProc.Create(nil);
 SP.Connection := Connection;
 SP.StoredProcName :=  'del_pic';
 try
  try
   SP.PrepareSQL;

   SP.Params.ParamByName('fPicID').AsInteger := PicID;

   SP.ExecProc;
  except
   on E:Exception do
    begin
     Log.WriteLog('{DEL_PIC - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Execute Error : ' + E.Message);

     SP.Free;
     Connection.Close;

     B := False;

     ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
    end;
  end;
 finally
  if B = True then
   begin
    case SP.ParamByName('fResult').AsInteger of
     1 : begin
          FileName := SP.ParamByName('fFileName').AsString;
          DeleteFile(FileName);

          ASender.Context.Connection.IOHandler.WriteLn('1', IndyTextEncoding_UTF8);
         end;
     2 : begin
          ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

          B := False;
         end;
//     3 : begin
//          ASender.Context.Connection.IOHandler.WriteLn(SP.ParamByName('fMsg').AsString, IndyTextEncoding_UTF8);
//          Log.WriteLog(SP.ParamByName('fErrMsg').AsString);
//
//          B := False;
//         end;
    end;

    SP.Free;

    Connection.Close;
   end;
 end;
end;

procedure TMainFrm.TCPServerCommandHandlers2Command(ASender: TIdCommand);
var
 Connection : TMyConnection;
 SP : TMyStoredProc;
 IP, FileName : String;
 PicID : Integer;
 B : Boolean;
begin
 { GET_LIST }

 ASender.Context.Connection.IOHandler.DefStringEncoding := IndyTextEncoding_UTF8;
 ASender.PerformReply := False;

 IP := ASender.Context.Connection.Socket.Binding.PeerIP;

 B := True;

 Connection := TMyConnection.Create(nil);
 Connection.ConnectString := ConnectString;
 Connection.Options.UseUnicode := True;
 try
  Connection.Connect;
 except
  on E:Exception do
   begin
    B := False;
    Log.WriteLog('{GET_LIST - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Connection Error : ' + E.Message);

    Connection.Free;
   end;
 end;

 if B = False then
  begin
   ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

   Exit;
  end;

 SP := TMyStoredProc.Create(nil);
 SP.Connection := Connection;
 SP.StoredProcName :=  'select_list';
 try
  try
   SP.PrepareSQL;

   SP.ExecProc;
  except
   on E:Exception do
    begin
     Log.WriteLog('{GET_LIST - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Execute Error : ' + E.Message);

     SP.Free;
     Connection.Close;

     B := False;

     ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
    end;
  end;
 finally
  if B = True then
   begin
    case SP.ParamByName('fResult').AsInteger of
     1 : begin
          ASender.Context.Connection.IOHandler.WriteLn('1', IndyTextEncoding_UTF8);

          if SP.ParamByName('fList').AsString <> '' then
           ASender.Context.Connection.IOHandler.WriteLn(SP.ParamByName('fList').AsString, IndyTextEncoding_UTF8)
          else
           ASender.Context.Connection.IOHandler.WriteLn('3', IndyTextEncoding_UTF8);
         end;
     2 : begin
          ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

          B := False;
         end;
//     3 : begin
//          ASender.Context.Connection.IOHandler.WriteLn(SP.ParamByName('fMsg').AsString, IndyTextEncoding_UTF8);
//          Log.WriteLog(SP.ParamByName('fErrMsg').AsString);
//
//          B := False;
//         end;
    end;

    SP.Free;

    Connection.Close;
   end;
 end;

end;

procedure TMainFrm.TCPServerCommandHandlers3Command(ASender: TIdCommand);
var
 Connection : TMyConnection;
 SP : TMyStoredProc;
 IP, FileName : String;
 PicID, FileSize : Integer;
 MS : TMemoryStream;
 B : Boolean;
begin
 { GET_FILE }

 ASender.Context.Connection.IOHandler.DefStringEncoding := IndyTextEncoding_UTF8;
 ASender.PerformReply := False;

 IP := ASender.Context.Connection.Socket.Binding.PeerIP;
 try
  PicID := StrToInt(ASender.Params.Strings[0]);
 except
  PicID := 0;
 end;

 B := True;

 Connection := TMyConnection.Create(nil);
 Connection.ConnectString := ConnectString;
 Connection.Options.UseUnicode := True;
 try
  Connection.Connect;
 except
  on E:Exception do
   begin
    B := False;
    Log.WriteLog('{GET_FILE - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Connection Error : ' + E.Message);

    Connection.Free;
   end;
 end;

 if B = False then
  begin
   ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

   Exit;
  end;

 SP := TMyStoredProc.Create(nil);
 SP.Connection := Connection;
 SP.StoredProcName := 'select_pic';
 try
  try
   SP.PrepareSQL;

   SP.ParamByName('fPicID').AsInteger := PicID;

   SP.ExecProc;
  except
   on E:Exception do
    begin
     Log.WriteLog('{GET_FILE - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Execute Error : ' + E.Message);

     SP.Free;
     Connection.Close;

     B := False;

     ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
    end;
  end;
 finally
  if B = True then
   begin
    case SP.ParamByName('fResult').AsInteger of
     1 : begin
          ASender.Context.Connection.IOHandler.WriteLn('1', IndyTextEncoding_UTF8);

          FileName := SP.ParamByName('fFileName').AsString;

          if (FileName <> '') and FileExists(FileName) then
           begin
            FileSize := GetFileSize(FileName);

            ASender.Context.Connection.IOHandler.WriteLn(IntToStr(FileSize), IndyTextEncoding_UTF8);

            MS := TMemoryStream.Create;
            try
             try
              MS.LoadFromFile(FileName);
              MS.Position := 0;

              ASender.Context.Connection.IOHandler.Write(MS);
             except
              on E:Exception do
               begin
                B := False;

                ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
               end;
             end;
            finally
             MS.Free;
            end;
           end
          else
           ASender.Context.Connection.IOHandler.WriteLn('3', IndyTextEncoding_UTF8);
         end;
     2 : begin
          ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

          B := False;
         end;
//     3 : begin
//          ASender.Context.Connection.IOHandler.WriteLn(SP.ParamByName('fMsg').AsString, IndyTextEncoding_UTF8);
//          Log.WriteLog(SP.ParamByName('fErrMsg').AsString);
//
//          B := False;
//         end;
    end;

    SP.Free;

    Connection.Close;
   end;
 end;
end;

procedure TMainFrm.TCPServerCommandHandlers4Command(ASender: TIdCommand);
var
 Connection : TMyConnection;
 SP : TMyStoredProc;
 IP, PicDir : String;
 B : Boolean;
begin
 { DEL_ALL }

 B := True;

 ASender.Context.Connection.IOHandler.DefStringEncoding := IndyTextEncoding_UTF8;
 ASender.PerformReply := False;

 IP := ASender.Context.Connection.Socket.Binding.PeerIP;

 if B = False then
  Exit;

 Connection := TMyConnection.Create(nil);
 Connection.ConnectString := ConnectString;
 Connection.Options.UseUnicode := True;
 try
  Connection.Connect;
 except
  on E:Exception do
   begin
    B := False;
    Log.WriteLog('{DEL_ALL - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Connection Error : ' + E.Message);

    Connection.Free;
   end;
 end;

 if B = False then
  begin
   ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

   Exit;
  end;

 SP := TMyStoredProc.Create(nil);
 SP.Connection := Connection;
 SP.StoredProcName :=  'del_all';
 try
  try
   SP.PrepareSQL;

   SP.ExecProc;
  except
   on E:Exception do
    begin
     Log.WriteLog('{DEL_ALL - "' + DateTimeToStr(Now) + '" - "' + IP + '"} - Execute Error : ' + E.Message);

     SP.Free;
     Connection.Close;

     B := False;

     ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);
    end;
  end;
 finally
  if B = True then
   begin
    case SP.ParamByName('fResult').AsInteger of
     1 : begin
          PicDir := ExtractFilePath(Application.ExeName) + '\Pictures';

          DelFilesFromDir(PicDir, '*.*', False);

          ASender.Context.Connection.IOHandler.WriteLn('1', IndyTextEncoding_UTF8);
         end;
     2 : begin
          ASender.Context.Connection.IOHandler.WriteLn('2', IndyTextEncoding_UTF8);

          B := False;
         end;
//     3 : begin
//          ASender.Context.Connection.IOHandler.WriteLn(SP.ParamByName('fMsg').AsString, IndyTextEncoding_UTF8);
//          Log.WriteLog(SP.ParamByName('fErrMsg').AsString);
//
//          B := False;
//         end;
    end;

    SP.Free;

    Connection.Close;
   end;
 end;
end;

procedure TMainFrm.TCPServerConnect(AContext: TIdContext);
begin
 AContext.Connection.IOHandler.DefStringEncoding := IndyTextEncoding_UTF8;
end;

procedure TMainFrm.UpdateLog(var Msg: TMessage);
begin
 LogMemo.Lines.LoadFromFile(LogFileName);
end;

end .
