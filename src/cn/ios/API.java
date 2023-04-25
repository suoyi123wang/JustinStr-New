package cn.ios;

import cn.ios.casegen.constraint.ConstraintFactory;
import cn.ios.casegen.generator.GenerationFactory;
import cn.ios.report.ReportFactory;

public class API {

	public static void generateTestCaseAfterConfig() {
		ConstraintFactory.processConstraints();
		GenerationFactory.generateClasses();
		ReportFactory.genReport();
	}
}