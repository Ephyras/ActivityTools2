#include "portal.h"
#include <qdebug.h>
#include <qmessagebox.h>
#include <qprocess.h>

bool matchProcessName( DWORD processID, std::wstring processName);
bool isProcessExists(TCHAR* processName);

Portal::Portal(QWidget *parent)
	: QMainWindow(parent)
{
	ui.setupUi(this);

	connect(ui.startBtn, SIGNAL(clicked()), this, SLOT(startCollecter()));
}

Portal::~Portal()
{

}

void Portal::startCollecter()
{
	qDebug()<<"start to data collecter"<<endl;

	bool isRunning = isProcessExists(_T("ActivityCollecterConsole.exe"));
	qDebug()<<"process exists: "<<isRunning<<endl;

	if(isRunning)
	{
		QMessageBox::warning(this, "Warning", "The Data Collecter Program is still running");
		return;
	}

	/*
	STARTUPINFO si;
    PROCESS_INFORMATION pi;
	ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    ZeroMemory( &pi, sizeof(pi) );
	BOOL success = CreateProcess(_T("ActivityCollecterConsole.exe"), NULL, NULL,NULL,FALSE,0,NULL, NULL,&si,&pi);
	
	*/
	QProcess process;
	//process.start("D:\\baolingfeng\\GitHub\\ActivityTools\\ActivityCollecter\\Debug\\ActivityCollecterConsole.exe", QStringList() <<" < 1.txt");
	process.start("./Debug\\ActivityCollecterConsole.exe");
	bool success = process.waitForStarted();

	if(!success)
	{
		QMessageBox::warning(this, "Warning", "Can not start the process, please check");
		return;
	}

	//int res = system("reg add \"HKEY_CURRENT_USER\\Control Panel\\Desktop\" /v LowLevelHooksTimeout /t reg_dword /d 100000 /f");

	//res = (int)ShellExecute(NULL,NULL, _T("D:\\baolingfeng\\GitHub\\ActivityTools\\ActivityCollecter\\Debug\\ActivityCollecterConsole.exe"), NULL, _T("-l"),SW_SHOW);
	
}

bool matchProcessName( DWORD processID, std::wstring processName)
{
    TCHAR szProcessName[MAX_PATH] = TEXT("<unknown>");

    // Get a handle to the process.

    HANDLE hProcess = OpenProcess( PROCESS_QUERY_INFORMATION |
                                   PROCESS_VM_READ,
                                   FALSE, processID );

    // Get the process name.

    if (NULL != hProcess )
    {
        HMODULE hMod;
        DWORD cbNeeded;

        if ( EnumProcessModules( hProcess, &hMod, sizeof(hMod), 
             &cbNeeded) )
        {
            GetModuleBaseName( hProcess, hMod, szProcessName, 
                               sizeof(szProcessName)/sizeof(TCHAR) );
        }
    }

    // Compare process name with your string        
    bool matchFound = !_tcscmp(szProcessName, processName.c_str() );

    // Release the handle to the process.    
    CloseHandle( hProcess );

    return matchFound;
}

bool isProcessExists(TCHAR* processName)
{
    // strip path
    DWORD aProcesses[1024], cbNeeded, cProcesses;
    unsigned int i;

    if ( !EnumProcesses( aProcesses, sizeof(aProcesses), &cbNeeded ) )
    {
        return 1;
    }

    // Calculate how many process identifiers were returned.
    cProcesses = cbNeeded / sizeof(DWORD);

    // Print the name and process identifier for each process.
    for ( i = 0; i < cProcesses; i++ )
    {
        if( aProcesses[i] != 0 )
        {
            if(matchProcessName(aProcesses[i], processName))
			{
				return true;
			}
        }
    }
	return false;
}