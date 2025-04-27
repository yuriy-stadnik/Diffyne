package com.syv.data.Diffyne;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.DriverManager;
import java.sql.SQLException;

//@SpringBootApplication(exclude = {
//		DataSourceAutoConfiguration.class,
//		HibernateJpaAutoConfiguration.class,
//		DataSourceTransactionManagerAutoConfiguration.class
//})
@SpringBootApplication
@EnableScheduling
public class DiffyneApplication {
//	static{
//		try {
//			DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
//	}
	public static void main(String[] args) {
		SpringApplication.run(DiffyneApplication.class, args);
	}

}
