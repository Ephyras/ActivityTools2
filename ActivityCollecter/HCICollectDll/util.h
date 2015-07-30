#pragma once
#include "stdafx.h"
#include<string>
#include<ctime>
#include <codecvt>
#include <Oleacc.h>
#include <OleAuto.h>
#include <UIAutomation.h>
#include <UIAutomationClient.h>
#include <sstream>

//#include "opencv2/core/core.hpp"
//#include "opencv2/imgproc/imgproc.hpp"
//#include "opencv2/highgui/highgui.hpp" 
#ifndef PSAPI_VERSION
#define PSAPI_VERSION 1
#endif

#include<Psapi.h>

#include <GdiPlus.h>

using namespace std;

std::string __declspec(dllexport) WINAPI GetTimeStr(time_t timer);
std::wstring __declspec(dllexport) WINAPI GetTimeWStr(time_t timer);

std::wstring __declspec(dllexport) WINAPI GetWindowNameWStr(HWND hwnd);
std::string __declspec(dllexport) WINAPI GetWindowNameStr(HWND hwnd);

std::wstring __declspec(dllexport) WINAPI GetWindowClassWStr(HWND hwnd);
std::string __declspec(dllexport) WINAPI GetWindowClassStr(HWND hwnd);

std::wstring __declspec(dllexport) WINAPI GetProcessNameWStr(HWND hwnd, DWORD *processId);
std::string __declspec(dllexport) WINAPI GetProcessNameStr(HWND hwnd, DWORD *processId);

std::wstring __declspec(dllexport) WINAPI GetMouseEventNameWStr(WPARAM wParam);
std::string __declspec(dllexport) WINAPI GetMouseEventNameStr(WPARAM wParam);

std::wstring __declspec(dllexport) WINAPI GetNotNullParentNameWStr(HWND hwnd);
std::string __declspec(dllexport) WINAPI GetNotNullParentNameStr(HWND hwnd);

std::wstring __declspec(dllexport) GetSysLocalTimeWStr();
std::string __declspec(dllexport) GetSysLocalTimeStr();
std::string __declspec(dllexport) ToTimeFormatInDB(string timestr);

std::string __declspec(dllexport) GetSysLocalTimeStr(SYSTEMTIME sys);

std::string __declspec(dllexport) to_utf8(const wchar_t* buffer, int len);
std::string __declspec(dllexport) to_utf8(const std::wstring& str);

std::wstring __declspec(dllexport) from_string(const std::string& str);

//cv::Mat GetScreenRect(cv::Rect rect);

std::wstring __declspec(dllexport) WINAPI GetElementDescWStr(IUIAutomationElement* element);
std::string __declspec(dllexport) WINAPI GetElementDescStr(IUIAutomationElement* element);

std::wstring __declspec(dllexport) WINAPI GetElementNameWStr(IUIAutomationElement* element);
std::string __declspec(dllexport) WINAPI GetElementNameStr(IUIAutomationElement* element);

std::wstring __declspec(dllexport) WINAPI GetElementValueWStr(IUIAutomationElement* element);
std::string __declspec(dllexport) WINAPI GetElementValueStr(IUIAutomationElement* element);

std::wstring __declspec(dllexport) WINAPI GetRuntimeIDWStr(SAFEARRAY* runtimeId);
std::string __declspec(dllexport) WINAPI GetRuntimeIDStr(SAFEARRAY* runtimeId);

void __declspec(dllexport) WINAPI GetElementParentNameWStr(IUIAutomationTreeWalker* walker, IUIAutomationElement* element, string& pname, string& ptype);
//std::string __declspec(dllexport) WINAPI GetElementParentNameStr(IUIAutomationTreeWalker* walker,IUIAutomationElement* element);


double __declspec(dllexport) GetTimeDifference( SYSTEMTIME &st1, SYSTEMTIME &st2 );

int __declspec(dllexport) GetScreeny(RECT rect,LPCWSTR lpszFilename, ULONG uQuality);

template<typename T>
T __declspec(dllexport) _StringToNumber(const string& numberAsString)
{
    T val;

    std::stringstream stream(numberAsString);
    stream >> val;
    if (stream.fail()) {
        //std::runtime_error e(numberAsString);
        //throw e;
		return -1;
    }

    return val;
}

void  trim(std::string& str);
std::string trim_space(std::string& str);

vector<string> split(string& str, string delim);
string  join(vector<string> sarray, string sep);
void  ReplaceAll(std::string& str, const std::string from,const std::string to);

string  gb2utf8(const char* gb2312);
string  gb2utf8(string gb2312);

SYSTEMTIME toSystemTime(string timestamp, int flag=1);