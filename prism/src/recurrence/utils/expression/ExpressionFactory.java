package recurrence.utils.expression;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;

public class ExpressionFactory
{
	private String validate_method = "";
	private String className, superClassName;

	private static CtClass pmdClass;

	IChecker tc;

	private static final String PACKAGE_NAME = ExpressionFactory.class.getPackage().getName();

	
	/**
	 * Generates a Checker class on the fly that validates a state based on the indexed constraint expresison
	 * @param indexedConstraint condition for a state to be valid
	 * @throws Exception
	 */
	public ExpressionFactory(String indexedConstraint) throws Exception
	{
		className = "Checker" + System.currentTimeMillis();
		superClassName = PACKAGE_NAME + ".IChecker";

		ClassPool pool = ClassPool.getDefault();
		CtClass ic = pool.get(superClassName);
		pmdClass = pool.makeClass(PACKAGE_NAME + "." + className);
		pmdClass.setInterfaces(new CtClass[] { ic });

		validate_method += "\tpublic boolean validate(Object[] state) {\n";
		validate_method += "\t\treturn " + indexedConstraint + "; \n";
		validate_method += "\t}\n";

		pmdClass.addMethod(CtNewMethod.make(validate_method, pmdClass));
		Class<?> clazz = pmdClass.toClass();
		tc = (IChecker) clazz.newInstance();
	}

	
	/**
	 * @return the instance of the IChecker
	 * @throws Exception
	 */
	public IChecker getChecker() throws Exception
	{
		return tc;
	}

}
