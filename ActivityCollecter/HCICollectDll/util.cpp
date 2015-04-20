#include "stdafx.h"
#include "util.h"
#include <iostream>

std::wstring WINAPI GetMouseEventNameWStr(WPARAM wParam)
{	
	std::wstring eventName = _T("");
	switch( wParam  )
	{
		case WM_MOUSEMOVE:
		case WM_NCMOUSEMOVE:
			eventName = _T("MOUSE MOVE");
			break;
		case WM_LBUTTONDOWN:
		case WM_NCLBUTTONDOWN:
			eventName = _T("MOUSE LEFT DOWN");
			break;
		case WM_RBUTTONDOWN:
		case WM_NCRBUTTONDOWN:
			eventName = _T("MOUSE RIGHT DOWN");
			break;
		case WM_LBUTTONUP:
		case WM_NCLBUTTONUP:
			eventName = _T("MOUSE LEFT UP");
			break;
		case WM_RBUTTONUP:
		case WM_NCRBUTTONUP:
			eventName = _T("MOUSE RIGHT UP");
			break;
		case WM_LBUTTONDBLCLK:
			eventName = _T("MOUSE DOUBLE CLICK");
			break;
		case WM_MOUSEWHEEL:
			eventName = _T("MOUSE WHEEL");
			break;
		default:
			eventName = _T("MOUSE UNKNOWN");
   }

	return eventName;
}

std::string WINAPI GetMouseEventNameStr(WPARAM wParam)
{
	return to_utf8(GetMouseEventNameWStr(wParam));
}

std::wstring WINAPI GetWindowNameWStr(HWND hwnd)
{
  int textlen = GetWindowTextLength(hwnd);
 
  if(textlen == 0)
  {
	return _T("");
  }

  TCHAR *temp = new TCHAR[textlen+1]; 
  GetWindowText(hwnd, temp, textlen+1);
  
  std::wstring name(temp);

  delete[] temp;

  return name;
}

std::string WINAPI GetWindowNameStr(HWND hwnd)
{
	return to_utf8(GetWindowNameWStr(hwnd));
}

std::wstring WINAPI GetWindowClassWStr(HWND hwnd)
{
	const int MAXLEN = 255;
	TCHAR temp[MAXLEN];
	GetClassName(hwnd, temp, MAXLEN);
	std::wstring className(temp);

	return className;
}

std::string WINAPI GetWindowClassStr(HWND hwnd)
{
	return to_utf8(GetWindowClassWStr(hwnd));
}

std::wstring WINAPI GetProcessNameWStr(HWND hwnd, DWORD *processId)
{
	//TCHAR processName[255];
	const int MAXLEN = 255;

	GetWindowThreadProcessId(hwnd,processId);
	HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION |
							PROCESS_VM_READ, FALSE, *processId);
	if(hProcess == NULL)
	{
		CloseHandle(hProcess);
		return _T("");
	}

	TCHAR temp[MAXLEN];
	int res = GetProcessImageFileName(hProcess,temp,MAXLEN);
	CloseHandle(hProcess);

	if(res <= 0 || !temp)
	{
		return _T("");
	}

	std::wstring str(temp);

	int index = str.find_last_of(_T("\\"));
	if(index >= 0)
	{
		return str.substr(index+1);
	}
	else
	{
		return str;
	}
}

std::string WINAPI GetProcessNameStr(HWND hwnd, DWORD *processId)
{
	std::wstring temp = GetProcessNameWStr(hwnd, processId);

	return to_utf8(temp);
}

std::string GetSysLocalTimeStr()
{
	SYSTEMTIME sys;
    GetLocalTime( &sys );

	char timestr[30];

	sprintf_s(timestr, "%4d-%02d-%02d-%02d-%02d-%02d-%03d", sys.wYear, sys.wMonth, sys.wDay,
		sys.wHour, sys.wMinute,sys.wSecond, sys.wMilliseconds);

	return std::string(timestr);
}

