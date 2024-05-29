
;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  ;Name and file
  Name "Joget DX"
  ;OutFile "joget-installer.exe"

  ;Default installation folder
  ;InstallDir "$PROGRAMFILES\Joget"
  InstallDir "C:\Joget-DX8"

  ;Get installation folder from registry if available
  ;InstallDirRegKey HKCU "Software\Joget"

  ;Request application privileges for Windows Vista
  RequestExecutionLevel user

;--------------------------------
;Variables and Constants

  !define INSTALL_TYPE_FULL full
  !define INSTALL_TYPE_UPGRADE upgrade
  !define INSTALL_TYPE_MINOR_UPGRADE minor_upgrade
  !define INSTALL_TYPE_UPDATE update
  !define INSTALL_TYPE_ABORT abort
  Var INSTALL_TYPE
  Var EXISTING_TOMCAT_VERSION 


;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_BITMAP "joget_logo.bmp"
  !define MUI_HEADERIMAGE_RIGHT
  !define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
  !define MUI_ICON "joget.ico"

;--------------------------------
;Language Selection Dialog Settings

  ;Remember the installer language
  ;!define MUI_LANGDLL_REGISTRY_ROOT "HKCU"
  ;!define MUI_LANGDLL_REGISTRY_KEY "Software\Joget"
  ;!define MUI_LANGDLL_REGISTRY_VALUENAME "Joget Installer Language"
  !define MUI_LANGDLL_ALLLANGUAGES 
;--------------------------------

;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "LICENSE.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English" ;first language is the default language
  !insertmacro MUI_LANGUAGE "French"
  !insertmacro MUI_LANGUAGE "German"
  !insertmacro MUI_LANGUAGE "Spanish"
  !insertmacro MUI_LANGUAGE "SpanishInternational"
  !insertmacro MUI_LANGUAGE "SimpChinese"
  !insertmacro MUI_LANGUAGE "TradChinese"
  !insertmacro MUI_LANGUAGE "Japanese"
  !insertmacro MUI_LANGUAGE "Korean"
  !insertmacro MUI_LANGUAGE "Italian"
  !insertmacro MUI_LANGUAGE "Dutch"
  !insertmacro MUI_LANGUAGE "Danish"
  !insertmacro MUI_LANGUAGE "Swedish"
  !insertmacro MUI_LANGUAGE "Norwegian"
  !insertmacro MUI_LANGUAGE "NorwegianNynorsk"
  !insertmacro MUI_LANGUAGE "Finnish"
  !insertmacro MUI_LANGUAGE "Greek"
  !insertmacro MUI_LANGUAGE "Russian"
  !insertmacro MUI_LANGUAGE "Portuguese"
  !insertmacro MUI_LANGUAGE "PortugueseBR"
  !insertmacro MUI_LANGUAGE "Polish"
  !insertmacro MUI_LANGUAGE "Ukrainian"
  !insertmacro MUI_LANGUAGE "Czech"
  !insertmacro MUI_LANGUAGE "Slovak"
  !insertmacro MUI_LANGUAGE "Croatian"
  !insertmacro MUI_LANGUAGE "Bulgarian"
  !insertmacro MUI_LANGUAGE "Hungarian"
  !insertmacro MUI_LANGUAGE "Thai"
  !insertmacro MUI_LANGUAGE "Romanian"
  !insertmacro MUI_LANGUAGE "Latvian"
  !insertmacro MUI_LANGUAGE "Macedonian"
  !insertmacro MUI_LANGUAGE "Estonian"
  !insertmacro MUI_LANGUAGE "Turkish"
  !insertmacro MUI_LANGUAGE "Lithuanian"
  !insertmacro MUI_LANGUAGE "Slovenian"
  !insertmacro MUI_LANGUAGE "Serbian"
  !insertmacro MUI_LANGUAGE "SerbianLatin"
  !insertmacro MUI_LANGUAGE "Arabic"
  !insertmacro MUI_LANGUAGE "Farsi"
  !insertmacro MUI_LANGUAGE "Hebrew"
  !insertmacro MUI_LANGUAGE "Indonesian"
  !insertmacro MUI_LANGUAGE "Mongolian"
  !insertmacro MUI_LANGUAGE "Luxembourgish"
  !insertmacro MUI_LANGUAGE "Albanian"
  !insertmacro MUI_LANGUAGE "Breton"
  !insertmacro MUI_LANGUAGE "Belarusian"
  !insertmacro MUI_LANGUAGE "Icelandic"
  !insertmacro MUI_LANGUAGE "Malay"
  !insertmacro MUI_LANGUAGE "Bosnian"
  !insertmacro MUI_LANGUAGE "Kurdish"
  !insertmacro MUI_LANGUAGE "Irish"
  !insertmacro MUI_LANGUAGE "Uzbek"
  !insertmacro MUI_LANGUAGE "Galician"
  !insertmacro MUI_LANGUAGE "Afrikaans"
  !insertmacro MUI_LANGUAGE "Catalan"
  !insertmacro MUI_LANGUAGE "Esperanto"

