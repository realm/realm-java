package org.jannocessor.config;

import org.jannocessor.collection.api.PowerList;
import org.jannocessor.model.JavaElement;
import org.jannocessor.model.structure.AbstractJavaClass;
import org.jannocessor.processor.annotation.Annotated;
import org.jannocessor.processor.annotation.Types;
import org.jannocessor.processor.api.CodeProcessor;
import org.jannocessor.processor.api.ProcessingContext;

import com.tightdb.generator.CodeGenerator;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.Table;


/**
 * This is a configuration class and it must have the name
 * "org.jannocessor.config.Processors" by convention. This is the entry point of
 * the annotation processor and contains a list of code processors, as well as
 * their target source code elements, defined by annotation and kind.
 */
public class Processors {

	private CodeGenerator generator = new CodeGenerator();

	@Annotated(Table.class)
	@Types(AbstractJavaClass.class)
	public CodeProcessor<? extends JavaElement> tables() {
		return new CodeProcessor<JavaElement>() {
			@Override
			public void process(PowerList<JavaElement> tables, ProcessingContext context) {
				generator.processTables(tables);
			}
		};
	}

	@Annotated(NestedTable.class)
	@Types(AbstractJavaClass.class)
	public CodeProcessor<? extends JavaElement> subtables() {
		return new CodeProcessor<JavaElement>() {
			@Override
			public void process(PowerList<JavaElement> subtables, ProcessingContext context) {
				generator.processSubtables(subtables);
			}
		};
	}

}
