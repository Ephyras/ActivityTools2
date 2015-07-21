#ifndef PORTAL_H
#define PORTAL_H

#include <QtWidgets/QMainWindow>
#include "ui_portal.h"
#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <psapi.h>

class Portal : public QMainWindow
{
	Q_OBJECT

public:
	Portal(QWidget *parent = 0);
	~Portal();

public slots:
	void startCollecter();
private:
	Ui::PortalClass ui;
};

#endif // PORTAL_H