;--------------------------------
;Reserve Files

  ;If you are using solid compression, files that are required before
  ;the actual installation should be stored first in the data block,
  ;because this will make your installer start faster.

  !insertmacro MUI_RESERVEFILE_LANGDLL

;--------------------------------

;Installer Sections

Section "Joget DX" SecJoget

  SectionIn RO
  SetOutPath "$INSTDIR"

  Call CheckUpgrade

  ${If} $INSTALL_TYPE == "${INSTALL_TYPE_ABORT}"
    MessageBox MB_OK "Existing v2 installation found in the directory '$INSTDIR'.$\r$\nSorry, upgrade from this version is not supported."
    Quit
  ${EndIf}

  ${If} $INSTALL_TYPE != "${INSTALL_TYPE_FULL}"
    ;MessageBox MB_OK $INSTALL_TYPE
    MessageBox MB_YESNO "Existing installation found in the directory '$INSTDIR'.$\r$\nWould you like to update? $\r$\n" IDYES DoUpgrade
    MessageBox MB_OK "Installation aborted"
    Quit

    DoUpgrade:
        ;MessageBox MB_OK "Upgrading"
        RmDir /r "$SMPROGRAMS\Joget Workflow v3"
        RmDir /r "$SMPROGRAMS\Joget Workflow v4"
        RmDir /r "$SMPROGRAMS\Joget Workflow v5"
        RmDir /r "$SMPROGRAMS\Joget Workflow v6"
        RmDir /r "$INSTDIR\apache-tomcat-$EXISTING_TOMCAT_VERSION\webapps\jw"
        RmDir /r "$INSTDIR\apache-tomcat-$EXISTING_TOMCAT_VERSION\webapps\jwdesigner"
        CreateDirectory "$INSTDIR\apache-tomcat-$EXISTING_TOMCAT_VERSION\webapps"
        File /oname=apache-tomcat-$EXISTING_TOMCAT_VERSION\webapps\jw.war apache-tomcat-9.0.86\webapps\jw.war
        CreateDirectory "$INSTDIR\data"
        File /oname=data\jwdb-empty.sql data\jwdb-empty.sql
        File /oname=data\jwdb-sample.sql data\jwdb-sample.sql
        File build.xml
        File LICENSE.txt
        File NOTICE.txt
        File VERSION.txt
        File CHANGES.txt
    Return

  ${EndIf}

  ;Joget Files Here
  File /r apache-ant-1.7.1
  CreateDirectory "$INSTDIR\apache-tomcat-9.0.86\webapps"
  File /oname=apache-tomcat-9.0.86\webapps\jw.war apache-tomcat-9.0.86\webapps\jw.war
  CreateDirectory "$INSTDIR\data"
  File /oname=data\jwdb-empty.sql data\jwdb-empty.sql
  File /oname=data\jwdb-sample.sql data\jwdb-sample.sql
  ;File docs
  File /r wflow*.*
  File build.xml
  File LICENSE.txt
  File NOTICE.txt
  File VERSION.txt
  File CHANGES.txt
  ;File README.txt
  File joget.ico
  File joget_start.ico
  File joget_stop.ico

  ;Store installation folder
  ;WriteRegStr HKCU "Software\Joget" "" $INSTDIR

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Apache Tomcat 9" SecTomcat

  SectionIn RO
  SetOutPath "$INSTDIR"

${If} $INSTALL_TYPE == "${INSTALL_TYPE_FULL}"
  ;Tomcat File Here
  File /r /x *.war apache-tomcat-9.0.86
  File tomcat-run.bat
  File tomcat-stop.bat
  File joget-start.bat
  File joget-stop.bat

  CreateShortCut "$INSTDIR\Start Joget Server.lnk" "$INSTDIR\joget-start.bat" "Start Joget Server" "$INSTDIR\joget_start.ico"
  CreateShortCut "$INSTDIR\Stop Joget Server.lnk" "$INSTDIR\joget-stop.bat" "Stop Joget Server" "$INSTDIR\joget_stop.ico"
${EndIf}

SectionEnd

Section "Java 11" SecJava

  SectionIn RO
  SetOutPath "$INSTDIR"

  ${If} $INSTALL_TYPE == "${INSTALL_TYPE_FULL}"  
    ;Java Files Here
    File /r jre11.0.18
  ${EndIf}  

SectionEnd

Section "MariaDB 10" SecMariaDB

  SectionIn RO
  SetOutPath "$INSTDIR"

  ${If} $INSTALL_TYPE == "${INSTALL_TYPE_FULL}"
    ;MariaDB Files Here
    File /r mariadb-10.6.12-winx64
    File mariadb-start.bat
    File mariadb-stop.bat
  ${EndIf}

SectionEnd

