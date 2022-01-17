program Server;

uses
  Vcl.Forms,
  MainUnit in 'MainUnit.pas' {MainFrm},
  AsistantUnit in 'AsistantUnit.pas',
  LogUnit in 'LogUnit.pas';

{$R *.res}

begin
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.CreateForm(TMainFrm, MainFrm);
  Application.Run;
end.
