package tyRuBa.util;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegularExpression implements Serializable {
	private static final long serialVersionUID = 1L;

	private Pattern pattern;

	private String original;

	public RegularExpression(String pattern) throws PatternSyntaxException {
		original = pattern;
		this.pattern = Pattern.compile(".*(" + pattern + ").*");
	}

	public boolean matches(String s) {
		return pattern.matcher(s).matches();
	}

	public String toString() {
		return "/" + original + "/";
	}
}
