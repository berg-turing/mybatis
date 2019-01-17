package com.berg.base.xmlconfigures.databaseidprovider;

import org.apache.ibatis.mapping.DatabaseIdProvider;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public class MyDatabaseIdProvider implements DatabaseIdProvider {

    @Override
    public void setProperties(Properties properties) {

    }

    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        return null;
    }
}
