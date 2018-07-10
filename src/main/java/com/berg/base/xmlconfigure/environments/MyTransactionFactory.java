package com.berg.base.xmlconfigure.environments;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * 自定义的事务工厂
 */
public class MyTransactionFactory implements TransactionFactory {

    public void setProperties(Properties properties) {

    }

    public Transaction newTransaction(Connection connection) {
        return null;
    }

    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel transactionIsolationLevel, boolean b) {
        return null;
    }
}
