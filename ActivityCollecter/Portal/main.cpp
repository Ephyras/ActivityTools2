#include "portal.h"
#include <QtWidgets/QApplication>

int main(int argc, char *argv[])
{
	QApplication a(argc, argv);
	Portal w;
	w.show();
	return a.exec();
}
