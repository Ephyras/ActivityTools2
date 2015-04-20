#include "activitycollecter.h"
#include "../HCICollectDll/HCICollectDll.h"
#include <Windows.h>
#include <QMessageBox>
#include <QCloseEvent>
#include "util.h"

#pragma comment (lib,"Psapi.lib")
#pragma comment(lib, "gdiplus.lib")
#pragma comment(lib, "sqlite3.lib")

ActivityCollecter::ActivityCollecter(QWidget *parent)
	: QWidget(parent)
{
	ui.setupUi(this);
	
	config = new ConfigForm();

	init();

	connect(ui.btnStart, SIGNAL(clicked()), this, SLOT(start()));
	connect(ui.btnSetting, SIGNAL(clicked()), this, SLOT(openConfig()));
}

ActivityCollecter::~ActivityCollecter()
{

}

bool ActivityCollecter::initDatabase()
{
	QString dbFile = config->getLogDir() + "/log.db3";

	WIN32_FIND_DATA wfd;
	HANDLE hFind = FindFirstFile((LPCTSTR)dbFile.utf16(), &wfd);
	sqlite3* db;
	char *zErrMsg = 0;

	if(hFind == INVALID_HANDLE_VALUE)
	{
		int rc = sqlite3_open_v2(dbFile.toStdString().c_str(), &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL);
	}
	else
	{
		//CloseHandle(hFind);
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
		<<"window_name varchar(200)," 
		<<"win_rect_left int," 
		<<"win_rect_top int," 
		<<"win_rect_right int,"
		<<"win_rect_bottom int,"
		<<"process_name varchar(30),"
		<<"parent_window varchar(200) )";

	
	int rc1 = sqlite3_exec(db, ss1.str().c_str(), NULL, 0, &zErrMsg);
	if(rc1 > 0) printf(zErrMsg);

	std::stringstream ss2;
	ss2<<"create table tbl_key_event ("
		<<"timestamp varchar(25),"
		<<"event_name varchar(20),"
		<<"p_x int,"
		<<"p_y int,"
		<<"window_name varchar(200),"
		<<"win_rect_left int,"
		<<"win_rect_top int,"
		<<"win_rect_right int,"
		<<"win_rect_bottom int,"
		<<"process_name varchar(30),"
		<<"parent_window varchar(200))";
		
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
		<<"window_name varchar(200),"
		<<"process_name varchar(30),"
		<<"parent_window varchar(200),"
		<<"copy_text text )";
		
	int rc4 = sqlite3_exec(db, ss4.str().c_str(), NULL, 0, &zErrMsg);
	if(rc4 > 0) printf(zErrMsg);

	sqlite3_close_v2(db);
	return rc1 == 0 && rc2 == 0 && rc3 == 0 && rc4 == 0;
}

void ActivityCollecter::init()
{
	QString dir = config->getLogDir();

	WIN32_FIND_DATA wfd;
	HANDLE hFind = FindFirstFile((LPCTSTR)dir.utf16(), &wfd);
	if(hFind == INVALID_HANDLE_VALUE || !(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
	{
		CreateDirectory((LPCTSTR)dir.utf16(),NULL);
		CreateDirectory((LPCTSTR)(dir + "/screen").utf16(),NULL);
	}
	else
	{
		hFind = FindFirstFile((LPCTSTR)(dir + "/screen").utf16(), &wfd);
		if(hFind == INVALID_HANDLE_VALUE || !(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY))
		{
			CreateDirectory((LPCTSTR)(dir + "/screen").utf16(),NULL);
		}
	}

	initDatabase();
}

void ActivityCollecter::start()
{
	if(ui.btnStart->text() == "Start to Record")
	{
		//init();

		vector<string> procs = toVector(config->getProcessFilter());
		FilterType type = FilterType::WhiteList;
		if(config->isAllProcAllowed())
		{
			type = FilterType::All; 
		}
		SetFitlerForHook(type, procs);

		//DataMode dm;
		CConfig dllConfig;
		dllConfig.mouseMode = config->getMouseMode();
		dllConfig.keyMode = config->getKeyBoardMode();
		dllConfig.screenCaptureMode = config->getScreenCapturedMode();
		dllConfig.copyMode = config->getCopyMode();
		dllConfig.logDir = config->getLogDir().toStdString();

		ui.btnStart->setText("Pause");

		showMinimized();
		initDll(dllConfig);
	}
	else if(ui.btnStart->text() == "Pause")
	{
		close_data_collect();

		ui.btnStart->setText("Start to Record");
	}
}

void ActivityCollecter::openConfig()
{
	//config->setWindowModality(Qt::ApplicationModal);
	config->show();
}

void ActivityCollecter::closeEvent(QCloseEvent* event)
{
	if(ui.btnStart->text() == "Pause")
	{
		QMessageBox::information(NULL, "Warning","Can't close, please pause the recorder first");
		event->ignore();
	}
	else
	{
		event->accept();
	}
}