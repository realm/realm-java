package org.jannocessor.config;

import org.jannocessor.model.JavaElement;
import org.jannocessor.model.structure.AbstractJavaClass;
import org.jannocessor.processor.annotation.Annotated;
import org.jannocessor.processor.annotation.Types;
import org.jannocessor.processor.api.CodeProcessor;

import com.tightdb.generator.CodeGenerator;
import com.tightdb.lib.Table;

/**
 * This is a configuration class and it must have the name
 * "org.jannocessor.config.Processors" by convention. This is the entry point of
 * the annotation processor and contains a list of code processors, as well as
 * their target source code elements, defined by annotation and kind.
 */
public class Processors {

	@Annotated(Table.class)
	@Types(AbstractJavaClass.class)
	public CodeProcessor<? extends JavaElement> tables() {
		return new CodeGenerator();
	}
}
