#include "activitycollecter.h"
#include <QtWidgets/QApplication>
#include "../HCICollectDll/HCICollectDll.h"
#include <QtPlugin>
Q_IMPORT_PLUGIN(QWindowsIntegrationPlugin)

#pragma comment(lib, "qwindowsd.lib")

int main(int argc, char *argv[])
{
	QApplication a(argc, argv);
	ActivityCollecter w;
	w.show();
	return a.exec();
}
