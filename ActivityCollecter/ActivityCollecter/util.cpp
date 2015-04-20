#include "util.h"

vector<string> toVector(QList<QString> qlist)
{
	vector<string> v;
	for(int i=0; i<qlist.size(); i++)
	{
		v.push_back(qlist[i].toStdString());
	}
	return v;
}

std::string GetSysLocalTimeStr()
{
	SYSTEMTIME sys;
    GetLocalTime( &sys );

	char timestr[30];

	sprintf_s(timestr, "%4d-%02d-%02d %02d:%02d:%02d.%03d", sys.wYear, sys.wMonth, sys.wDay,
		sys.wHour, sys.wMinute,sys.wSecond, sys.wMilliseconds);

	return std::string(timestr);
}