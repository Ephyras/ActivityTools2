// HCICollectDll.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include "util.h"
#include "HCICollectDll.h"
#include <vector>
#include <hash_map>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <sqlite3.h>

#pragma comment (lib,"Ole32.lib")
#pragma comment (lib,"Psapi.lib")
#pragma comment (lib,"OleAcc.lib")

#pragma comment(lib, "gdiplus.lib")
#pragma comment(lib, "sqlite3.lib")

/*--------------Gobal Variable-----------------------*/
HHOOK g_hLLKeyBoardHook = NULL;
HHOOK g_hLLMouseHook = NULL;
FILE* g_fMouseFile = NULL;
FILE* g_fAccTimeOut = NULL;

IUIAutomation *g_pAutomation=NULL;
IUIAutomationTreeWalker* g_pControlWalker = NULL;

const int MAX_LEN = 5 * 1024 * 1024;
CRITICAL_SECTION  cs_mouse;
CRITICAL_SECTION  cs_click;
CRITICAL_SECTION  cs_key;
CRITICAL_SECTION  cs_copy;
volatile int mLen = 0;
volatile int kLen = 0;
volatile int cLen = 0;
volatile int copyLen = 0;
volatile int used = 0;
char shared_mouse_memory1[MAX_LEN];
char shared_mouse_memory2[MAX_LEN];
char shared_key_memory1[MAX_LEN];
char shared_key_memory2[MAX_LEN];
char shared_click_memory1[MAX_LEN];
char shared_click_memory2[MAX_LEN];
char shared_copy_memory1[MAX_LEN];
char shared_copy_memory2[MAX_LEN];

sqlite3* db;
ofstream errlog;
HANDLE hWriteDisk;
//DataMode dm;

hash_map<string, string> window_map; //the window

HWND copyHwnd;
HWND copyViewer;

DWORD preProcessId = -1;
HWND preWindow = NULL;
HWND preLeftDownWindow = NULL;
HWND preWheelWindow = NULL;

SYSTEMTIME preKeyTime;
SYSTEMTIME preMouseMoveTime;
SYSTEMTIME preLClickTime;
SYSTEMTIME preMouseWheelTime;

RECT SCREEN_RECT;

CConfig config;

BOOL is_no_logged = FALSE;
BOOL isFirst = TRUE;
/*--------------Gobal Variable-----------------------*/

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
	return TRUE;
}

HMODULE WINAPI ModuleFromAddress(PVOID pv) 
{
	MEMORY_BASIC_INFORMATION mbi;
	if (::VirtualQuery(pv, &mbi, sizeof(mbi)) != 0)
	{
		return (HMODULE)mbi.AllocationBase;
	}
	else
	{
		return NULL;
	}
}

BOOL WINAPI initDll(CConfig& config_in)
{
	config = config_in;

	errlog = ofstream(config.logDir + "/error.txt", ofstream::app);
	
	if(isFirst)
	{
		InitializeCriticalSection(&cs_mouse);
		InitializeCriticalSection(&cs_click);

		CoInitializeEx(NULL,COINIT_MULTITHREADED);
		HRESULT hr = CoCreateInstance(__uuidof(CUIAutomation), NULL,
			CLSCTX_INPROC_SERVER, __uuidof(IUIAutomation), (void**)&g_pAutomation);

		g_pAutomation->get_ContentViewWalker(&g_pControlWalker);

		SCREEN_RECT.left = 0;
		SCREEN_RECT.top = 0;
		SCREEN_RECT.right = GetSystemMetrics(SM_CXSCREEN);
		SCREEN_RECT.bottom = GetSystemMetrics(SM_CYSCREEN);

		CoUninitialize();

		isFirst = FALSE;
	}
	
	memset(shared_mouse_memory1, 0, MAX_LEN);
	memset(shared_mouse_memory2, 0, MAX_LEN);
	memset(shared_click_memory1, 0, MAX_LEN);
	memset(shared_click_memory2, 0, MAX_LEN);

	cout<<"setup global hook..."<<endl;
	if(!SetLowMouseHook())
	{
		errlog<<"setup mouse hook failed"<<endl;
	}
	
	if(config.keyMode == 1)
	{
		if(!SetLowKeyboardHook()) {errlog<<"setup keyboard hook failed"<<endl;}
		
		InitializeCriticalSection(&cs_key);
		memset(shared_key_memory1, 0, MAX_LEN);
		memset(shared_key_memory2, 0, MAX_LEN);
	}
		
	if(config.copyMode == 1)
	{
		SetCopyMonitor();

		InitializeCriticalSection(&cs_copy);
		memset(shared_copy_memory1, 0, MAX_LEN);
		memset(shared_copy_memory2, 0, MAX_LEN);
	}

	int rc = sqlite3_open_v2((config.logDir + "/log.db3").c_str(), &db, SQLITE_OPEN_READWRITE, NULL);
	if(rc > 0)
	{
		errlog<<"open database error...";
	}

	string strsql = "insert into tbl_record(start_time) values('" + GetSysLocalTimeStr() + "')";
	sqlite3_exec(db, strsql.c_str(), NULL, 0, NULL);

	hWriteDisk = CreateThread(NULL, 0, WriteToDiskThreadFunction, NULL, 0, NULL);
	
	return TRUE;
}

