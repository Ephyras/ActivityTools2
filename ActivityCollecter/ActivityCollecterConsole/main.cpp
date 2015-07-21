#include<iostream>
#include<fstream>
#include<string>
#include"util.h"
#include<Windows.h>
#include"../HCICollectDll/HCICollectDll.h"
//#include"upload.h"
using namespace std;

#pragma comment (lib,"Psapi.lib")
#pragma comment(lib, "gdiplus.lib")
#pragma comment(lib, "sqlite3.lib")
//#pragma comment(lib, "mysqlcppconn.lib")
//#pragma comment(lib, "mysqlcppconn-static.lib")

StructConfiguration readConfig()
{
	StructConfiguration config;

	ifstream is("config.txt");

	string line;

	for (std::string line; std::getline(is, line); ) 
	{
		trim(line);
		if(line == "" || line.find("#") == 0)
		{
			continue;
		}

		vector<string> param = split(line, "=");
		if(param.size() <2) continue;

		if(param[0] == "ALLMOUSE") //monitor mouse event including click action using accessibility api
		{
			config.mouseMode = param[1] == "TRUE" ? true : false;
		}
		else if(param[0] == "KEYBOARD") //monitor keyboard event
		{
			config.keyMode =  str2Num<int>(param[1]); //param[1] == "TRUE" ? true : false;
		}
		else if(param[0] == "COPY") //monitor copy event
		{
			config.copyMode =  param[1] == "TRUE" ? true : false;
		}
		else if(param[0] == "SCREENCAPTURE") //screen capture mode;
		{
			config.screenCaptureMode = str2Num<int>(param[1]);
		}
		else if(param[0] == "FILTER") //filter type, white list or all
		{
			if(param[1] == "WHITE")
			{
				config.filter = FilterType::WhiteList;
			}
			else
			{
				config.filter = FilterType::All;
			}
		}
		else if(param[0] == "PROCESS") // process list
		{
			config.processes = split(param[1], ",");
		}
		else if(param[0] == "UPLOAD") // whether upload to a server, uploader is another program, see project ActivityUploader
		{
			config.isUpload =  param[1] == "TRUE" ? true : false;
		}
		else if(param[0] == "EXTRACTURL")
		{
			config.isNeedUrl =  param[1] == "TRUE" ? true : false;
		}
		else if(param[0] == "KEYS") // if keymode == 2, only monitor keys in config
		{
			config.keys = split(param[1], ",");
		}
		else if(param[0] == "ACTIONKEYS") // if keymode not 0,  monitor key action using accessiblity api
		{
			config.actionKeys = split(param[1], ",");
		}
		else if(param[0] == "ANALYSIS")
		{
			config.analysis_job = param[1];
		}
    }

	return config;
}