Section "Start Menu Shortcuts" SecStartMenu

  SetOutPath "$INSTDIR"

  CreateDirectory "$SMPROGRAMS\Joget DX 8"
  CreateShortCut "$SMPROGRAMS\Joget DX 8\Start Joget Server.lnk" "$INSTDIR\joget-start.bat" "Start Joget Server" "$INSTDIR\joget_start.ico"
  CreateShortCut "$SMPROGRAMS\Joget DX 8\Stop Joget Server.lnk" "$INSTDIR\joget-stop.bat" "Stop Joget Server" "$INSTDIR\joget_stop.ico"
  CreateShortCut "$SMPROGRAMS\Joget DX 8\App Center.lnk" "http://localhost:8080/jw" "App Center" "$INSTDIR\joget.ico"
  CreateShortCut "$SMPROGRAMS\Joget DX 8\www.joget.org.lnk" "http://www.joget.org" "www.joget.org" "$INSTDIR\joget.ico"

SectionEnd

;--------------------------------
;Installer Functions

Function .onInit

  
  !insertmacro MUI_LANGDLL_DISPLAY

FunctionEnd


;--------------------------------
;Functions

Function CheckUpgrade

  ${If} ${FileExists} $INSTDIR\apache-tomcat-6.0.18\webapps\wflow-designerweb.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_ABORT}
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.86\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.86"
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.85\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.85"
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.82\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.82"
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.76\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.76"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.74\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.74"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.71\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.71"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-9.0.62\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "9.0.62"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.72\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.72"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.65\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.65"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.58\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.58"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.41\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.41"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.38\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.38"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.23\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.23"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.16\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.16"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.14\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.14"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.5.9\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPDATE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.5.9"  
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-8.0.20\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPGRADE}
    StrCpy $EXISTING_TOMCAT_VERSION "8.0.20"
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-7.0.52\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPGRADE}
    StrCpy $EXISTING_TOMCAT_VERSION "7.0.52"
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-7.0.39\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPGRADE}
    StrCpy $EXISTING_TOMCAT_VERSION "7.0.39"
  ${ElseIf} ${FileExists} $INSTDIR\apache-tomcat-6.0.18\webapps\jw.war
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_UPGRADE}
    StrCpy $EXISTING_TOMCAT_VERSION "6.0.18"
  ${Else}
    StrCpy $INSTALL_TYPE ${INSTALL_TYPE_FULL}
  ${EndIf}
  ;MessageBox MB_OK $INSTALL_TYPE

FunctionEnd


;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecJoget ${LANG_ENGLISH} "Core Joget DX Application"
  LangString DESC_SecTomcat ${LANG_ENGLISH} "Apache Tomcat Web Application Server"
  LangString DESC_SecJava ${LANG_ENGLISH} "Java 11 Standard Edition"
  LangString DESC_SecMariaDB ${LANG_ENGLISH} "MariaDB 10 Database Server"
  LangString DESC_SecStartMenu ${LANG_ENGLISH} "Start Menu Shortcuts"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecJoget} $(DESC_SecJoget)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcat} $(DESC_SecTomcat)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecJava} $(DESC_SecJava)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMariaDB} $(DESC_SecMariaDB)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecStartMenu} $(DESC_SecStartMenu)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"


  ;Uninstall Files Here
  RMDir /r "$SMPROGRAMS\Joget DX 8"

  RmDir /r "$INSTDIR\apache-ant-1.7.1"
  RmDir /r "$INSTDIR\jre11.0.18"
  RmDir /r "$INSTDIR\apache-tomcat-9.0.86\webapps\jw"
  Delete "$INSTDIR\apache-tomcat-9.0.86\webapps\jw.war"
  Delete "$INSTDIR\build.xml"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\NOTICE.txt"
  Delete "$INSTDIR\VERSION.txt"
  Delete "$INSTDIR\README.txt"
  Delete "$INSTDIR\CHANGES.txt"
  Delete "$INSTDIR\joget.ico"
  Delete "$INSTDIR\joget_start.ico"
  Delete "$INSTDIR\joget_stop.ico"
  Delete "$INSTDIR\Start Joget Server.lnk"
  Delete "$INSTDIR\Stop Joget Server.lnk"
  Delete "$INSTDIR\tomcat-run.bat"
  Delete "$INSTDIR\tomcat-stop.bat"
  Delete "$INSTDIR\mariadb-start.bat"
  Delete "$INSTDIR\mariadb-stop.bat"
  Delete "$INSTDIR\joget-start.bat"
  Delete "$INSTDIR\joget-stop.bat"

  Delete "$INSTDIR\Uninstall.exe"

  ;RMDir "$INSTDIR"

  ;DeleteRegKey /ifempty HKCU "Software\Joget"

  MessageBox MB_OK "Uninstallation complete. Data files in $INSTDIR have not been deleted. $\r$\nPlease delete the folder manually if you do not wish to keep the data."

SectionEnd

;--------------------------------
;Uninstaller Functions

Function un.onInit

  !insertmacro MUI_UNGETLANGUAGE

FunctionEnd