BOOL WINAPI SetFitlerForHook(FilterType &pFilterType_in, std::vector<std::string> &processNameList_in)
{
	//g_pFilterType = pFilterType_in;
	//processNameList = processNameList_in;

	return TRUE;
}

BOOL WINAPI SetLowKeyboardHook()
{
	g_hLLKeyBoardHook = SetWindowsHookEx(
                  WH_KEYBOARD_LL,      /* Type of hook */
                  LLKeyboardHookProc,    /* Hook process */
                  ModuleFromAddress(LLKeyboardHookProc),//hInstance,        /* Instance */
                  NULL);

	return g_hLLKeyBoardHook != NULL;
}

BOOL WINAPI SetLowMouseHook()
{
	g_hLLMouseHook = SetWindowsHookEx(
                  WH_MOUSE_LL,      /* Type of hook */
                  LLMouseHookProc,    /* Hook process */
                  ModuleFromAddress(LLMouseHookProc),//hInstance,        /* Instance */
                  NULL);

	return g_hLLMouseHook != NULL;
}

BOOL WINAPI UnlockLowKeyboardHook()
{
	BOOL bOk = UnhookWindowsHookEx(g_hLLKeyBoardHook);
	g_hLLKeyBoardHook = NULL;

	return bOk;
}

BOOL WINAPI UnlockLowMouseHook()
{
	BOOL bOk = UnhookWindowsHookEx(g_hLLMouseHook);
	g_hLLMouseHook = NULL;

	return bOk;
}

BOOL WINAPI SetCopyMonitor()
{
	HINSTANCE hinst = GetModuleHandle(NULL);

	WNDCLASSEX wnd;
	memset( &wnd, 0, sizeof( wnd ) );
	wnd.cbSize = sizeof( wnd );
    wnd.lpszClassName = _T("clipmonitor");
    wnd.lpfnWndProc = (WNDPROC)ClipMonitorProc;
    wnd.hInstance = hinst;
    RegisterClassEx(&wnd);
	copyHwnd = CreateWindowEx(0, wnd.lpszClassName, _T("Clip Monitor"),
		 0, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, NULL, NULL, wnd.hInstance, NULL);
	copyViewer = SetClipboardViewer(copyHwnd);

	return TRUE;
}

BOOL WINAPI CloseCopyMonitor()
{
	//CloseWindow(copyHwnd);
	//CloseWindow(copyViewer);

	return TRUE;
}

void close_data_collect()
{
	int rc = sqlite3_open_v2((config.logDir+"/log.db3").c_str(), &db, SQLITE_OPEN_READWRITE, NULL);
	if(rc > 0)
	{
		printf_s("open database error...");
	}

	char* share_mouse = (used == 0 ? shared_mouse_memory1 : shared_mouse_memory2);
	char* share_key = (used == 0 ? shared_key_memory1 : shared_key_memory2);
	char* share_click = (used == 0 ? shared_click_memory1 : shared_click_memory2);
	char* share_copy = (used == 0 ? shared_copy_memory1 : shared_copy_memory2);

	printf_s("%d %d %d\n", mLen, kLen, cLen);

	string strMouse(share_mouse, share_mouse+mLen);
	string strKey(share_key, share_key+kLen);
	string strClick(share_click, share_click+cLen);
	string strCopy(share_copy, share_copy+copyLen);

	process_mouse_string(strMouse);
	process_click_string(strClick);
	if(config.keyMode == 1)
		process_key_string(strKey);
	if(config.copyMode == 1)
		process_copy_string(strCopy);

	char* errMsg = 0;
	string strsql = "update tbl_record set end_time='" + GetSysLocalTimeStr() + "' where start_time = (select max(start_time) from tbl_record)";
	sqlite3_exec(db, strsql.c_str(), NULL, 0, &errMsg);

	sqlite3_close(db);

	UnlockLowKeyboardHook();
	UnlockLowMouseHook();
	CloseCopyMonitor();

	errlog.close();
	
	CloseHandle(hWriteDisk);

	//DeleteCriticalSection(&cs_mouse);
	//DeleteCriticalSection(&cs_click);
	//DeleteCriticalSection(&cs_mouse);
}

BOOL IsNeedProcess(std::string processName)
{	
	if(config.filter == FilterType::None)
	{
		return FALSE;
	}

	if(config.filter == FilterType::WhiteList)
	{
		bool flag = FALSE;
		for(int i=0; i<config.processes.size(); i++)
		{
			int index = processName.find(config.processes[i]);
			if(index >= 0)
			{
				flag = TRUE;
				break;
			}
		}

		if(!flag)
		{
			return FALSE;
		}
	}
	else if(config.filter == FilterType::BlackList)
	{
		for(int i=0; i<config.processes.size(); i++)
		{
			int index = processName.find(config.processes[i]);
			if(index >= 0)
			{
				return FALSE;
			}
		}
	}

	return TRUE;
}