std::string GetSysLocalTimeStr(SYSTEMTIME sys)
{
	char timestr[256];

	sprintf_s(timestr, "%4d-%02d-%02d-%02d-%02d-%02d-%03d", sys.wYear, sys.wMonth, sys.wDay,
		sys.wHour, sys.wMinute,sys.wSecond, sys.wMilliseconds);

	return std::string(timestr);
}

std::string ToTimeFormatInDB(string timestr)
{
	vector<string> vstr = split(timestr, "-");
	if(vstr.size() != 7)
		return "";

	stringstream ss;
	ss<<vstr[0]<<"-"<<vstr[1]<<"-"<<vstr[2]<<" "<<vstr[3]<<":"
		<<vstr[4]<<":"<<vstr[5]<<"."<<vstr[6];

	return ss.str();
}

std::wstring GetSysLocalTimeWStr()
{
	SYSTEMTIME sys;
    GetLocalTime( &sys );

	TCHAR timestr[30];

	wsprintf(timestr, _T("%4d-%02d-%02d-%02d-%02d-%02d-%03d"), sys.wYear, sys.wMonth, sys.wDay,
		sys.wHour, sys.wMinute,sys.wSecond, sys.wMilliseconds);

	return std::wstring(timestr);
}

std::string WINAPI GetTimeStr(time_t timer)
{
	tm ptm;
	errno_t err = localtime_s(&ptm,&timer);
	char buffer[32];
	// Format: Mo, 15.06.2009 20:20:00
	//strftime(buffer, 32, "%a, %d.%m.%Y %H:%M:%S", ptm); 
	if(err != 0)
		return "";

	strftime(buffer, 32, "%Y-%m-%d-%H-%M-%S", &ptm); 

	return std::string(buffer);
}

std::wstring WINAPI GetTimeWStr(time_t timer)
{
	std::wstring_convert<codecvt_utf8_utf16<wchar_t>> converter;
	std::string temp = GetTimeStr(timer);
	return converter.from_bytes(temp);
}

std::wstring WINAPI GetNotNullParentNameWStr(HWND hwnd)
{
	HWND parent = GetParent(hwnd);

	if(parent != NULL)
	{
		std::wstring name = GetWindowNameWStr(parent);
		if(name == _T(""))
		{
			return GetNotNullParentNameWStr(parent);
		}
		else
		{
			return name;
		}
	}
	else
	{
		return _T("");
	}
}

std::string WINAPI GetNotNullParentNameStr(HWND hwnd)
{
	HWND parent = GetParent(hwnd);

	if(parent != NULL)
	{
		std::string name = GetWindowNameStr(parent);
		trim(name);
		if(name == "")
		{
			return GetNotNullParentNameStr(parent);
		}
		else
		{
			return name;
		}
	}
	else
	{
		return "";
	}
}

std::string to_utf8(const wchar_t* buffer, int len)
{
	//assert(buffer != NULL);
	if(buffer == NULL)
	{
		return "";
	}
  
	int nLen = ::WideCharToMultiByte(CP_ACP, 0, buffer, -1, NULL, 0, NULL, NULL);  
	if (nLen == 0)   
	{  
		return "";  
	}  
	
		std::string newbuffer;
		newbuffer.resize(nLen) ;
	::WideCharToMultiByte(CP_ACP, 0, buffer, -1, const_cast< char* >(newbuffer.c_str()), nLen, NULL, NULL);  

    return newbuffer;
}

/*
string to_utf8(const wchar_t* buffer, int len)
{
        int nChars = ::WideCharToMultiByte(
                CP_UTF8,
                0,
                buffer,
                len,
                NULL,
                0,
                NULL,
                NULL);
        if (nChars == 0) return "";

        string newbuffer;
        newbuffer.resize(nChars) ;
        ::WideCharToMultiByte(
                CP_UTF8,
                0,
                buffer,
                len,
                const_cast< char* >(newbuffer.c_str()),
                nChars,
                NULL,
                NULL); 

        return newbuffer;
}
*/

std::string to_utf8(const std::wstring& str)
{
       return to_utf8(str.c_str(), (int)str.size());
}

