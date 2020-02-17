package com.thatgamerblue.tools.diffparser;

import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main
{
	private static final Set<String> classes = new HashSet<>();

	public static void main(String[] args) throws IOException
	{
		// this is from commit cbb5c509394c8f59109b435491b91ad7998e01a2
		File file = new File("H:\\oprs-enabled-by-default\\commit.patch\\0001-client-Remove-all-plugins.patch");
		List<String> diff = Files.readAllLines(file.toPath());
		Patch<String> patches = UnifiedDiffUtils.parseUnifiedDiff(diff);
		patches.getDeltas().forEach((delta) -> {
			//System.out.println(delta.getSource().getLines());
			var ref = new Object()
			{
				boolean log = false;
				String className = "";
			};
			delta.getSource().getLines().forEach((line) -> {
				if (line.contains("enabledByDefault")) {
					ref.log = true;
				}

				if (line.contains("public class")) {
					ref.className = (line.replace("public class ", "").replace(" extends Plugin", "").replaceAll(" implements .*", ""));
				}
			});
			if (ref.log)
			{
				classes.add(ref.className + ".java");
			}
		});

		File rootDir = new File("H:\\oprs-plugins-enabled-by-default");
		parseRecursive(rootDir);
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
	private static void parseRecursive(File root) throws IOException
	{
		for (File f : root.listFiles())
		{
			if (f.isDirectory())
			{
				parseRecursive(f);
			}
			else
			{
				if (classes.contains(f.getName()))
				{
					List<String> lines = Files.readAllLines(f.toPath());
					List<String> write = new ArrayList<>();
					for (String line : lines)
					{
						write.add(line);
						int tabCount = line.length() - line.replace("\t", "").length();
						if (line.contains("name = \"") && line.trim().endsWith("\",")) {
							write.add("\t".repeat(tabCount) + "enabledByDefault = false,");
						}
					}
					f.delete();
					Files.writeString(f.toPath(), String.join("\n", write), StandardOpenOption.CREATE_NEW);
				}
			}
		}
	}

}
