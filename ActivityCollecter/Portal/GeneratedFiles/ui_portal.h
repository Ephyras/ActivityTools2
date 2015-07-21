/********************************************************************************
** Form generated from reading UI file 'portal.ui'
**
** Created by: Qt User Interface Compiler version 5.2.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_PORTAL_H
#define UI_PORTAL_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QMainWindow>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_PortalClass
{
public:
    QWidget *centralWidget;
    QPushButton *startBtn;
    QLineEdit *descEdit;
    QPushButton *configBtn;

    void setupUi(QMainWindow *PortalClass)
    {
        if (PortalClass->objectName().isEmpty())
            PortalClass->setObjectName(QStringLiteral("PortalClass"));
        PortalClass->resize(478, 63);
        centralWidget = new QWidget(PortalClass);
        centralWidget->setObjectName(QStringLiteral("centralWidget"));
        startBtn = new QPushButton(centralWidget);
        startBtn->setObjectName(QStringLiteral("startBtn"));
        startBtn->setGeometry(QRect(10, 20, 101, 25));
        descEdit = new QLineEdit(centralWidget);
        descEdit->setObjectName(QStringLiteral("descEdit"));
        descEdit->setGeometry(QRect(120, 20, 321, 25));
        configBtn = new QPushButton(centralWidget);
        configBtn->setObjectName(QStringLiteral("configBtn"));
        configBtn->setGeometry(QRect(450, 20, 25, 25));
        QIcon icon;
        icon.addFile(QStringLiteral("../../ActivityTracker/src/icons/setting.png"), QSize(), QIcon::Normal, QIcon::Off);
        configBtn->setIcon(icon);
        PortalClass->setCentralWidget(centralWidget);

        retranslateUi(PortalClass);

        QMetaObject::connectSlotsByName(PortalClass);
    } // setupUi

    void retranslateUi(QMainWindow *PortalClass)
    {
        PortalClass->setWindowTitle(QApplication::translate("PortalClass", "Data Collector", 0));
        startBtn->setText(QApplication::translate("PortalClass", "Start to Record", 0));
        descEdit->setPlaceholderText(QApplication::translate("PortalClass", "task description, what will you do, etc", 0));
        configBtn->setText(QString());
    } // retranslateUi

};

namespace Ui {
    class PortalClass: public Ui_PortalClass {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_PORTAL_H
