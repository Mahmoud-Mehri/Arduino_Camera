unit LogUnit;

interface

uses
 WinApi.Windows, WinApi.Messages, System.SysUtils, System.Classes, Math, System.SyncObjs,
 IdBaseComponent, IdComponent, IdCustomTCPServer, IdTCPConnection, IdYarn, IdStack,
 IdContext, IdThreadSafe, IdGlobal, AsistantUnit;

type
 TLog = class(TThread)
  private
   fResultHandle : THandle;
   fFileName : String;
   fMsgList : TStringList;
   fCS : TRTLCriticalSection;
  public
   procedure WriteLog(Msg : String);
   procedure ClearLogFile;
   constructor Create(FileName : String);
   destructor Destroy; override;
  protected
   procedure Execute; override;
 end;

implementation

{ TLog }

procedure TLog.ClearLogFile;
begin
 EnterCriticalSection(fCS);
 try
  fMsgList.Add('CLEAR');
 finally
  LeaveCriticalSection(fCS);
 end;
end;

constructor TLog.Create(FileName: String);
begin
 inherited Create(True);

 FreeOnTerminate := True;
 fFileName := FileName;
 fMsgList := TStringList.Create;

 InitializeCriticalSection(fCS);
end;

destructor TLog.Destroy;
begin
 DeleteCriticalSection(fCS);
 fMsgList.Free;

 inherited Destroy;
end;

procedure TLog.Execute;
var
 F : TextFile;
 I : Integer;
 S : String;
begin
 while not Terminated do
  begin
   if fMsgList.Count > 0 then
    begin
     EnterCriticalSection(fCS);
     try
      AssignFile(F, fFileName);
      if FileExists(fFileName) then
       Append(F)
      else
       Rewrite(F);

      for I := 0 to fMsgList.Count - 1 do
       begin
        if Terminated then
         Break;

        S := fMsgList.Strings[I];
        if S = 'CLEAR' then
         Rewrite(F)
        else
         WriteLn(F, S);
       end;

      fMsgList.Clear;

      PostMessage(fResultHandle, MSG_LOG_UPDATE, 0, 0);
     finally
      CloseFile(F);
      LeaveCriticalSection(fCS);
     end;
    end
   else
    begin
     Sleep(200);
    end;
  end;
end;

procedure TLog.WriteLog(Msg: String);
begin
 EnterCriticalSection(fCS);
 try
  fMsgList.Add(Msg);
 finally
  LeaveCriticalSection(fCS);
 end;
end;

end.
