package com.github.fancyerii.deepnlu.tools.client;

public class PathReplacer {
	public static String replaceVariableInPath(final String path, final String varName, final String varValue) {
		return path.replace("{" + varName + "}", varValue);
	}

	public static String replaceVariableInPath(final String path, final String varName1, final String varValue1,
			final String varName2, final String varValue2) {
		return replaceVariableInPath(replaceVariableInPath(path, varName1, varValue1), varName2, varValue2);
	}

	public static String replaceVariableInPath(final String path, final String varName1, final String varValue1,
			final String varName2, final String varValue2, final String varName3, final String varValue3) {
		return replaceVariableInPath(replaceVariableInPath(path, varName1, varValue1, varName2, varValue2), varName3,
				varValue3);
	}
}
