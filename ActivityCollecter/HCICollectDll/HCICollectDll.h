#pragma once

#include "stdafx.h"
#include "util.h"
#include <Oleacc.h>
#include <OleAuto.h>
#include <UIAutomation.h>
#include <UIAutomationClient.h>
#include <sqlite3.h>
#include <string>
//#include "d3d9.h"
//#include "D3dx9tex.h"

enum FilterType{ BlackList, WhiteList, None, All };

typedef struct struct_ParamData {
	WPARAM wParam;
	LPARAM lParam;
	time_t timer;
	SYSTEMTIME sys;
	POINT pt;
	std::string windowName;
	std::string processName;
	std::string parentWindow;
	HWND hwnd;
} ParamData, *PParamData;


class StructConfiguration
{
public:
	bool mouseMode;
	bool keyMode;
	bool copyMode;
	int screenCaptureMode;
	string logDir;
	FilterType filter;
	vector<string> processes;
	bool isUpload;
	bool isNeedUrl;
	string analysis_job;

	vector<string> keys;
	vector<string> actionKeys;

	StructConfiguration()
	{
		mouseMode = true;
		keyMode = true;
		copyMode = true;
		screenCaptureMode = 0;
		logDir = "log";
		filter = FilterType::All;

		isUpload = false;
		isNeedUrl = false;
		analysis_job = "";
	}
};
//CConfig, *PConfig;

BOOL __declspec(dllexport) WINAPI initDll(StructConfiguration& config);

BOOL __declspec(dllexport) WINAPI SetFitlerForHook(FilterType &pFilterType_in, std::vector<std::string> &processNameList_in);

BOOL __declspec(dllexport) WINAPI SetLowKeyboardHook();
BOOL __declspec(dllexport) WINAPI UnlockLowKeyboardHook();

BOOL __declspec(dllexport) WINAPI SetLowMouseHook();
BOOL __declspec(dllexport) WINAPI UnlockLowMouseHook();

BOOL __declspec(dllexport) WINAPI SetCopyMonitor();
LRESULT CALLBACK ClipMonitorProc(HWND hwnd,UINT uMsg,WPARAM wParam,LPARAM lParam);
BOOL __declspec(dllexport) WINAPI CloseCopyMonitor();

void __declspec(dllexport) close_data_collect();

LRESULT CALLBACK LLKeyboardHookProc(int nCode, WPARAM wParam, LPARAM lParam);
LRESULT CALLBACK LLMouseHookProc(int nCode, WPARAM wParam, LPARAM lParam);

LRESULT CALLBACK LLKeyboardHookProc_txt(int nCode, WPARAM wParam, LPARAM lParam);
LRESULT CALLBACK LLMouseHookProc_txt(int nCode, WPARAM wParam, LPARAM lParam);

DWORD WINAPI AccessUIThreadFunction( LPVOID lpParam );
DWORD WINAPI AccessUIWhenKeyPressThreadFunction( LPVOID lpParam );
DWORD WINAPI WriteToDiskThreadFunction( LPVOID lpParam );

void process_mouse_string(string strMouse);
void process_key_string(string strKey);
void process_click_string(string strClick);
void process_copy_string(string strCopy);

//HRESULT	InitD3D(IDirect3DDevice9** g_pd3dDevice,int* screenW,int* screenH,HWND hWnd);
//BOOL ScreenShot(LPDIRECT3DDEVICE9 lpDevice, std::wstring fileName,RECT rect);