BOOL isNeedScreenCaptured(string window, string strtime)
{
	if(config.screenCaptureMode == 0)
	{
		return FALSE;
	}
	else if(config.screenCaptureMode == 1)
	{
		if(window_map.find(window) != window_map.end())
		{
			double interval = GetTimeDifference(toSystemTime(window_map[window]), toSystemTime(strtime));
			cout<<window<<"#"<<interval<<endl;
			if(GetTimeDifference(toSystemTime(window_map[window]), toSystemTime(strtime)) > 30)
			{
				window_map[window] = strtime;
				return TRUE;
			}
			else
			{
				//window_map.insert(make_pair(window, strtime));
				return FALSE;
			}
		}
		
		window_map.insert(make_pair(window, strtime));
		return TRUE;
	}
	
	return TRUE;
}

LRESULT CALLBACK LLKeyboardHookProc(int nCode, WPARAM wParam, LPARAM lParam)
{
	if (nCode < 0 || nCode == HC_NOREMOVE)
		return ::CallNextHookEx(NULL, nCode, wParam, lParam);
	
   if (lParam & 0x40000000)	// Check the previous key state
	{
		return ::CallNextHookEx(NULL, nCode, wParam, lParam);
	}

	if(wParam == WM_KEYDOWN)
	{
		//time_t timer;
		//time(&timer);

		KBDLLHOOKSTRUCT  *pkbhs = (KBDLLHOOKSTRUCT *)lParam;

		//check that the message is from keyboard or is synthesized by SendInput API
		if((pkbhs->flags & LLKHF_INJECTED))
			return ::CallNextHookEx(NULL, nCode, wParam, lParam);
		
		SYSTEMTIME sys;
		GetLocalTime( &sys );
		std::string strTime = GetSysLocalTimeStr(sys);

		HWND hwnd = GetForegroundWindow();
		std::string windowname = GetWindowNameStr(hwnd);
		ReplaceAll(windowname,"\n","\\n");
		ReplaceAll(windowname,"\t","\\t");

		DWORD processId;
		std::string processName = GetProcessNameStr(hwnd,&processId);
		std::string parentWindowName = GetNotNullParentNameStr(hwnd);

		if(!IsNeedProcess(processName))
		{
			return CallNextHookEx(NULL, nCode, wParam, lParam);
		}

		DWORD dwMsg = 1;
		dwMsg += pkbhs->scanCode << 16;
		dwMsg += pkbhs->flags << 24;

		CHAR strKey[80];
		GetKeyNameTextA(dwMsg,strKey,80);

		POINT point;
		GUITHREADINFO pg;
		pg.cbSize=48;
		::GetGUIThreadInfo(NULL,&pg);
		if (pg.hwndCaret)
		{
			point.x=pg.rcCaret.right;
			point.y=pg.rcCaret.bottom;
			::ClientToScreen(pg.hwndCaret,&point);
		}
		else
		{
			point.x = point.y = -1;
		}
		
		RECT winRect;
		GetWindowRect(hwnd,&winRect);

		stringstream ss;
		ss<<strTime.c_str()<<endl
			<<strKey<<"#"<<point.x<<" "<<point.y<<endl
			<<windowname.c_str()<<"#"<<winRect.left<<" "<<winRect.top<<" "<<winRect.right<<" "<<winRect.bottom<<endl
			<<processName.c_str()<<endl
			<<parentWindowName.c_str()<<endl;

		EnterCriticalSection(&cs_key);

		if(kLen<MAX_LEN)
		{
			char* shared = (used == 0 ? shared_key_memory1 : shared_key_memory2);
			memcpy(shared+kLen, ss.str().c_str(), ss.str().length());
			kLen += ss.str().length();
		}

		LeaveCriticalSection(&cs_key);		
		
		/*
		double interval = GetTimeDifference(preKeyTime,sys);	
		if(interval<0 || interval > 1)
		{
			preKeyTime = sys;
			
			std::string img = "log/screen/" + strTime +  ".png";
			GetScreeny(SCREEN_RECT,from_string(img).c_str(),100);
		}
		*/
	}

	return CallNextHookEx(NULL, nCode, wParam, lParam);
}

