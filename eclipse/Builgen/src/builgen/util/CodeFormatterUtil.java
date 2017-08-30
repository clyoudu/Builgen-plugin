package builgen.util;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings("unchecked")
public class CodeFormatterUtil {
	
	// take default Eclipse formatting options
	private final static Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
	static {
		// initialize the compiler settings to be able to format 1.5 code
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

		// change the option to wrap each enum constant on a new line
		options.put(
			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
			DefaultCodeFormatterConstants.createAlignmentValue(
			true,
			DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
			DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
	}
	
	// instantiate the default code formatter with the given options
	final static CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);
	
	
	
	public static String format(String source) {
		TextEdit edit = codeFormatter.format(
				CodeFormatter.K_COMPILATION_UNIT, // format a compilation unit
				source, // source to format
				0, // starting position
				source.length(), // length
				0, // initial indentation
				System.getProperty("line.separator") // line separator
			);
		IDocument document = new Document(source);
		try {
			edit.apply(document);
			return document.get();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		return source;

	}

}
