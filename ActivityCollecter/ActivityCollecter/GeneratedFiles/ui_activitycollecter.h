/********************************************************************************
** Form generated from reading UI file 'activitycollecter.ui'
**
** Created by: Qt User Interface Compiler version 5.2.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_ACTIVITYCOLLECTER_H
#define UI_ACTIVITYCOLLECTER_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_ActivityCollecterClass
{
public:
    QPushButton *btnStart;
    QPushButton *btnSetting;

    void setupUi(QWidget *ActivityCollecterClass)
    {
        if (ActivityCollecterClass->objectName().isEmpty())
            ActivityCollecterClass->setObjectName(QStringLiteral("ActivityCollecterClass"));
        ActivityCollecterClass->resize(358, 50);
        btnStart = new QPushButton(ActivityCollecterClass);
        btnStart->setObjectName(QStringLiteral("btnStart"));
        btnStart->setGeometry(QRect(10, 10, 281, 31));
        btnSetting = new QPushButton(ActivityCollecterClass);
        btnSetting->setObjectName(QStringLiteral("btnSetting"));
        btnSetting->setGeometry(QRect(310, 10, 32, 32));
        QIcon icon;
        icon.addFile(QStringLiteral("setting.png"), QSize(), QIcon::Normal, QIcon::Off);
        btnSetting->setIcon(icon);

        retranslateUi(ActivityCollecterClass);

        QMetaObject::connectSlotsByName(ActivityCollecterClass);
    } // setupUi

    void retranslateUi(QWidget *ActivityCollecterClass)
    {
        ActivityCollecterClass->setWindowTitle(QApplication::translate("ActivityCollecterClass", "ActivityCollecter", 0));
        btnStart->setText(QApplication::translate("ActivityCollecterClass", "Start to Record", 0));
        btnSetting->setText(QString());
    } // retranslateUi

};

namespace Ui {
    class ActivityCollecterClass: public Ui_ActivityCollecterClass {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_ACTIVITYCOLLECTER_H
