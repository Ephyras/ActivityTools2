#include"util.h"


void trim(std::string& str)
{
	string::size_type pos = str.find_last_not_of(" \f\n\r\t\v");
	if(pos != string::npos) 
	{
		str.erase(pos + 1);
		pos = str.find_first_not_of(" \f\n\r\t\v");
		if(pos != string::npos) 
		{
			str.erase(0, pos);
		}
	}
	else 
	{
		str.erase(str.begin(), str.end());
	}
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

std::wstring str2wstr(const std::string& s)
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

std::string wstr2str(const wchar_t* buffer, int len)
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

std::string wstr2str(wstring wstr)
{
	return wstr2str(wstr.c_str(), wstr.length());
}

string getUserName()
{
	TCHAR username[UNLEN+1];
	DWORD username_len = UNLEN+1;
	GetUserName(username, &username_len);

	return wstr2str(username, username_len-1);
}

string getSysLocalTimeStr()
{
	SYSTEMTIME sys;
    GetLocalTime( &sys );

	char timestr[30];

	sprintf_s(timestr, "%4d-%02d-%02d %02d:%02d:%02d.%03d", sys.wYear, sys.wMonth, sys.wDay,
		sys.wHour, sys.wMinute,sys.wSecond, sys.wMilliseconds);

	return std::string(timestr);
}