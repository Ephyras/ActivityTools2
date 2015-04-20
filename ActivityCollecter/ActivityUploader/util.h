#pragma once
#include<iostream>
#include<string>
#include<vector>
#include<sstream>
#include<Windows.h>
#include<Lmcons.h>
using namespace std;

void trim(std::string& str);

vector<string> split(string& str, string delim);

string join(vector<string> sarray, string sep);

void replaceAll(std::string& str,const std::string from,const std::string to);

std::wstring str2wstr(const std::string& s);

std::string wstr2str(const wchar_t* buffer, int len);
std::string wstr2str(wstring wstr);

string getUserName();

string getSysLocalTimeStr();

string dbTime2ImgTime(const string& dbTime);

template<typename T>
T str2Num(const string& numberAsString)
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