package builgen.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import builgen.util.CodeFormatterUtil;

/**
 * BuilgenAction
 * 
 * @author chenlei
 * @version 1.0.0.qualifier
 * @github https://github.com/Vabshroo/Builgen-plugin
 */
@SuppressWarnings("restriction")
public class BuilgenAction implements IEditorActionDelegate {

	private ISelection selection = null;
	private Shell shell;

	public void alert(Object content) {
		MessageDialog.openInformation(shell, "ÌáÊ¾", content + "");
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		shell = targetEditor.getSite().getShell();
	}

	public void run(IAction action) {
		CompilationUnit compilationUnit = ((CompilationUnit) ((TreeSelection) selection).getFirstElement());

		try {
			// all types
			IType types[] = compilationUnit.getAllTypes();

			if (types.length == 0) {
				alert("Error Type!");
				return;
			}

			// only generate the type found 1st
			IType classFile = types[0];
			if (classFile == null || !classFile.isClass()) {
				alert("Error Type!");
				return;
			}

			// all fields
			IField[] fields = classFile.getFields();
			if (fields.length == 0) {
				alert("No fields found!");
				return;
			}

			// generate getter and setter
			IMethod method = null;
			List<IField> fieldTypes = new ArrayList<IField>();
			for (IField field : fields) {
				if (!field.getSource().contains(" static ") && !field.getSource().contains(" final ")) {
					method = classFile.createMethod(genGetter(field), method, false, null);
					method = classFile.createMethod(genSetter(field), method, false, null);
					fieldTypes.add(field);
				}
			}
			
			// generate constructor
			classFile.createMethod(genConstructor(classFile.getElementName(), fieldTypes), method, false, null);
			
			// generate null param constructor
			classFile.createMethod(genNullParamConstructor(classFile.getElementName()), method, false, null);

			// generate builder
			classFile.createType(genBuilder(classFile.getElementName(), fieldTypes), method, false, null);

			// format file
			String source = CodeFormatterUtil.format(classFile.getSource());
			classFile.delete(false, null);
			classFile.createType(source, null, false, null);
			System.err.println(source);

		} catch (JavaModelException e1) {
			e1.printStackTrace();
			alert("Fatal Error : " + e1.getMessage());
		}

	}

	/**
	 * generate builder
	 * 
	 * @param typeName
	 * @param fieldTypes
	 * @return
	 * @throws JavaModelException
	 * @throws IllegalArgumentException
	 */
		/**
	 * generate builder
	 * 
	 * @param typeName
	 * @param fieldTypes
	 * @return
	 * @throws JavaModelException
	 * @throws IllegalArgumentException
	 */
	private String genBuilder(String typeName, List<IField> fieldTypes)
			throws IllegalArgumentException, JavaModelException {
		StringBuilder builder = new StringBuilder();
		
		String builderTypeName = genBuilderTypeName(typeName);
		
		builder.append("public static class ").append(builderTypeName).append("{").append(typeName).append(" ")
				.append(firstCharLowercase(typeName)).append(";")
				.append(genBuilderConstructor(builderTypeName, typeName)).append("");

		for (IField field : fieldTypes) {

			builder.append(genBuilderMethod(builderTypeName, typeName, Signature.toString(field.getTypeSignature()),
					field.getElementName())).append("");

		}
		builder.append(genStaticCreator(typeName));
		builder.append(genBuilMethod(typeName)).append("}");

		return builder.toString();
	}
	
	public static final String builderTypeName = "Builder";
	
