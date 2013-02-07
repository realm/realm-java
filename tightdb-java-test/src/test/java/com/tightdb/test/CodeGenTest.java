package com.tightdb.test;

import com.tightdb.DefineTable;

/**
 * A helper class containing model(s) for simple code generation tests.
 */
class CodeGenTest {

	@DefineTable // this is enabled only for occasional local tests
	class someModel {
		String name;
		int age;
	}

}