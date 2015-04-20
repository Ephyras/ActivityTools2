#include"upload.h"

bool ResultEntity::addColumn(string col, string value)
{
	if(hasColumn(col))
	{
		return false;
	}

	record.insert(make_pair(col, value));
	return true;
}

bool ResultEntity::hasColumn(string col)
{
	if(record.find(col) != record.end())
	{
		return true;
	}
	return false;
}

string ResultEntity::getColumn(string col)
{
	if(hasColumn(col))
	{
		return record[col];
	}

	return "";
}

MySqlImpl::MySqlImpl()
{
	sucess = false;
	init("tcp://155.69.147.247:3306/hci", "blf", "123456");
}

MySqlImpl::MySqlImpl(string url, string username, string password)
{
	sucess = false;
	init(url, username, password);
}

MySqlImpl::~MySqlImpl()
{
	if(sucess)
	{
		delete conn;
	}
}

string MySqlImpl::getLastTime(string user)
{
	string timestamp = "";
	try
	{
		sql::Statement *stmt;
		sql::ResultSet *rs;

		string sql = "select max(timestamp) from tbl_interactions where user_name = '" + string(user.c_str()) + "'";
		
		stmt = conn->createStatement();
		rs = stmt->executeQuery(sql.c_str());
		
		if(rs->next())
		{
			timestamp = string(rs->getString(1).c_str());
		}

		delete rs;
		delete stmt;
	}
	catch(sql::SQLException &exception)
	{
		printException(exception);
	}
	return timestamp;
}

void MySqlImpl::upload(vector<ResultEntity>& rs, string user)
{
	try
	{
		sql::PreparedStatement *prep_stmt;
		
		string sql = "INSERT INTO tbl_interactions(user_name, timestamp, window, application, point_x, point_y, win_rect_left, win_rect_top, win_rect_right, win_rect_bottom, \
						ui_name, ui_type, ui_value, parent_ui_name, parent_ui_type, has_screen, screen) \
													   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		prep_stmt = conn->prepareStatement(sql.c_str());

		for(int i=0; i<rs.size(); i++)
		{
			int idx = 1;
			prep_stmt->setString(idx++, user.c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("timestamp").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("window_name").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("process_name").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("p_x").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("p_y").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("win_rect_left").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("win_rect_top").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("win_rect_right").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("win_rect_bottom").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("action_name").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("action_type").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("action_value").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("action_parent_name").c_str());
			prep_stmt->setString(idx++, rs[i].getColumn("action_parent_value").c_str());
			prep_stmt->setBoolean(idx++, false);
			prep_stmt->setBlob(idx++, NULL);

			prep_stmt->execute();
		}

		delete prep_stmt;
	}
	catch(sql::SQLException &exception)
	{
		printException(exception);
	}
	
}

void MySqlImpl::init(string url, string username, string password)
{
	try
	{
		sql::SQLString DB_HOST(url.c_str());
		sql::SQLString DB_USER(username.c_str());
		sql::SQLString DB_PWD(password.c_str());
		
		driver = get_driver_instance();
		conn = driver->connect(DB_HOST, DB_USER, DB_PWD);
		conn->setSchema("hci");
		
		sucess = !conn->isClosed();
	}
	catch(sql::SQLException &e)
	{
		printException(e);
		sucess = false;
	}
}

void MySqlImpl::printException(sql::SQLException &e)
{
	cout << "# ERR: SQLException in " << __FILE__
			<< "(" << __FUNCTION__ << ") on line " 
			<< __LINE__ << endl;
	cout<< "# ERR: " << e.what();
	cout << " (MySQL error code: " << e.getErrorCode();
	cout << ", SQLState: " << e.getSQLState() << " )" << endl;
}

LogUploader::LogUploader(string logDir)
{
	user = getUserName();

	int rc = sqlite3_open_v2((logDir+"/log.db3").c_str(), &db, SQLITE_OPEN_READONLY, NULL);
	if(rc>0)
	{
		cout<<"open database error..."<<endl;
	}

	sqlite3_busy_timeout(db, 10 * 1000);
	
	serverDb = new MySqlImpl();

	lastTime = serverDb->getLastTime(user);
}

LogUploader::~LogUploader()
{
	sqlite3_close_v2(db);
}

vector<ResultEntity> LogUploader::getLastRecords()
{
	string sql = "select * from tbl_mouse_event a, tbl_click_action b where a.timestamp = b.timestamp ";
	if(lastTime != "")
	{
		sql +=  "and datetime(a.timestamp) > datetime('" + lastTime + "')";
	}
	sql += " order by a.timestamp";

	sqlite3_stmt *statement;    
	vector<ResultEntity> result;

	if ( sqlite3_prepare(db, sql.c_str(), -1, &statement, 0 ) == SQLITE_OK ) 
    {
        int ctotal = sqlite3_column_count(statement);
        int res = 0;

        while ( 1 )         
        {
            res = sqlite3_step(statement);

            if ( res == SQLITE_ROW ) 
            {
				ResultEntity entity;
                for ( int i = 0; i < ctotal; i++ ) 
                {
					string column = sqlite3_column_name(statement, i);
                    string value = (char*)sqlite3_column_text(statement, i);
					entity.addColumn(column, value);
                }
				result.push_back(entity);
            }
            
            if (res == SQLITE_DONE || res==SQLITE_ERROR)    
            {
				cout << "Query Done #num: " << result.size()<< endl;
                break;
            }    
        }
    }

	sqlite3_finalize(statement);

	return result;
}

void LogUploader::upload()
{
	while(true)
	{
		Sleep(5 * 1000);

		cout<<"upload..."<<endl;

		vector<ResultEntity> rs = getLastRecords();

		serverDb->upload(rs, user);

		if(rs.size() > 0)
			lastTime = rs[rs.size()-1].getColumn("timestamp");
	}
}

void LogUploader::run()
{
	thread t(&LogUploader::upload, this);

	t.join();
}