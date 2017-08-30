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
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import builgen.util.CodeFormatterUtil;

/**
 * BuilgenAction
 * @author chenlei
 * @version 1.0.0.qualifier
 * @github {@link} https://github.com/Vabshroo/
 */
@SuppressWarnings("restriction")
public class BuilgenAction implements IEditorActionDelegate {  
	
    private ISelection selection = null;  
    private Shell shell; 

    public void alert(Object content) {  
        MessageDialog.openInformation(shell, "ÌבÊ¾", content + "");  
    }  
      
  
      
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {  
        shell = targetEditor.getSite().getShell(); 
    }  
  
	public void run(IAction action) { 
    	CompilationUnit compilationUnit = ((CompilationUnit)((TreeSelection)selection).getFirstElement());
    	
    	try {
    		//all types
			IType types[] = compilationUnit.getAllTypes();
			
			if(types.length == 0) {
				alert("Error Type!");
				return;
			}
			
			//only generate the type found 1st
			IType classFile = types[0];
			if(classFile == null || !classFile.isClass()) {
				alert("Error Type!");
				return;
			}
			
			//all fields
			IField[] fields = classFile.getFields();
			if(fields.length == 0) {
				alert("No fields found!");
				return ;
			}

			//generate getter and setter
			IMethod method = null;
			List<IField> fieldTypes = new ArrayList<IField>();
			for(IField field : fields) {
				if(!field.getSource().contains(" static ") && !field.getSource().contains(" final ")) {
					method = classFile.createMethod(genGetter(field), method, false, null);
					method = classFile.createMethod(genSetter(field), method, false, null);
					fieldTypes.add(field);
				}
			}
			
			//generate builder
			classFile.createType(genBuilder(classFile.getElementName(),fieldTypes), method, false, null);
			
			//format file
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
	 * @param typeName
	 * @param fieldTypes
	 * @return
	 * @throws JavaModelException 
	 * @throws IllegalArgumentException 
	 */
    private String genBuilder(String typeName, List<IField> fieldTypes) throws IllegalArgumentException, JavaModelException {
    	StringBuilder builder = new StringBuilder();
    	
    	String builderTypeName = typeName + "Builder";
    	builder.append("public static class ").append(builderTypeName).append("{\n").append(typeName).append(" ").append(firstCharLowercase(typeName)).append(";\n")
    			.append(genBuilderConstructor(builderTypeName, typeName)).append("\n");
    	
    	for (IField field : fieldTypes) {
			
    		builder.append(genBuilderMethod(builderTypeName, typeName, Signature.toString(field.getTypeSignature()), field.getElementName())).append("\n");
    		
		}
    	builder.append(genBuilMethod(typeName)).append("\n}");
    	
		return builder.toString();
	}



	public void selectionChanged(IAction action, ISelection selection) {  
        this.selection=selection;  
    } 
    
    /**
     * generate getter
     * @param field
     * @return
     * @throws JavaModelException 
     * @throws IllegalArgumentException 
     */
    private String genGetter(IField field) throws IllegalArgumentException, JavaModelException {
    	StringBuilder builder = new StringBuilder();
    	
    	String fieldType = Signature.toString(field.getTypeSignature());
    	String fieldName = field.getElementName();
    	builder.append("public ").append(fieldType).append(" get").append(firstCharUppercase(fieldName)).append("() {\n\treturn this.").append(fieldName).append(";\n}");
    	
    	return builder.toString();
    }
    /**
     * generate setter
     * @param field
     * @return
     * @throws JavaModelException 
     * @throws IllegalArgumentException 
     */
    private String genSetter(IField field) throws IllegalArgumentException, JavaModelException {
    	StringBuilder builder = new StringBuilder();
    	
    	String fieldType = Signature.toString(field.getTypeSignature());
    	String fieldName = field.getElementName();
    	builder.append("public ").append("void").append(" set").append(firstCharUppercase(fieldName)).append("(").append(fieldType).append(" ").append(fieldName)
    			.append(") {\n\t this.").append(fieldName).append(" = ").append(fieldName).append(";\n}");
    	
    	return builder.toString();
    }

    /**
     * generate build method
     * @param buildTypeName
     * @param typeName
     * @param fieldType
     * @param fieldName 
     * @return
     */
    private String genBuilderMethod(String buildTypeName,String typeName,String fieldType, String fieldName) {
    	StringBuilder builder = new StringBuilder();
    	
    	builder.append("public ").append(buildTypeName).append(" ").append(fieldName).append("(").append(fieldType).append(" ").append(fieldName)
    	.append(") {\n\t").append(firstCharLowercase(typeName)).append(".set").append(firstCharUppercase(fieldName)).append("(").append(fieldName).append(");\n\treturn this;\n}");
    	
    	return builder.toString();
    }
    
    /**
     * generate build method
     * @param typeName
     * @return
     */
    private String genBuilMethod(String typeName) {
    	StringBuilder builder = new StringBuilder();
    	
    	builder.append("public ").append(typeName).append(" ").append("build").append("() {\n\t")
    			.append("return this.").append(firstCharLowercase(typeName)).append(";\n}");
    	
    	return builder.toString();
    }
    
    /**
     * generate constructor
     * @param typeName
     * @param fieldTypes
     * @return
     */
    private String genConstructor(String typeName,List<String> fieldTypes) {
    	StringBuilder builder = new StringBuilder();
    	
    	List<String> fieldsDeclar = new ArrayList<String>();
    	List<String> fieldsAssign = new ArrayList<String>();
    	for (String field : fieldTypes) {
			fieldsDeclar.add(field + " " + firstCharLowercase(field));
			fieldsAssign.add("\tthis." + firstCharLowercase(field) + " = " + firstCharLowercase(field) + ";");
		}
    	
    	builder.append("public ").append(typeName).append("(").append(String.join(",", fieldsDeclar)).append(") {\n").append(String.join("\n", fieldsAssign)).append("\n}");
    	
    	
    	return builder.toString();
    }
    
    /**
     * generate builder constructor
     * @param typeName
     * @param fieldType
     * @return
     */
    private String genBuilderConstructor(String typeName,String fieldType) {
    	StringBuilder builder = new StringBuilder();
    	
    	builder.append("public ").append(typeName).append("() {\n").append(firstCharLowercase(fieldType)).append(" = new ").append(fieldType).append("();\n}");
    	
    	return builder.toString();
    }
    
    /**
     * first char to upper case
     * @param string
     * @return
     */
    private String firstCharUppercase(String string) {
    	return string.substring(0,1).toUpperCase() + string.substring(1);
    }
    
    /**
     * first char to lower case
     * @param string
     * @return
     */
    private String firstCharLowercase(String string) {
    	return string.substring(0,1).toLowerCase() + string.substring(1);
    }
    
}  