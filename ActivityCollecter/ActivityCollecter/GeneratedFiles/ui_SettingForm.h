/********************************************************************************
** Form generated from reading UI file 'SettingForm.ui'
**
** Created by: Qt User Interface Compiler version 5.2.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_SETTINGFORM_H
#define UI_SETTINGFORM_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QCheckBox>
#include <QtWidgets/QGridLayout>
#include <QtWidgets/QGroupBox>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QListWidget>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QTabWidget>
#include <QtWidgets/QToolButton>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_SettingForm
{
public:
    QVBoxLayout *verticalLayout;
    QTabWidget *tabWidget;
    QWidget *tab;
    QLabel *label_2;
    QToolButton *toolBtnLogDir;
    QLineEdit *editLogDir;
    QWidget *tab_2;
    QListWidget *listProcess;
    QCheckBox *checkBoxProc;
    QPushButton *btnAddProc;
    QPushButton *btnDelProc;
    QWidget *tab_3;
    QVBoxLayout *verticalLayout_4;
    QGroupBox *groupBox;
    QVBoxLayout *verticalLayout_2;
    QCheckBox *cbAllMouse;
    QCheckBox *cbMouseClick;
    QGroupBox *groupBox_2;
    QVBoxLayout *verticalLayout_3;
    QCheckBox *cbKeyinput;
    QGroupBox *groupBox_3;
    QVBoxLayout *verticalLayout_5;
    QCheckBox *cbCopy;
    QGroupBox *groupBox_4;
    QGridLayout *gridLayout;
    QCheckBox *cbSCNever;
    QCheckBox *cbSCWindow;
    QCheckBox *cbSCEvery;

    void setupUi(QWidget *SettingForm)
    {
        if (SettingForm->objectName().isEmpty())
            SettingForm->setObjectName(QStringLiteral("SettingForm"));
        SettingForm->resize(400, 355);
        verticalLayout = new QVBoxLayout(SettingForm);
        verticalLayout->setObjectName(QStringLiteral("verticalLayout"));
        tabWidget = new QTabWidget(SettingForm);
        tabWidget->setObjectName(QStringLiteral("tabWidget"));
        tab = new QWidget();
        tab->setObjectName(QStringLiteral("tab"));
        label_2 = new QLabel(tab);
        label_2->setObjectName(QStringLiteral("label_2"));
        label_2->setGeometry(QRect(10, 20, 101, 16));
        toolBtnLogDir = new QToolButton(tab);
        toolBtnLogDir->setObjectName(QStringLiteral("toolBtnLogDir"));
        toolBtnLogDir->setGeometry(QRect(340, 50, 25, 19));
        editLogDir = new QLineEdit(tab);
        editLogDir->setObjectName(QStringLiteral("editLogDir"));
        editLogDir->setGeometry(QRect(10, 50, 321, 20));
        tabWidget->addTab(tab, QString());
        tab_2 = new QWidget();
        tab_2->setObjectName(QStringLiteral("tab_2"));
        listProcess = new QListWidget(tab_2);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        new QListWidgetItem(listProcess);
        listProcess->setObjectName(QStringLiteral("listProcess"));
        listProcess->setGeometry(QRect(10, 30, 341, 192));
        checkBoxProc = new QCheckBox(tab_2);
        checkBoxProc->setObjectName(QStringLiteral("checkBoxProc"));
        checkBoxProc->setGeometry(QRect(10, 10, 131, 17));
        btnAddProc = new QPushButton(tab_2);
        btnAddProc->setObjectName(QStringLiteral("btnAddProc"));
        btnAddProc->setGeometry(QRect(10, 230, 75, 23));
        btnDelProc = new QPushButton(tab_2);
        btnDelProc->setObjectName(QStringLiteral("btnDelProc"));
        btnDelProc->setGeometry(QRect(90, 230, 75, 23));
        tabWidget->addTab(tab_2, QString());
        tab_3 = new QWidget();
        tab_3->setObjectName(QStringLiteral("tab_3"));
        verticalLayout_4 = new QVBoxLayout(tab_3);
        verticalLayout_4->setObjectName(QStringLiteral("verticalLayout_4"));
        groupBox = new QGroupBox(tab_3);
        groupBox->setObjectName(QStringLiteral("groupBox"));
        groupBox->setCheckable(false);
        groupBox->setChecked(false);
        verticalLayout_2 = new QVBoxLayout(groupBox);
        verticalLayout_2->setObjectName(QStringLiteral("verticalLayout_2"));
        cbAllMouse = new QCheckBox(groupBox);
        cbAllMouse->setObjectName(QStringLiteral("cbAllMouse"));
        cbAllMouse->setAutoExclusive(false);

        verticalLayout_2->addWidget(cbAllMouse);

        cbMouseClick = new QCheckBox(groupBox);
        cbMouseClick->setObjectName(QStringLiteral("cbMouseClick"));
        cbMouseClick->setChecked(true);

        verticalLayout_2->addWidget(cbMouseClick);


        verticalLayout_4->addWidget(groupBox);

        groupBox_2 = new QGroupBox(tab_3);
        groupBox_2->setObjectName(QStringLiteral("groupBox_2"));
        verticalLayout_3 = new QVBoxLayout(groupBox_2);
        verticalLayout_3->setObjectName(QStringLiteral("verticalLayout_3"));
        cbKeyinput = new QCheckBox(groupBox_2);
        cbKeyinput->setObjectName(QStringLiteral("cbKeyinput"));

        verticalLayout_3->addWidget(cbKeyinput);


        verticalLayout_4->addWidget(groupBox_2);

        groupBox_3 = new QGroupBox(tab_3);
        groupBox_3->setObjectName(QStringLiteral("groupBox_3"));
        verticalLayout_5 = new QVBoxLayout(groupBox_3);
        verticalLayout_5->setObjectName(QStringLiteral("verticalLayout_5"));
        cbCopy = new QCheckBox(groupBox_3);
        cbCopy->setObjectName(QStringLiteral("cbCopy"));

        verticalLayout_5->addWidget(cbCopy);


        verticalLayout_4->addWidget(groupBox_3);

        groupBox_4 = new QGroupBox(tab_3);
        groupBox_4->setObjectName(QStringLiteral("groupBox_4"));
        gridLayout = new QGridLayout(groupBox_4);
        gridLayout->setObjectName(QStringLiteral("gridLayout"));
        cbSCNever = new QCheckBox(groupBox_4);
        cbSCNever->setObjectName(QStringLiteral("cbSCNever"));

        gridLayout->addWidget(cbSCNever, 0, 0, 1, 1);

        cbSCWindow = new QCheckBox(groupBox_4);
        cbSCWindow->setObjectName(QStringLiteral("cbSCWindow"));
        cbSCWindow->setChecked(true);

        gridLayout->addWidget(cbSCWindow, 1, 0, 1, 1);

        cbSCEvery = new QCheckBox(groupBox_4);
        cbSCEvery->setObjectName(QStringLiteral("cbSCEvery"));

        gridLayout->addWidget(cbSCEvery, 1, 1, 1, 1);


        verticalLayout_4->addWidget(groupBox_4);

        tabWidget->addTab(tab_3, QString());

        verticalLayout->addWidget(tabWidget);


        retranslateUi(SettingForm);

        tabWidget->setCurrentIndex(0);


        QMetaObject::connectSlotsByName(SettingForm);
    } // setupUi

    void retranslateUi(QWidget *SettingForm)
    {
        SettingForm->setWindowTitle(QApplication::translate("SettingForm", "Configuration", 0));
        label_2->setText(QApplication::translate("SettingForm", "Log Directory:", 0));
        toolBtnLogDir->setText(QApplication::translate("SettingForm", "...", 0));
        editLogDir->setText(QApplication::translate("SettingForm", "log", 0));
        tabWidget->setTabText(tabWidget->indexOf(tab), QApplication::translate("SettingForm", "General", 0));

        const bool __sortingEnabled = listProcess->isSortingEnabled();
        listProcess->setSortingEnabled(false);
        QListWidgetItem *___qlistwidgetitem = listProcess->item(0);
        ___qlistwidgetitem->setText(QApplication::translate("SettingForm", "eclipse", 0));
        QListWidgetItem *___qlistwidgetitem1 = listProcess->item(1);
        ___qlistwidgetitem1->setText(QApplication::translate("SettingForm", "javaw", 0));
        QListWidgetItem *___qlistwidgetitem2 = listProcess->item(2);
        ___qlistwidgetitem2->setText(QApplication::translate("SettingForm", "chrome", 0));
        QListWidgetItem *___qlistwidgetitem3 = listProcess->item(3);
        ___qlistwidgetitem3->setText(QApplication::translate("SettingForm", "firefox", 0));
        QListWidgetItem *___qlistwidgetitem4 = listProcess->item(4);
        ___qlistwidgetitem4->setText(QApplication::translate("SettingForm", "iexplore", 0));
        QListWidgetItem *___qlistwidgetitem5 = listProcess->item(5);
        ___qlistwidgetitem5->setText(QApplication::translate("SettingForm", "devenv", 0));
        QListWidgetItem *___qlistwidgetitem6 = listProcess->item(6);
        ___qlistwidgetitem6->setText(QApplication::translate("SettingForm", "WINWORD", 0));
        QListWidgetItem *___qlistwidgetitem7 = listProcess->item(7);
        ___qlistwidgetitem7->setText(QApplication::translate("SettingForm", "EXCEL", 0));
        listProcess->setSortingEnabled(__sortingEnabled);

        checkBoxProc->setText(QApplication::translate("SettingForm", "Record All Processes", 0));
        btnAddProc->setText(QApplication::translate("SettingForm", "Add ", 0));
        btnDelProc->setText(QApplication::translate("SettingForm", "Delete", 0));
        tabWidget->setTabText(tabWidget->indexOf(tab_2), QApplication::translate("SettingForm", "Process Filter", 0));
        groupBox->setTitle(QApplication::translate("SettingForm", "Mouse", 0));
        cbAllMouse->setText(QApplication::translate("SettingForm", "All Mouse Event", 0));
        cbMouseClick->setText(QApplication::translate("SettingForm", "Only Mouse Click", 0));
        groupBox_2->setTitle(QApplication::translate("SettingForm", "Keyboard", 0));
        cbKeyinput->setText(QApplication::translate("SettingForm", "Keyboard Input", 0));
        groupBox_3->setTitle(QApplication::translate("SettingForm", "Clipboard", 0));
        cbCopy->setText(QApplication::translate("SettingForm", "Copy Event(only text)", 0));
        groupBox_4->setTitle(QApplication::translate("SettingForm", "Screen-capture", 0));
        cbSCNever->setText(QApplication::translate("SettingForm", "Never", 0));
        cbSCWindow->setText(QApplication::translate("SettingForm", "For New Window", 0));
        cbSCEvery->setText(QApplication::translate("SettingForm", "Every Important Event", 0));
        tabWidget->setTabText(tabWidget->indexOf(tab_3), QApplication::translate("SettingForm", "Collected Data", 0));
    } // retranslateUi

};

namespace Ui {
    class SettingForm: public Ui_SettingForm {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_SETTINGFORM_H