bool init(string logDir)
{
	WIN32_FIND_DATA wfd;
	wstring temp = str2wstr(logDir);
	HANDLE hFind = FindFirstFile(str2wstr(logDir).c_str(), &wfd);
	if(hFind == INVALID_HANDLE_VALUE || !(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
	{
		CreateDirectory(str2wstr(logDir).c_str(),NULL);
		CreateDirectory(str2wstr(logDir+"/screen").c_str(),NULL);
	}
	else
	{
		hFind = FindFirstFile(str2wstr(logDir+"/screen").c_str(), &wfd);
		if(hFind == INVALID_HANDLE_VALUE || !(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
		{
			CreateDirectory(str2wstr(logDir+"/screen").c_str(),NULL);
		}
	}

	string dbFile = logDir + "/log.db3";

	//WIN32_FIND_DATA wfd;
	hFind = FindFirstFile(str2wstr(dbFile).c_str(), &wfd);
	sqlite3* db;
	char *zErrMsg = 0;

	if(hFind == INVALID_HANDLE_VALUE)
	{
		int rc = sqlite3_open_v2(dbFile.c_str(), &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL);
	}
	else
	{
		return TRUE;
	}

	std::stringstream ss;
	ss<<"create table tbl_record ("
		<<"start_time varchar(25), "
		<<"end_time varchar(25) )";
	int rc = sqlite3_exec(db, ss.str().c_str(), NULL, 0, &zErrMsg);
	if(rc > 0) printf(zErrMsg);

	//sqlite3_exec(db, strsql.c_str(), NULL, 0, &zErrMsg);

	std::stringstream ss1;
	ss1<<"create table tbl_mouse_event ("
		<<"timestamp varchar(25)," 
		<<"event_name varchar(20), " 
		<<"p_x int," 
		<<"p_y int," 
		<<"window_name varchar(300)," 
		<<"win_rect_left int," 
		<<"win_rect_top int," 
		<<"win_rect_right int,"
		<<"win_rect_bottom int,"
		<<"process_name varchar(30),"
		<<"parent_window varchar(300) )";

	
	int rc1 = sqlite3_exec(db, ss1.str().c_str(), NULL, 0, &zErrMsg);
	if(rc1 > 0) printf(zErrMsg);

	std::stringstream ss2;
	ss2<<"create table tbl_key_event ("
		<<"timestamp varchar(25),"
		<<"event_name varchar(20),"
		<<"p_x int,"
		<<"p_y int,"
		<<"window_name varchar(300),"
		<<"win_rect_left int,"
		<<"win_rect_top int,"
		<<"win_rect_right int,"
		<<"win_rect_bottom int,"
		<<"process_name varchar(30),"
		<<"parent_window varchar(300))";
		
	int rc2 = sqlite3_exec(db, ss2.str().c_str(), NULL, 0, &zErrMsg);
	if(rc2 > 0) printf(zErrMsg);

	std::stringstream ss3;
	ss3<<"create table tbl_click_action ("
		<<"timestamp varchar(25),"
		<<"action_name text,"
		<<"action_type varchar(50),"
		<<"action_value text,"
		<<"bound_left int,"
		<<"bound_top int,"
		<<"bound_right int,"
		<<"bound_bottom int,"
		<<"action_parent_name text,"
		<<"action_parent_type varchar(50) )";
		
	int rc3 = sqlite3_exec(db, ss3.str().c_str(), NULL, 0, &zErrMsg);
	if(rc3 > 0) printf(zErrMsg);

	std::stringstream ss4;
	ss4<<"create table tbl_copy_event ("
		<<"timestamp varchar(25),"
		<<"window_name varchar(300),"
		<<"process_name varchar(30),"
		<<"parent_window varchar(300),"
		<<"copy_text text )";
		
	int rc4 = sqlite3_exec(db, ss4.str().c_str(), NULL, 0, &zErrMsg);
	if(rc4 > 0) printf(zErrMsg);

	std::stringstream ss5;
	ss5<<"create table tbl_webpage_url ("
		//<<"window_name varchar(300),"
		<<"url varchar(300),"
		<<"window_name varchar(300),"
		<<"parent_window varchar(300),"
		<<"timestamp varchar(25) )";

	int rc5 = sqlite3_exec(db, ss5.str().c_str(), NULL, 0, &zErrMsg);
	if(rc5 > 0) printf(zErrMsg);

	std::stringstream ss6;
	ss6<<"create table tbl_key_action ("
		<<"timestamp varchar(25),"
		<<"action_name text,"
		<<"action_type varchar(50),"
		<<"action_value text,"
		<<"bound_left int,"
		<<"bound_top int,"
		<<"bound_right int,"
		<<"bound_bottom int,"
		<<"action_parent_name text,"
		<<"action_parent_type varchar(50) )";

	int rc6 = sqlite3_exec(db, ss6.str().c_str(), NULL, 0, &zErrMsg);
	if(rc6 > 0) printf(zErrMsg);

	sqlite3_close_v2(db);
	return rc1 == 0 && rc2 == 0 && rc3 == 0 && rc4 == 0;
}

BOOL WINAPI beforeClose(DWORD CEvent)
{
	switch(CEvent)
	{
	case CTRL_CLOSE_EVENT:
		close_data_collect();
		break;
	}
	return true;
}

int main()
{
	/************************************regedit operation: LowLevelHooksTimeout***********************************************************/
	int res = system("reg add \"HKEY_CURRENT_USER\\Control Panel\\Desktop\" /v LowLevelHooksTimeout /t reg_dword /d 100000 /f");
	if(res != 0) {
		cout<<"Reg Hook Time Fail !"<<endl;
	}

	string logDir = "log";
	StructConfiguration config = readConfig();
	config.logDir = logDir;

	init(logDir);

	initDll(config);

	SetConsoleCtrlHandler(beforeClose, TRUE);

	if(config.isUpload)
	{
		STARTUPINFO si;
		PROCESS_INFORMATION pi;
		ZeroMemory( &si, sizeof(si) );
		si.cb = sizeof(si);
		ZeroMemory( &pi, sizeof(pi) );
		CreateProcess(_T("ActivityUploader.exe"), NULL, NULL,NULL,FALSE,0,NULL, NULL,&si,&pi);
	}

	if(config.analysis_job != "")
	{
		STARTUPINFO si;
		PROCESS_INFORMATION pi;
		ZeroMemory( &si, sizeof(si) );
		si.cb = sizeof(si);
		ZeroMemory( &pi, sizeof(pi) );
		res = CreateProcess(str2wstr(config.analysis_job).c_str(), NULL, NULL,NULL,FALSE,0,NULL, NULL,&si,&pi);
		cout<<"create analysis process "<<config.analysis_job<<", result: "<<res<<endl;
	}
	//uploader.run();

	MSG msg; //message recieved
	while(GetMessage(&msg, NULL, 0, 0) > 0) {
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	return 0;
}

