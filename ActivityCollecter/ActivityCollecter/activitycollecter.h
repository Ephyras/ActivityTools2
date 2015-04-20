#ifndef ACTIVITYCOLLECTER_H
#define ACTIVITYCOLLECTER_H

#include <QtWidgets/QWidget>
#include "ui_activitycollecter.h"
#include "configform.h"
#include <iostream>

using namespace std;


class ActivityCollecter : public QWidget
{
	Q_OBJECT

public:
	ActivityCollecter(QWidget *parent = 0);
	~ActivityCollecter();

	bool initDatabase();
	void init();
	
	void closeEvent(QCloseEvent* event);

public slots:
	void start();
	void openConfig();
private:
	Ui::ActivityCollecterClass ui;

	ConfigForm* config;
};

#endif // ACTIVITYCOLLECTER_H