/*
cv::Mat GetScreenRect(cv::Rect rect)
{
	HDC hwindowDC,hwindowCompatibleDC;

    int srcheight,srcwidth;
    HBITMAP hbwindow;
    cv::Mat src;
    BITMAPINFOHEADER  bi;

    hwindowDC = GetDC(NULL); //screen HDC
    hwindowCompatibleDC = CreateCompatibleDC(hwindowDC);
    SetStretchBltMode(hwindowCompatibleDC,COLORONCOLOR);  

	srcwidth = GetSystemMetrics(SM_CXSCREEN);
	srcheight = GetSystemMetrics(SM_CYSCREEN);

    src.create(rect.height,rect.width,CV_8UC4);

    // create a bitmap
    hbwindow = CreateCompatibleBitmap( hwindowDC, rect.width, rect.height);
    bi.biSize = sizeof(BITMAPINFOHEADER);   
    bi.biWidth = rect.width;    
    bi.biHeight = -rect.height;  //this is the line that makes it draw upside down or not
    bi.biPlanes = 1;    
    bi.biBitCount = 32;    
    bi.biCompression = BI_RGB;    
    bi.biSizeImage = 0;  
    bi.biXPelsPerMeter = 0;    
    bi.biYPelsPerMeter = 0;    
    bi.biClrUsed = 0;    
    bi.biClrImportant = 0;

    // use the previously created device context with the bitmap
    SelectObject(hwindowCompatibleDC, hbwindow);
    // copy from the window device context to the bitmap device context
	StretchBlt( hwindowCompatibleDC, 0,0, rect.width, rect.height, hwindowDC, rect.x, rect.y,rect.width,rect.height, SRCCOPY | CAPTUREBLT); //change SRCCOPY to NOTSRCCOPY for wacky colors !
    GetDIBits(hwindowCompatibleDC,hbwindow,0,rect.height,src.data,(BITMAPINFO *)&bi,DIB_RGB_COLORS);  //copy from hwindowCompatibleDC to hbwindow

	DeleteObject(hbwindow);
	ReleaseDC(NULL, hwindowDC);
	DeleteDC(hwindowCompatibleDC);

	return src;
}
*/

void ReplaceAll(std::string& str,const std::string from,const std::string to)
{
	if(from.length() == 0) return;

	std::string::size_type start_pos = 0;
	while((start_pos = str.find(from, start_pos)) != std::string::npos) {
         str.replace(start_pos, from.length(), to);
         start_pos += to.length(); // ...
	}
}

std::wstring WINAPI GetElementDescWStr(IUIAutomationElement* element)
{
	BSTR desc = NULL;
	//element->get_CachedLocalizedControlType(&desc);
	element->get_CurrentLocalizedControlType(&desc);

	if(desc)
	{
		wstring strDesc(desc);
		SysFreeString(desc);
		return strDesc;
	}
	else
	{
		SysFreeString(desc);
		return _T("");
	}
}

std::string WINAPI GetElementDescStr(IUIAutomationElement* element)
{
	return to_utf8(GetElementDescWStr(element));
}

std::wstring WINAPI GetElementNameWStr(IUIAutomationElement* element)
{
	BSTR name = NULL;
	//element->get_CachedName(&name);
	element->get_CurrentName(&name);
	
	if(name)
	{
		wstring strName(name);
		SysFreeString(name);
		return strName;
	}
	else
	{
		SysFreeString(name);
		return _T("");
	}
	
}

std::string WINAPI GetElementNameStr(IUIAutomationElement* element)
{
	return to_utf8(GetElementNameWStr(element));
}

std::wstring WINAPI GetElementValueWStr(IUIAutomationElement* element)
{
	IValueProvider* pattern = NULL; 
	//HRESULT hr = element->GetCachedPattern(UIA_ValuePatternId,(IUnknown**)&pattern);
	HRESULT hr = element->GetCurrentPattern(UIA_ValuePatternId,(IUnknown**)&pattern);

	if(hr == S_OK && pattern != NULL)
	{
		BSTR value = NULL;
		pattern->get_Value(&value);

		if(value)
		{
			wstring strValue(value);
			SysFreeString(value);
			return strValue;
		}
		SysFreeString(value);
	}

	
	return _T("");
}

