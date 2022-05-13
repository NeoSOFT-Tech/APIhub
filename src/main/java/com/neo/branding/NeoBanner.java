package com.neo.branding;

import java.io.PrintStream;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;

public class NeoBanner implements Banner {

	private static final String[] BANNER = { "", " " + " .    _   _                                 __   _   __ _ _",
			" /\\\\  | \\ | |   ___    ___    ___    ___    / _| | |_ \\ \\ \\ \\",
			"( ( ) |  \\| |  / _ \\  / _ \\  / __|  / _ \\  | |_  | __| \\ \\ \\ \\",
			" \\\\/  | |\\  | |  __/ | (_) | \\__ \\ | (_) | |  _| | |_   ) ) ) )",
			"  '   |_| \\_|  \\___|  \\___/  |___/  \\___/  |_|    \\__| / / / /",
			" =====================================================/_/_/_/" };

	private static final String SPRING_BOOT = " :: Spring Boot :: ";

	private static final int STRAP_LINE_SIZE = 62;

	@Override
	public void printBanner(Environment environment, Class<?> sourceClass, PrintStream printStream) {
		for (String line : BANNER) {
			printStream.println(line);
		}
		String version = SpringBootVersion.getVersion();
		version = (version != null) ? " (v" + version + ")" : "";
		StringBuilder padding = new StringBuilder();
		while (padding.length() < STRAP_LINE_SIZE - (version.length() + SPRING_BOOT.length())) {
			padding.append(" ");
		}

		printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT, AnsiColor.DEFAULT, padding.toString(),
				AnsiStyle.FAINT, version));
		printStream.println();
	}
}
