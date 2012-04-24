package com.tightdb.db.main;

import com.tightdb.db.utility.AutoCodeGeneration;
import com.tightdb.db.utility.IConstants;


public class AutoCodeGenerationUtility implements IConstants
{

	public static void main(String[] args)
	{
		AutoCodeGeneration utilityObj = new AutoCodeGeneration();
		utilityObj.generatedAutoCode();
	}

}