	public static String genBuilderTypeName(String typeName) {
		return typeName + builderTypeName;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	private String genStaticCreator(String beanName) {
		StringBuilder builder = new StringBuilder();
		String builderType =genBuilderTypeName(beanName);
		builder.append("public static ").append(builderType).append(" create(){");
		builder.append("return new ").append(builderType).append("();");
		builder.append("}");
		return builder.toString();
	}

	/**
	 * generate getter
	 * 
	 * @param field
	 * @return
	 * @throws JavaModelException
	 * @throws IllegalArgumentException
	 */
	private String genGetter(IField field) throws IllegalArgumentException, JavaModelException {
		StringBuilder builder = new StringBuilder();

		String fieldType = Signature.toString(field.getTypeSignature());
		String fieldName = field.getElementName();
		builder.append("public ").append(fieldType).append(" get").append(firstCharUppercase(fieldName))
				.append("() {return this.").append(fieldName).append(";}");

		return builder.toString();
	}

	/**
	 * generate setter
	 * 
	 * @param field
	 * @return
	 * @throws JavaModelException
	 * @throws IllegalArgumentException
	 */
	private String genSetter(IField field) throws IllegalArgumentException, JavaModelException {
		StringBuilder builder = new StringBuilder();

		String fieldType = Signature.toString(field.getTypeSignature());
		String fieldName = field.getElementName();
		builder.append("public ").append("void").append(" set").append(firstCharUppercase(fieldName)).append("(")
				.append(fieldType).append(" ").append(fieldName).append(") { this.").append(fieldName).append(" = ")
				.append(fieldName).append(";}");

		return builder.toString();
	}

	/**
	 * generate build method
	 * 
	 * @param buildTypeName
	 * @param typeName
	 * @param fieldType
	 * @param fieldName
	 * @return
	 */
	private String genBuilderMethod(String buildTypeName, String typeName, String fieldType, String fieldName) {
		StringBuilder builder = new StringBuilder();

		builder.append("public ").append(buildTypeName).append(" ").append(fieldName).append("(").append(fieldType)
				.append(" ").append(fieldName).append(") {").append(firstCharLowercase(typeName)).append(".set")
				.append(firstCharUppercase(fieldName)).append("(").append(fieldName).append(");return this;}");

		return builder.toString();
	}

	/**
	 * generate build method
	 * 
	 * @param typeName
	 * @return
	 */
	private String genBuilMethod(String typeName) {
		StringBuilder builder = new StringBuilder();

		builder.append("public ").append(typeName).append(" ").append("build").append("() {").append("return new ").append(typeName).append("(this.")
				.append(firstCharLowercase(typeName)).append(");}");

		return builder.toString();
	}

	/**
	 * generate constructor
	 * 
	 * @param typeName
	 * @param fieldTypes
	 * @return
	 */
	private String genConstructor(String typeName, List<IField> fieldTypes) {
		StringBuilder builder = new StringBuilder();

		List<String> fieldsAssign = new ArrayList<String>();
		for (IField field : fieldTypes) {
			fieldsAssign.add("this." + field.getElementName() + " = " + firstCharLowercase(typeName) + ".get" + firstCharUppercase(field.getElementName()) + "();");
		}

		builder.append("public ").append(typeName).append("(").append(typeName).append(" ")
				.append(firstCharLowercase(typeName)).append(") {").append(String.join(" ", fieldsAssign)).append("}");

		return builder.toString();
	}	

	/**
	 * generate null param constructor
	 * 
	 * @param typeName
	 * @return
	 */
	private String genNullParamConstructor(String typeName) {
		StringBuilder builder = new StringBuilder();

		builder.append("public ").append(typeName).append("() {}");

		return builder.toString();
	}

	/**
	 * generate builder constructor
	 * 
	 * @param typeName
	 * @param fieldType
	 * @return
	 */
	private String genBuilderConstructor(String typeName, String fieldType) {
		StringBuilder builder = new StringBuilder();

		builder.append("public ").append(typeName).append("() {").append(firstCharLowercase(fieldType))
				.append(" = new ").append(fieldType).append("();}");

		return builder.toString();
	}

	/**
	 * first char to upper case
	 * 
	 * @param string
	 * @return
	 */
	private String firstCharUppercase(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	/**
	 * first char to lower case
	 * 
	 * @param string
	 * @return
	 */
	private String firstCharLowercase(String string) {
		return string.substring(0, 1).toLowerCase() + string.substring(1);
	}

}
