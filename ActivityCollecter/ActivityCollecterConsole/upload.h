#pragma once
#include<iostream>
#include<thread>
#include<windows.h>
#include<Lmcons.h>
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>
#include<sqlite3.h>
#include<hash_map>
#include<vector>
#include"util.h"
using namespace std;


class ResultEntity
{
public:
	bool hasColumn(string col) ;
	string getColumn(string col) ;
	bool addColumn(string col, string value);

private:
	hash_map<string, string> record;
};

class MySqlImpl
{
public:
	MySqlImpl();
	MySqlImpl(string url, string username, string password);
	~MySqlImpl();

	void init(string url, string username, string password);
	void printException(sql::SQLException &e);

	string getLastTime(string user);
	void upload(vector<ResultEntity>& rs, string user);
private:
	sql::Driver *driver;
	sql::Connection *conn;
	bool sucess;
};

class LogUploader
{
public:
	LogUploader(string logDir);
	~LogUploader();

	void run();
	void upload();
	vector<ResultEntity> getLastRecords();
private:
	string user;
	sqlite3* db;
	string lastTime;

	MySqlImpl* serverDb;
};