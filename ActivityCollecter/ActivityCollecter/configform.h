#ifndef CONFIGFORM_H
#define CONFIGFORM_H

#include <QObject>
#include <qwidget.h>
#include "ui_SettingForm.h"

class ConfigForm : public QWidget
{
	Q_OBJECT

public:
	ConfigForm(QWidget *parent=0);
	~ConfigForm();

	bool isAllProcAllowed();
	QList<QString> getProcessFilter();
	QString getLogDir();
	int getMouseMode();
	int getKeyBoardMode();
	int getScreenCapturedMode();
	int getCopyMode();

private:
	Ui::SettingForm ui;
};

#endif // CONFIGFORM_H
