object MainFrm: TMainFrm
  Left = 0
  Top = 0
  BorderStyle = bsDialog
  ClientHeight = 211
  ClientWidth = 457
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  OnCreate = FormCreate
  PixelsPerInch = 96
  TextHeight = 13
  object Label1: TLabel
    Left = 8
    Top = 8
    Width = 99
    Height = 13
    Caption = 'Current IP Address :'
  end
  object IPLabel: TLabel
    Left = 113
    Top = 8
    Width = 336
    Height = 13
    AutoSize = False
  end
  object LogMemo: TMemo
    Left = 8
    Top = 27
    Width = 441
    Height = 176
    TabOrder = 0
  end
  object TCPServer: TIdCmdTCPServer
    Bindings = <
      item
        IP = '0.0.0.0'
        Port = 1717
      end>
    DefaultPort = 1717
    OnConnect = TCPServerConnect
    CommandHandlers = <
      item
        CmdDelimiter = '#'
        Command = 'ADD_PIC'
        Disconnect = False
        Name = 'AddPicCmd'
        NormalReply.Code = '200'
        ParamDelimiter = '#'
        ParseParams = True
        Tag = 0
        OnCommand = TCPServerCommandHandlers0Command
      end
      item
        CmdDelimiter = '#'
        Command = 'DEL_PIC'
        Disconnect = False
        Name = 'DelPicCmd'
        NormalReply.Code = '200'
        ParamDelimiter = '#'
        ParseParams = True
        Tag = 0
        OnCommand = TCPServerCommandHandlers1Command
      end
      item
        CmdDelimiter = '#'
        Command = 'GET_LIST'
        Disconnect = False
        Name = 'GetListCmd'
        NormalReply.Code = '200'
        ParamDelimiter = '#'
        ParseParams = True
        Tag = 0
        OnCommand = TCPServerCommandHandlers2Command
      end
      item
        CmdDelimiter = '#'
        Command = 'GET_FILE'
        Disconnect = False
        Name = 'GetFileCmd'
        NormalReply.Code = '200'
        ParamDelimiter = '#'
        ParseParams = True
        Tag = 0
        OnCommand = TCPServerCommandHandlers3Command
      end
      item
        CmdDelimiter = '#'
        Command = 'DEL_ALL'
        Disconnect = False
        Name = 'DelAllCmd'
        NormalReply.Code = '200'
        ParamDelimiter = '#'
        ParseParams = True
        Tag = 0
        OnCommand = TCPServerCommandHandlers4Command
      end>
    ExceptionReply.Code = '500'
    ExceptionReply.Text.Strings = (
      'Unknown Internal Error')
    Greeting.Code = '200'
    Greeting.Text.Strings = (
      'Welcome')
    HelpReply.Code = '100'
    HelpReply.Text.Strings = (
      'Help follows')
    MaxConnectionReply.Code = '300'
    MaxConnectionReply.Text.Strings = (
      'Too many connections. Try again later.')
    ReplyTexts = <>
    ReplyUnknownCommand.Code = '400'
    ReplyUnknownCommand.Text.Strings = (
      'Unknown Command')
    Left = 360
    Top = 80
  end
end
