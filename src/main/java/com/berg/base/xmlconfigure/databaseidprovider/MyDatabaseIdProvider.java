package com.berg.base.xmlconfigure.databaseidprovider;

import org.apache.ibatis.mapping.DatabaseIdProvider;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public class MyDatabaseIdProvider implements DatabaseIdProvider {

    public void setProperties(Properties properties) {

    }

    public String getDatabaseId(DataSource dataSource) throws SQLException {
        return null;
    }
}
