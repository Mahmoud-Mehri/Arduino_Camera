unit AsistantUnit;

interface

uses
 WinApi.Windows, WinApi.Messages, System.SysUtils, System.Classes, Math, ShellApi, Forms;

 function GetFileSize(const FName : String): Int64;
 procedure DelFilesFromDir(Directory, FileMask: string; DelSubDirs: Boolean);

const
 MSG_LOG_UPDATE = WM_USER + 1;
 ConnectString = 'User ID=root;Data Source=localhost;Database=arduino;Port=3306;Login Prompt=False';

var
 LogFileName : String;

implementation

uses MainUnit;

procedure DelFilesFromDir(Directory, FileMask: string; DelSubDirs: Boolean);
var
  SourceLst: string;
  FOS: TSHFileOpStruct;
begin
  FillChar(FOS, SizeOf(FOS), 0);
  FOS.Wnd := Application.MainForm.Handle;
  FOS.wFunc := FO_DELETE;
  SourceLst := Directory + '\' + FileMask + #0;
  FOS.pFrom := PChar(SourceLst);
  if not DelSubDirs then
    FOS.fFlags := FOS.fFlags OR FOF_FILESONLY;
  // Remove the next line if you want a confirmation dialog box
  FOS.fFlags := FOS.fFlags OR FOF_NOCONFIRMATION;
  // Uncomment the next line for a "silent operation" (no progress box)
  // FOS.fFlags := FOS.fFlags OR FOF_SILENT;
  SHFileOperation(FOS);
end;

function HeapAllocatedPChar(const Value: string): PChar;
var
 BufferSize : Integer;
begin
 BufferSize := (Length(Value)+1)*SizeOf(Char);
 GetMem(Result, bufferSize);
 Move(PChar(Value)^, Result^, bufferSize);
end;

procedure PostString(Window: HWND; Msg: UINT; wParam: WPARAM; const Value: string);
var
 P : PChar;
begin
 P := HeapAllocatedPChar(Value);
 if not PostMessage(Window, Msg, wParam, LPARAM(P)) then
  FreeMem(P);
end;

function GetFileSize(const FName : String): Int64;
var
  info: TWin32FileAttributeData;
begin
  result := -1;

  if GetFileAttributesEx(PWideChar(FName), GetFileExInfoStandard, @info) then
   result := Int64(info.nFileSizeLow) or Int64(info.nFileSizeHigh shl 32);
end;

function IsNumber(N : String) : Boolean;
var
 I : Integer;
begin
 Result := True;
 if Trim(N) = '' then
  Exit(False);

 if (Length(Trim(N)) > 1) and (Trim(N)[1] = '0') then
  Exit(False);

 for I := 1 to Length(N) do
  begin
   if not (N[I] in ['0'..'9']) then
    begin
     Result := False;
     Break;
    end;
  end;
end;

function CheckPhoneNumber(N : String) : Boolean;
begin
 Result := True;

 if (Length(N) < 10) or (not (N[1] in ['+', '0', '9'])) then
  begin
   Result := False;
   Exit;
  end;

 if N[1] = '+' then
  N := Copy(N, 2, Length(N) - 1)
 else
  begin
   if N[1] = '0' then
    N := Copy(N, 2, Length(N) - 1);
  end;

 if not IsNumber(N) then
  Result := False;
end;

end.