LRESULT CALLBACK LLMouseHookProc(int nCode, WPARAM wParam, LPARAM lParam)
{
	if(nCode < 0)
	{ 
		return CallNextHookEx(NULL, nCode, wParam, lParam);
	}

	if(wParam != WM_LBUTTONDOWN && !config.mouseMode) 
	{
		return CallNextHookEx(NULL, nCode, wParam, lParam);
	}

	PMSLLHOOKSTRUCT pHookStruct = (PMSLLHOOKSTRUCT) lParam;
	POINT point = pHookStruct->pt;
	
	HWND hwnd = WindowFromPoint(point);
	if(hwnd == NULL)
	{
		return CallNextHookEx(NULL, nCode, wParam, lParam);
	}

	std::string windowName = GetWindowNameStr(hwnd);
	ReplaceAll(windowName,"\n","\\n");
	ReplaceAll(windowName,"\t","\\t");

	DWORD processId;
	std::string processName = GetProcessNameStr(hwnd, &processId);

	std::string parentWindowName = GetNotNullParentNameStr(hwnd);
	ReplaceAll(parentWindowName,"\n","\\n");
	ReplaceAll(parentWindowName,"\t","\\t");

	SYSTEMTIME sys;
	GetLocalTime( &sys );
	std::string strTime = GetSysLocalTimeStr(sys);

	if(wParam == WM_MOUSEMOVE)
	{
		double interval = GetTimeDifference(preMouseMoveTime,sys);	
		if(processId == preProcessId && hwnd == preWindow && interval<0.5)
		{
			preMouseMoveTime = sys;
			return CallNextHookEx(NULL, nCode, wParam, lParam);
		}
		preMouseMoveTime = sys;
	}

	RECT winRect;
	GetWindowRect(hwnd,&winRect);
	if(winRect.left<0)
	{
		winRect.right = winRect.right + winRect.left;
		winRect.left = 0;
	}
	if(winRect.top<0)
	{
		winRect.bottom = winRect.bottom + winRect.top;
		winRect.top = 0;
	}

	std::string msgName = GetMouseEventNameStr(wParam);
	stringstream ss;
	if(!IsNeedProcess(processName))
	{
		if(!is_no_logged)
		{
			ss<<strTime<<endl
				<<"NOT LOGGED APPLICATION"<<endl;

			EnterCriticalSection(&cs_mouse);
			if(mLen<MAX_LEN)
			{
				char* shared = (used == 0 ? shared_mouse_memory1 : shared_mouse_memory2);

				memcpy(shared+mLen, ss.str().c_str(), ss.str().length());
				mLen += ss.str().length();
			}
			LeaveCriticalSection(&cs_mouse);
		}

		preProcessId = processId;
		preWindow = hwnd;
		is_no_logged = TRUE;
		return CallNextHookEx(NULL, nCode, wParam, lParam);
	}
	else
	{
		is_no_logged = FALSE;

		ss<<strTime.c_str()<<endl
			<<msgName.c_str()<<"#"<<point.x<<" "<<point.y<<endl
			<<windowName.c_str()<<"#"<<winRect.left<<" "<<winRect.top<<" "<<winRect.right<<" "<<winRect.bottom<<endl
			<<processName.c_str()<<endl
			<<parentWindowName.c_str()<<endl;
	}

	EnterCriticalSection(&cs_mouse);
	if(mLen<MAX_LEN)
	{
		char* shared = (used == 0 ? shared_mouse_memory1 : shared_mouse_memory2);

		memcpy(shared+mLen, ss.str().c_str(), ss.str().length());
		mLen += ss.str().length();
	}
	LeaveCriticalSection(&cs_mouse);

	if(wParam == WM_LBUTTONDOWN)
	{
		double interval = GetTimeDifference(preLClickTime,sys);
		
		if(interval > 0.5 || preLeftDownWindow != hwnd)
		{
			ParamData p;
			p.pt = point;
			p.sys = sys;

			HANDLE thread = CreateThread(NULL, 0, AccessUIThreadFunction, (LPVOID)&p, 0, NULL);  	
			if(WAIT_TIMEOUT == WaitForSingleObject(thread,500))
			{
				errlog<<strTime<<" thread time out\n";
			}
			
			CloseHandle(thread);
			
			if(processName != "explorer.exe" && interval > 1)
				//isNeedScreenCaptured(windowName, strTime))
			{
				std::string img = "log/screen/" + strTime +  ".png";
				GetScreeny(SCREEN_RECT,from_string(img).c_str(),100);
			}

		}
		preLClickTime = sys;
		preLeftDownWindow = hwnd;

	}
	else if(wParam == WM_MOUSEWHEEL)
	{
		double interval = GetTimeDifference(preMouseWheelTime,sys);
		if(interval > 1 || preWheelWindow !=  hwnd)
		{
			std::string img = "log/screen/" + strTime +  ".png";
			GetScreeny(SCREEN_RECT,from_string(img).c_str(),100);
		}
		preMouseWheelTime = sys;
		preWheelWindow = hwnd;
	}

	preProcessId = processId;
	preWindow = hwnd;

	return CallNextHookEx(NULL, nCode, wParam, lParam);
}

