#include "configform.h"

ConfigForm::ConfigForm(QWidget *parent)
	: QWidget(parent)
{
	ui.setupUi(this);

	//connect(ui.cbMouseClick, SIGNAL()
}

ConfigForm::~ConfigForm()
{

}

bool ConfigForm::isAllProcAllowed()
{
	return ui.checkBoxProc->isChecked();
}

QList<QString> ConfigForm::getProcessFilter()
{
	QList<QString> procs;
	
	for(int i = 0; i < ui.listProcess->count(); ++i)
	{
		QListWidgetItem* item = ui.listProcess->item(i);
		procs.push_back(item->text());
	}
	return procs;
}

QString ConfigForm::getLogDir()
{
	return ui.editLogDir->text();
}

int ConfigForm::getMouseMode()
{
	if(ui.cbAllMouse->isChecked())
	{
		return 0;
	}
	else if(ui.cbMouseClick->isChecked())
	{
		return 1;
	}
	return 0;
}

int ConfigForm::getKeyBoardMode()
{
	return ui.cbKeyinput->isChecked() ? 1 : 0;
}

int ConfigForm::getScreenCapturedMode()
{
	if(ui.cbSCNever->isChecked())
	{
		return 0;
	}
	else if(ui.cbSCWindow->isChecked())
	{
		return 1;
	}
	else if(ui.cbSCEvery->isChecked())
	{
		return 2;
	}

	return 0;
}

int ConfigForm::getCopyMode()
{
	return ui.cbCopy->isChecked() ? 1 : 0;
}