std::string WINAPI GetElementValueStr(IUIAutomationElement* element)
{
	return to_utf8(GetElementValueWStr(element));
}

bool isWantedControl(string type)
{
	//string CONTROL_TYPE[] = {"window", "dialog", "menu item", "tab", "tool bar", "pane", "窗口", "对话框", "菜单项目", "工具栏", "窗格", "Tab 键"};
	wstring CONTROL_TYPE[] = {_T("window"), _T("dialog"), _T("menu item"), _T("tab"), _T("tool bar"), _T("pane"), _T("窗口"), _T("对话框"), _T("菜单项目"), _T("工具栏"), _T("窗格"), _T("Tab 键")};

	for(int i=0; i<12; i++)
	{
		if(type == to_utf8(CONTROL_TYPE[i]))
		{
			//printf_s("Get Parent %s\n", type.c_str());
			return true;
		}
	}
	return false;
}

void WINAPI GetElementParentNameWStr(IUIAutomationTreeWalker* walker, IUIAutomationElement* element,  string& pname, string& ptype)
{
	IUIAutomationElement* parent;
	//HRESULT hr = element->GetCachedParent(&parent);
	
	HRESULT hr = walker->GetParentElement(element, &parent);

	if(hr == S_OK && parent != NULL)
	{
		pname = GetElementNameStr(parent);
		ptype = GetElementDescStr(parent);
		trim(pname);
		//printf_s("Parent %s %s %d\n", ptype.c_str(), "窗格",ptype == to_utf8(_T("窗格")));
		if(pname == "" || !isWantedControl(ptype))
		{
			return GetElementParentNameWStr(walker, parent, pname, ptype);
		}
		else
		{
			//printf_s("Parent %s %s\n", ptype.c_str(), pname.c_str());
			return;
		}
	}

	pname = "";
	ptype = "";
	return;
}
/*
std::string WINAPI GetElementParentNameStr(IUIAutomationTreeWalker* walker, IUIAutomationElement* element)
{
	return to_utf8(GetElementParentNameWStr(walker, element));
}
*/

std::wstring WINAPI GetRuntimeIDWStr(SAFEARRAY* runtimeId)
{
	if(runtimeId == NULL)
	{
		return _T("");
	}

	ULONG nElements = runtimeId->rgsabound->cElements;
	long* pData = (long*)runtimeId->pvData;

	wstring strId;
	for(int i=0; i<nElements; i++)
	{
		strId += to_wstring((_Longlong)pData[i]);
		strId += _T(" ");
	}

	return strId;
}

std::string WINAPI GetRuntimeIDStr(SAFEARRAY* runtimeId)
{
	return to_utf8(GetRuntimeIDWStr(runtimeId));
}

double GetTimeDifference( SYSTEMTIME &st1, SYSTEMTIME &st2 )
{
    FILETIME        ft1, ft2;
    //LARGE_INTEGER   li1, li2, liDiff;
    //DWORD dwDiff;

    SystemTimeToFileTime( &st1, &ft1 );
    SystemTimeToFileTime( &st2, &ft2 );

	ULARGE_INTEGER ul1;
    ul1.LowPart = ft1.dwLowDateTime;
    ul1.HighPart = ft1.dwHighDateTime;

	ULARGE_INTEGER ul2;
    ul2.LowPart = ft2.dwLowDateTime;
    ul2.HighPart = ft2.dwHighDateTime;

	ULARGE_INTEGER uliRetValue;
    uliRetValue.QuadPart = 0;

	if(ul2.QuadPart >  ul1.QuadPart)
	{
		ul2.QuadPart -= ul1.QuadPart;
		uliRetValue = ul2;
		return (double)uliRetValue.QuadPart / 10 / 1000 / 1000;
	}
	else
	{
		ul1.QuadPart -= ul2.QuadPart;
		uliRetValue = ul1;
		return 0-(double)uliRetValue.QuadPart / 10 / 1000 / 1000;
	}

}

std::wstring from_string(const std::string& s)
{
	//std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	//std::wstring wide = converter.from_bytes(str);

	int len;
    int slength = (int)s.length() + 1;
    len = MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, 0, 0); 
    wchar_t* buf = new wchar_t[len];
    MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, buf, len);
    std::wstring r(buf);
    delete[] buf;
    return r;

	//return wide;
}