DWORD WINAPI AccessUIThreadFunction( LPVOID lpParam )
{
	try
    { 
	HRESULT hr;
	CoInitializeEx(NULL,COINIT_MULTITHREADED);

	PParamData data = (PParamData)lpParam;

	SYSTEMTIME sys = data->sys;
	POINT point = data->pt;
	string strTime =  GetSysLocalTimeStr(sys); 

	IUIAutomationElement* element = NULL;

	hr = g_pAutomation->ElementFromPoint(point, &element);

	if(element == NULL || S_OK != hr)
	{
		printf_s("Cann't get Element\n");
		
		if(element != NULL)
		{
			element->Release();
		}
		CoUninitialize();
		return 1;
	}

	std::string elementDesc = GetElementDescStr(element);
	std::string elementName = GetElementNameStr(element);
	std::string elementValue = GetElementValueStr(element);

	string pname, ptype;
	GetElementParentNameWStr(g_pControlWalker,element, pname, ptype);

	ReplaceAll(elementValue,"\n","\\n");
	ReplaceAll(elementValue,"\t","\\t");

	//SAFEARRAY* rumtimeId;
	//element->GetRuntimeId(&rumtimeId);

	RECT bounding;
	element->get_CurrentBoundingRectangle(&bounding);
	//std::string runtimeId = GetRuntimeIDStr(rumtimeId);
	
	stringstream ss;
	ss<<strTime.c_str()<<endl
		<<elementDesc.c_str()<<endl
		<<elementName.c_str()<<endl
		<<bounding.left<<" "<<bounding.top<<" "<<bounding.right<<" "<<bounding.bottom<<endl
		<<pname.c_str()<<endl
		<<ptype.c_str()<<endl
		<<elementValue.c_str()<<endl;

	EnterCriticalSection(&cs_click);
	if(cLen<MAX_LEN)
	{
		char* shared = (used == 0 ? shared_click_memory1 : shared_click_memory2);

		memcpy(shared+cLen, ss.str().c_str(), ss.str().length());
		cLen += ss.str().length();
	}
	LeaveCriticalSection(&cs_click);
		
	if(element != NULL)
	{
		element->Release();
	}

	//SafeArrayDestroy(rumtimeId);

	CoUninitialize();
	}
	catch(std::exception &e)
	{
		printf_s("exception: %s\n", e.what()); 
	}
	return 0; 
}

LRESULT CALLBACK ClipMonitorProc(HWND hwnd,UINT uMsg,WPARAM wParam,LPARAM lParam)
{
	if(uMsg == WM_CHANGECBCHAIN ||  uMsg == WM_DRAWCLIPBOARD)
	{
		 if (!OpenClipboard(NULL))
		 {
			return DefWindowProc(hwnd, uMsg, wParam, lParam);;
		 }
		 HANDLE hData = GetClipboardData(CF_TEXT);
		 if(hData == NULL)
		 {
			//printf("Get Clip Board Data Error\n");
			CloseClipboard();
			return DefWindowProc(hwnd, uMsg, wParam, lParam);
		 }
		
		 char* pszText = static_cast<char*>( GlobalLock(hData) );
		 std::string text(pszText);
		 ReplaceAll(text, "\n", "\\n");
		 ReplaceAll(text, "\t", "\\t");

		 GlobalUnlock( hData );
		 CloseClipboard();

		 SYSTEMTIME sys;
		 GetLocalTime( &sys );
		 std::string timestamp = GetSysLocalTimeStr(sys);
		 
		 //cout<<timestamp<<endl;

		 HWND focusHwnd = GetForegroundWindow();
		 string windowname = GetWindowNameStr(focusHwnd);
		 ReplaceAll(windowname, "\n", "\\n");
		 ReplaceAll(windowname, "\t", "\\t");

		 string parentWindowName = GetNotNullParentNameStr(focusHwnd);
		 ReplaceAll(parentWindowName, "\n", "\\n");
		 ReplaceAll(parentWindowName, "\t", "\\t");

		 DWORD processId;
		 string processName = GetProcessNameStr(focusHwnd, &processId);

		 if(!IsNeedProcess(processName))
		 {
			return DefWindowProc(hwnd, uMsg, wParam, lParam);
		 }
		 
		 stringstream ss;
		 ss<<timestamp.c_str()<<endl
			 <<windowname.c_str()<<endl
			 <<processName.c_str()<<endl
			 <<parentWindowName.c_str()<<endl
			 <<text.c_str()<<endl;

		 EnterCriticalSection(&cs_copy);
		 if(copyLen<MAX_LEN)
		 {
			char* shared = (used == 0 ? shared_copy_memory1 : shared_copy_memory2);
			memcpy(shared+copyLen, ss.str().c_str(), ss.str().length());
			copyLen += ss.str().length();
		 }
		 LeaveCriticalSection(&cs_copy);
	}
	
	return DefWindowProc(hwnd, uMsg, wParam, lParam);;
}

