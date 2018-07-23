package com.berg.base.xmlconfigures.environments;

import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义的事务控制
 */
public class MyTransaction implements Transaction{
    public Connection getConnection() throws SQLException {
        return null;
    }

    public void commit() throws SQLException {

    }

    public void rollback() throws SQLException {

    }

    public void close() throws SQLException {

    }

    public Integer getTimeout() throws SQLException {
        return null;
    }
}
