package org.jvnet.hudson.plugins.collapsingconsolesections;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.console.ConsoleAnnotationOutputStream;
import hudson.model.Run;

import mockit.Mocked;

import org.junit.Test;

public class MavenDownloadAnnotationTest {

	private static final String name = "{1} ";
	private static final String sectionStart = "^Downloading: .*/\\d+(\\.\\d+)*/(.*)$";
	private static final String sectionEndPattern = "(.*\\r)*Downloaded: .*";
	private static final String url = "http://repo:8081/nexus/content/groups/public/org/apache/maven/plugins/maven-war-plugin/2.2/maven-war-plugin-2.2.pom";
	private static final String logStart = "Downloading: "+ url ;
	private static final String logEnd = "Downloaded: "+ url+" (7 KB at 27.0 KB/sec)";
	private static final String logBody = "4 KB   \r7 KB   \r       \r";
	private static final String logSample = logStart +"\n"
			+ logBody
			+ logEnd + "\n";

	@Test
	public void testMatch() {
		Pattern pStart = Pattern.compile(sectionStart);
		Pattern pEnd = Pattern.compile(sectionEndPattern);
		match(pStart, logStart);
		match(pEnd, logBody + logEnd +"\n");
	}

	private void match(Pattern pEnd, String input) {
		Matcher m = pEnd.matcher(input);
		assertTrue(m.matches());
//		System.out.println("\ninput="+ input);
//		boolean match = m.find(); 
//		System.out.println("matches="+ match +" length="+ input.length() +" start:"+ m.start() +" end="+ m.end());
//		System.out.println( "*"+ m.group() +"*");
	}

	@Test
	public void testAnnotation(@Mocked Run run) throws IOException {
		StringWriter out = new StringWriter();
		final SectionDefinition def = new SectionDefinition(name,
				sectionStart, sectionEndPattern);
		CollapsingSectionAnnotator ann = new CollapsingSectionAnnotator(def);
		OutputStream caos = new ConsoleAnnotationOutputStream<Run>(out, ann, run,
				Charset.defaultCharset());
		PrintStream ps = new PrintStream(caos);
		ps.println(logSample);
		ps.flush();
		assertTrue(out.toString().endsWith("</div>\r\n"));
	}
}