void process_mouse_string(string strMouse)
{
	sqlite3_stmt* mouse_stmt;

	string sqlstr = "insert into tbl_mouse_event(timestamp, event_name, p_x, p_y, window_name, win_rect_left, win_rect_top, win_rect_right, win_rect_bottom, process_name, parent_window)";
	sqlstr += " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	if(sqlite3_prepare_v2(db, sqlstr.c_str(), -1, &mouse_stmt, NULL) != SQLITE_OK)
	{
		printf_s("database error\n");
		return;
	}

	vector<string> strList = split(strMouse, "\n");
	int i=0;
	
	char* sErrMsg = 0;
	string timestamp;
	sqlite3_exec(db, "BEGIN TRANSACTION", NULL, NULL, &sErrMsg);
	while(i<strList.size())
	{
		try
		{
			timestamp = strList.at(i++);
			trim(timestamp);
			if(timestamp == "")
			{
				continue;
			}

			timestamp = ToTimeFormatInDB(timestamp);
			string eventName = strList.at(i++);
			int px, py;
			px = py = -1;
			string window = "";
			RECT rect;
			rect.left = rect.top = rect.right = rect.bottom = -1;
			string process = "";
			string parent_window = "";

			if(eventName == "NOT LOGGED APPLICATION")
			{
				eventName = "UNKNOWN";
				window = "UNKNOWN";
				process = "UNKNOWN";
				parent_window = "UNKNOWN";
			}
			else
			{
				int index = eventName.find_last_of("#");
				string strPoint = eventName.substr(index+1);
				eventName = eventName.substr(0, index);
				vector<string> vPoint = split(strPoint, " ");
				px = _StringToNumber<int>(vPoint.at(0));
				py = _StringToNumber<int>(vPoint.at(1));

				window = strList.at(i++);
				index = window.find_last_of("#");
				string strRect = window.substr(index+1);
				window = gb2utf8(window.substr(0,index));

				vector<string> vRect = split(strRect, " ");
				rect.left = _StringToNumber<int>(vRect.at(0));
				rect.top = _StringToNumber<int>(vRect.at(1));
				rect.right = _StringToNumber<int>(vRect.at(2));
				rect.bottom = _StringToNumber<int>(vRect.at(3));

				process = strList.at(i++);
				parent_window = gb2utf8(strList.at(i++));
			}

			//printf_s("%s %s\n", timestamp.c_str(), window.c_str());

			int index = 1;
			
			sqlite3_bind_text(mouse_stmt, index++, timestamp.c_str(), timestamp.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(mouse_stmt, index++, eventName.c_str(), eventName.length(), SQLITE_TRANSIENT);
			sqlite3_bind_int(mouse_stmt, index++, px);
			sqlite3_bind_int(mouse_stmt, index++, py);
			sqlite3_bind_text(mouse_stmt, index++, window.c_str(), window.length(), SQLITE_TRANSIENT);
			sqlite3_bind_int(mouse_stmt, index++, rect.left);
			sqlite3_bind_int(mouse_stmt, index++, rect.top);
			sqlite3_bind_int(mouse_stmt, index++, rect.right);
			sqlite3_bind_int(mouse_stmt, index++, rect.bottom);
			sqlite3_bind_text(mouse_stmt, index++, process.c_str(), process.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(mouse_stmt, index++, parent_window.c_str(), parent_window.length(), SQLITE_TRANSIENT);

			sqlite3_step(mouse_stmt);
			sqlite3_clear_bindings(mouse_stmt);
			sqlite3_reset(mouse_stmt);
			
		}
		catch(const std::exception& e)
		{
			if(errlog.is_open())
			{
				errlog << "Mouse String Format Error: " << e.what() << '\n';
				errlog << strMouse;
			}
		}
	}

	sqlite3_finalize(mouse_stmt);
	sqlite3_exec(db, "END TRANSACTION", NULL, NULL, &sErrMsg);
}

void process_key_string(string strKey)
{
	sqlite3_stmt* key_stmt;

	string sqlstr = "insert into tbl_key_event(timestamp, event_name, p_x, p_y, window_name, win_rect_left, win_rect_top, win_rect_right, win_rect_bottom, process_name, parent_window)";
	sqlstr += " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	if(sqlite3_prepare_v2(db, sqlstr.c_str(), -1, &key_stmt, NULL) != SQLITE_OK)
	{
		printf_s("database error\n");
		return;
	}

	vector<string> strList = split(strKey, "\n");
	int i=0;
	
	char* sErrMsg = 0;
	string timestamp;
	sqlite3_exec(db, "BEGIN TRANSACTION", NULL, NULL, &sErrMsg);
	while(i<strList.size())
	{
		try
		{
			timestamp = strList.at(i++);
			trim(timestamp);
			//printf_s("%s", timestamp.c_str());
			if(timestamp == "")
			{
				continue;
			}

			timestamp = ToTimeFormatInDB(timestamp);
			string eventName = strList.at(i++);
			int px, py;
			px = py = -1;
			string window = "";
			RECT rect;
			rect.left = rect.top = rect.right = rect.bottom = -1;
			string process = "";
			string parent_window = "";

			if(eventName == "NOT LOGGED APPLICATION")
			{
				eventName = "UNKNOWN";
				window = "UNKNOWN";
				process = "UNKNOWN";
				parent_window = "UNKNOWN";
			}
			else
			{
				int index = eventName.find_last_of("#");
				string strPoint = eventName.substr(index+1);
				eventName = eventName.substr(0, index);
				vector<string> vPoint = split(strPoint, " ");
				px = _StringToNumber<int>(vPoint.at(0));
				py = _StringToNumber<int>(vPoint.at(1));

				window = strList.at(i++);
				//printf_s("%s", window.c_str());
				index = window.find_last_of("#");
				string strRect = window.substr(index+1);
				window = gb2utf8(window.substr(0,index));
				
				vector<string> vRect = split(strRect, " ");
				rect.left = _StringToNumber<int>(vRect.at(0));
				rect.top = _StringToNumber<int>(vRect.at(1));
				rect.right = _StringToNumber<int>(vRect.at(2));
				rect.bottom = _StringToNumber<int>(vRect.at(3));

				process = strList.at(i++);
				parent_window = gb2utf8(strList.at(i++));
			}

			int index = 1;
			sqlite3_bind_text(key_stmt, index++, timestamp.c_str(), timestamp.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(key_stmt, index++, eventName.c_str(), eventName.length(), SQLITE_TRANSIENT);
			sqlite3_bind_int(key_stmt, index++, px);
			sqlite3_bind_int(key_stmt, index++, py);
			sqlite3_bind_text(key_stmt, index++, window.c_str(), window.length(), SQLITE_TRANSIENT);
			sqlite3_bind_int(key_stmt, index++, rect.left);
			sqlite3_bind_int(key_stmt, index++, rect.top);
			sqlite3_bind_int(key_stmt, index++, rect.right);
			sqlite3_bind_int(key_stmt, index++, rect.bottom);
			sqlite3_bind_text(key_stmt, index++, process.c_str(), process.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(key_stmt, index++, parent_window.c_str(), parent_window.length(), SQLITE_TRANSIENT);

			sqlite3_step(key_stmt);
			sqlite3_clear_bindings(key_stmt);
			sqlite3_reset(key_stmt);
		}
		catch(const std::exception& e)
		{
			if(errlog.is_open())
			{
				errlog << "Keyboard String Format: " << e.what() << '\n';
				errlog << strKey;
			}
		}
	}

	sqlite3_finalize(key_stmt);
	sqlite3_exec(db, "END TRANSACTION", NULL, NULL, &sErrMsg);
}

void process_click_string(string strClick)
{
	sqlite3_stmt* click_stmt;

	string sqlstr = "insert into tbl_click_action(timestamp, action_name, action_type, action_value, bound_left, bound_top, bound_right, bound_bottom, action_parent_name, action_parent_type)";
	sqlstr += " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	if(sqlite3_prepare_v2(db, sqlstr.c_str(), -1, &click_stmt, NULL) != SQLITE_OK)
	{
		printf_s("database error\n");
		return;
	}

	vector<string> strList = split(strClick, "\n");
	int i=0;
	
	char* sErrMsg = 0;
	sqlite3_exec(db, "BEGIN TRANSACTION", NULL, NULL, &sErrMsg);
	string timestamp;
	while(i<strList.size())
	{
		try
		{
			timestamp = strList.at(i++);
			trim(timestamp);
			if(timestamp == "")
			{
				continue;
			}

			timestamp = ToTimeFormatInDB(timestamp);
			string actionType = gb2utf8(strList.at(i++));
			string actionName = gb2utf8(strList.at(i++));
			string actionBound = strList.at(i++);
			vector<string> bRect = split(actionBound, " ");
			RECT bound;
			bound.left = _StringToNumber<int>(bRect.at(0));
			bound.top = _StringToNumber<int>(bRect.at(1));
			bound.right = _StringToNumber<int>(bRect.at(2));
			bound.bottom = _StringToNumber<int>(bRect.at(3));
			string actionParentName = gb2utf8(strList.at(i++));
			string actionParentType = gb2utf8(strList.at(i++));
			string actionValue = gb2utf8(strList.at(i++));

			int index = 1;
			sqlite3_bind_text(click_stmt, index++, timestamp.c_str(), timestamp.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(click_stmt, index++, actionName.c_str(), actionName.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(click_stmt, index++, actionType.c_str(), actionType.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(click_stmt, index++, actionValue.c_str(), actionValue.length(), SQLITE_TRANSIENT);
			sqlite3_bind_int(click_stmt, index++, bound.left);
			sqlite3_bind_int(click_stmt, index++, bound.top);
			sqlite3_bind_int(click_stmt, index++, bound.right);
			sqlite3_bind_int(click_stmt, index++, bound.bottom);
			sqlite3_bind_text(click_stmt, index++, actionParentName.c_str(), actionParentName.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(click_stmt, index++, actionParentType.c_str(), actionParentType.length(), SQLITE_TRANSIENT);

			sqlite3_step(click_stmt);
			sqlite3_clear_bindings(click_stmt);
			sqlite3_reset(click_stmt);

		}
		catch(const std::exception& e)
		{
			if(errlog.is_open())
			{
				errlog << "Click Action Format: " << e.what() << '\n';
				errlog << strClick;
			}
		}

	}

	sqlite3_finalize(click_stmt);
	sqlite3_exec(db, "END TRANSACTION", NULL, NULL, &sErrMsg);
}

void process_copy_string(string strCopy)
{
	sqlite3_stmt* copy_stmt;

	string sqlstr = "insert into tbl_copy_event(timestamp, window_name, process_name, parent_window, copy_text) values (?, ?, ?, ?, ?)";
	sqlite3_prepare_v2(db, sqlstr.c_str(), -1, &copy_stmt, NULL);

	vector<string> strList = split(strCopy, "\n");
	int i=0;
	
	char* sErrMsg = 0;
	string timestamp;
	sqlite3_exec(db, "BEGIN TRANSACTION", NULL, NULL, &sErrMsg);
	while(i<strList.size())
	{
		try
		{
			timestamp = strList.at(i++);
			//trim(timestamp);
			if(timestamp == "")
			{
				continue;
			}

			//cout<<"copy: "<<timestamp<<endl;

			string windowName = gb2utf8(strList.at(i++));
			string processName = strList.at(i++);
			string parentWindow = gb2utf8(strList.at(i++));
			string text = gb2utf8(strList.at(i++));

			errlog<<"Copy log: "<<timestamp<<"#"<<windowName<<endl;

			int index = 1;
			sqlite3_bind_text(copy_stmt, index++, timestamp.c_str(), timestamp.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(copy_stmt, index++, windowName.c_str(), windowName.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(copy_stmt, index++, processName.c_str(), processName.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(copy_stmt, index++, parentWindow.c_str(), parentWindow.length(), SQLITE_TRANSIENT);
			sqlite3_bind_text(copy_stmt, index++, text.c_str(), text.length(), SQLITE_TRANSIENT);

			sqlite3_step(copy_stmt);
			sqlite3_clear_bindings(copy_stmt);
			sqlite3_reset(copy_stmt);
		}
		catch(const std::exception& e)
		{
			errlog << "Copy Data Error: " << e.what()<< strCopy << '\n';
		}
	}
	sqlite3_finalize(copy_stmt);
	sqlite3_exec(db, "END TRANSACTION", NULL, NULL, &sErrMsg);
}

DWORD WINAPI WriteToDiskThreadFunction( LPVOID lpParam )
{
	int mLenTemp = 0;
	int kLenTemp = 0;
	int cLenTemp = 0;
	int copyLenTemp = 0;

	while(TRUE)
	{
		Sleep(10 * 1000);

		EnterCriticalSection(&cs_mouse);
		EnterCriticalSection(&cs_click);
		if(config.keyMode == 1)
			EnterCriticalSection(&cs_key);
		if(config.copyMode == 1)
			EnterCriticalSection(&cs_copy);

		mLenTemp = mLen;
		cLenTemp = cLen;
		kLenTemp = kLen;
		copyLenTemp = copyLen;
			
		used = 1- used;
		mLen = 0;
		cLen = 0;
		kLen = 0;
		copyLen = 0;

		char* share_mouse = (used == 1 ? shared_mouse_memory1 : shared_mouse_memory2);
		char* share_click = (used == 1 ? shared_click_memory1 : shared_click_memory2);
		char* share_key = (used == 1 ? shared_key_memory1 : shared_key_memory2);
		char* share_copy = (used == 1 ? shared_copy_memory1 : shared_copy_memory2);

		LeaveCriticalSection(&cs_mouse);
		LeaveCriticalSection(&cs_click);
		if(config.keyMode == 1)
			LeaveCriticalSection(&cs_key);
		if(config.copyMode == 1)
			LeaveCriticalSection(&cs_copy);

		cout<<mLenTemp<<" "<<" "<<cLenTemp<<" ";
		
		if(mLenTemp > 0)
		{
			string strMouse(share_mouse, share_mouse+mLenTemp);
			process_mouse_string(strMouse);
			memset(share_mouse, 0, MAX_LEN);
		}
		
		if(cLenTemp > 0)
		{
			string strClick(share_click, share_click+cLenTemp);
			process_click_string(strClick);
			memset(share_click, 0, MAX_LEN);
		}

		if(kLenTemp > 0)
		{
			string strKey(share_key, share_key+kLenTemp);
			process_key_string(strKey);

			cout<<kLenTemp<<" ";
		}
		
		if(copyLenTemp > 0)
		{
			string strCopy(share_copy, share_copy+cLenTemp);
			process_copy_string(strCopy);
			cout<<copyLenTemp<<" ";
		}

		cout<<endl;
	}


}