int GetEncoderClsid(WCHAR *format, CLSID *pClsid)
{
        unsigned int num = 0,  size = 0;
        Gdiplus::GetImageEncodersSize(&num, &size);
        if(size == 0) return -1;
        Gdiplus::ImageCodecInfo *pImageCodecInfo = (Gdiplus::ImageCodecInfo *)(malloc(size));
        if(pImageCodecInfo == NULL) return -1;
        Gdiplus:GetImageEncoders(num, size, pImageCodecInfo);
        for(unsigned int j = 0; j < num; ++j)
        {
                if(wcscmp(pImageCodecInfo[j].MimeType, format) == 0){
                        *pClsid = pImageCodecInfo[j].Clsid;
                        free(pImageCodecInfo);
                        return j;
                }    
        }
        free(pImageCodecInfo);
        return -1;
}

int GetScreeny(RECT r,LPCWSTR  lpszFilename, ULONG uQuality) // by Napalm
{
        ULONG_PTR gdiplusToken;
        Gdiplus::GdiplusStartupInput gdiplusStartupInput;
        Gdiplus::GdiplusStartup(&gdiplusToken, &gdiplusStartupInput, NULL);
        //hwnd = GetDesktopWindow(); // get my own window
        //RECT  r;             // the area we are going to capture 
        int w, h;            // the width and height of the area
        HDC dc;              // the container for the area
        int nBPP;
        HDC hdcCapture;
        LPBYTE lpCapture;
        int nCapture;
        int iRes;
        CLSID imageCLSID;
        Gdiplus::Bitmap *pScreenShot;
        //HGLOBAL hMem;
        //int result;

        // get the area of my application's window      
        //GetClientRect(hMyWnd, &r);
        //GetWindowRect(hwnd, &r);
        //dc = GetWindowDC(hwnd);//   GetDC(hMyWnd) ;
        dc = GetDC(NULL);

		w = r.right - r.left;
        h = r.bottom - r.top;
        nBPP = GetDeviceCaps(dc, BITSPIXEL);
        hdcCapture = CreateCompatibleDC(dc);


        // create the buffer for the screenshot
        BITMAPINFO bmiCapture = {
                  sizeof(BITMAPINFOHEADER), w, -h, 1, nBPP, BI_RGB, 0, 0, 0, 0, 0,
        };

        // create a container and take the screenshot
        HBITMAP hbmCapture = CreateDIBSection(dc, &bmiCapture,
                DIB_PAL_COLORS, (LPVOID *)&lpCapture, NULL, 0);

        // failed to take it
        if(!hbmCapture)
        {
                DeleteDC(hdcCapture);
                DeleteDC(dc);
                Gdiplus::GdiplusShutdown(gdiplusToken);
                printf("failed to take the screenshot. err: %d\n", GetLastError());
                return 0;
        }

        // copy the screenshot buffer
        nCapture = SaveDC(hdcCapture);
        SelectObject(hdcCapture, hbmCapture);
		BitBlt(hdcCapture, 0, 0, w, h, dc, r.left, r.top, SRCCOPY);
        RestoreDC(hdcCapture, nCapture);
        DeleteDC(hdcCapture);
        DeleteDC(dc);

        //Gdiplus::GpImage *bob;
        //IStream *ssStr;

        // save the buffer to a file    
        pScreenShot = new Gdiplus::Bitmap(hbmCapture, (HPALETTE)NULL);
        Gdiplus::EncoderParameters encoderParams;
        encoderParams.Count = 1;
        encoderParams.Parameter[0].NumberOfValues = 1;
        encoderParams.Parameter[0].Guid  = Gdiplus::EncoderQuality;
        encoderParams.Parameter[0].Type  = Gdiplus::EncoderParameterValueTypeLong;
        encoderParams.Parameter[0].Value = &uQuality;
        GetEncoderClsid(L"image/png", &imageCLSID);
        iRes = (pScreenShot->Save(lpszFilename, &imageCLSID, &encoderParams) == Gdiplus::Ok);

        delete pScreenShot;
        DeleteObject(hbmCapture);
        Gdiplus::GdiplusShutdown(gdiplusToken);
        return iRes;

}

