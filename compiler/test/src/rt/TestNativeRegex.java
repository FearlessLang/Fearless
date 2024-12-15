package rt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import rt.NativeRuntime.Regex;

public class TestNativeRegex {
	@Test void shouldCreateAndReleasePattern() {
		var patternStr = wrap("hello");
		new Regex(patternStr);
	}
	@Test void regexMatch() {
		var patternStr = wrap("hello");
		var regex = new Regex(patternStr);
		Assertions.assertTrue(regex.doesRegexMatch(wrap("hello")));
		Assertions.assertTrue(regex.doesRegexMatch(wrap("hello!")));
		Assertions.assertFalse(regex.doesRegexMatch(wrap("he")));
	}
	@Test void invalidRegex() {
		var patternStr = wrap("[");
		try {
			new Regex(patternStr);
			Assertions.fail("Did not throw");
		} catch (Regex.InvalidRegexError e) {
			Assertions.assertEquals(e.getMessage(), """
				regex parse error:
				    [
				    ^
				error: unclosed character class""");
		}
	}
	@Test void shouldCreateEmptyPattern() {
		var patternStr = ByteBuffer.allocateDirect(0).asReadOnlyBuffer();
		new Regex(patternStr);
	}
	private ByteBuffer wrap(String str) {
		var bytes = str.getBytes(StandardCharsets.UTF_8);
		return ByteBuffer.allocateDirect(bytes.length).put(bytes).asReadOnlyBuffer();
	}
}
