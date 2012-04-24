package com.tightdb.db.valuebeans;

import java.io.Serializable;

import com.tightdb.db.annotation.DataModel;

@DataModel
@SuppressWarnings({ "unused", "serial" })
public class PersonBean implements Serializable
{

	
	private int age;

	private String firstName;

	
	private String lastName;

}