void trim(std::string& str)
{
  string::size_type pos = str.find_last_not_of(' ');
  if(pos != string::npos) {
    str.erase(pos + 1);
    pos = str.find_first_not_of(' ');
    if(pos != string::npos) str.erase(0, pos);
  }
  else str.erase(str.begin(), str.end());

  pos = str.find_last_not_of('\0');
  if(pos != string::npos) {
    str.erase(pos + 1);
    pos = str.find_first_not_of(' ');
    if(pos != string::npos) str.erase(0, pos);
  }
  else str.erase(str.begin(), str.end());
}

vector<string> split(string& str, string delim)
{
    string::size_type start = 0;
    string::size_type end = str.find(delim);
	vector<string> result;
    while (end != std::string::npos)
    {
        //std::cout << s.substr(start, end - start) << std::endl;
		result.push_back(str.substr(start,end-start));
		start = end + delim.length();
        end = str.find(delim, start);
    }

	result.push_back(str.substr(start,end));

	return result;
}

string join(vector<string> sarray, string sep)
{
	std::stringstream ss;
	for(size_t i = 0; i < sarray.size(); ++i)
	{
	  if(i != 0)
		ss << sep;
	  ss << sarray[i];
	}
	std::string s = ss.str();

	return ss.str();
}

string gb2utf8(const char* gb2312)
{
	if(gb2312 == NULL) return "";

	 int len = MultiByteToWideChar(CP_ACP, 0, gb2312, -1, NULL, 0);
	 wchar_t* wstr = new wchar_t[len+1];
	 memset(wstr, 0, len+1);
	 MultiByteToWideChar(CP_ACP, 0, gb2312, -1, wstr, len);
	 len = WideCharToMultiByte(CP_UTF8, 0, wstr, -1, NULL, 0, NULL, NULL);
	 char* str = new char[len+1];
	 memset(str, 0, len+1);
	 WideCharToMultiByte(CP_UTF8, 0, wstr, -1, str, len, NULL, NULL);
	 if(wstr) delete[] wstr;

	 string res(str, str+len);

	 return res;
}

string gb2utf8(string gb2312)
{
	return gb2utf8(gb2312.c_str());
}

SYSTEMTIME toSystemTime(string timestamp, int flag)
{
	if(flag == 1) //format: YYYY-MM-DD-HH-mm-SS-fff
	{
		SYSTEMTIME time;
		vector<string> sarray = split(timestamp,"-");
		time.wYear = _StringToNumber<WORD>(sarray[0]);
		time.wMonth = _StringToNumber<WORD>(sarray[1]);
		time.wDay = _StringToNumber<WORD>(sarray[2]);
		time.wHour = _StringToNumber<WORD>(sarray[3]);
		time.wMinute = _StringToNumber<WORD>(sarray[4]);
		time.wSecond = _StringToNumber<WORD>(sarray[5]);
		time.wMilliseconds = _StringToNumber<WORD>(sarray[6]);
		return time;
	}
	else  //format: YYYY-MM-DD HH:mm:SS.fff
	{
		SYSTEMTIME time;
		vector<string> sarray = split(timestamp," ");
		
		vector<string> sarray2 = split(sarray[0], "-");
		time.wYear = _StringToNumber<WORD>(sarray2[0]);
		time.wMonth = _StringToNumber<WORD>(sarray2[1]);
		time.wDay = _StringToNumber<WORD>(sarray2[2]);

		string::size_type pos = sarray[1].find(".");
		string pre = sarray[1].substr(0, pos);
		vector<string> sarray3 = split(sarray[1], ":");
		time.wHour = _StringToNumber<WORD>(sarray3[0]);
		time.wMinute = _StringToNumber<WORD>(sarray3[1]);
		time.wSecond = _StringToNumber<WORD>(sarray3[2]);

		time.wMilliseconds = _StringToNumber<WORD>(sarray[1].substr(pos+1));

		return time;
	}
}
