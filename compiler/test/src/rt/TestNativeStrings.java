package rt;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import rt.NativeRuntime;

import static org.junit.jupiter.api.Assertions.*;

public class TestNativeStrings {
	@Test void shouldThrowOnInvalidUtf8() {
		// Arrange
		var invalidUtf8 = wrap(new byte[]{-28});
		// Act/Assert
		assertThrows(NativeRuntime.StringEncodingError.class, ()->NativeRuntime.validateStringOrThrow(invalidUtf8));
	}
	@Test void shouldAllowEmptyUtf8() {
		// Arrange
		var invalidUtf8 = wrap(new byte[0]);
		// Act/Assert
		NativeRuntime.validateStringOrThrow(invalidUtf8);
	}
	@Test void shouldProduceValidUtf8FromInvalidJava() {
		// Arrange
		var invalidUtf8 = new byte[]{-28};
		var jStr = new String(invalidUtf8, StandardCharsets.UTF_8);
		var utf8 = jStr.getBytes(StandardCharsets.UTF_8);
		// Act/Assert
		NativeRuntime.validateStringOrThrow(wrap(utf8));
		assertNotEquals(invalidUtf8, utf8);
	}
	@Test void shouldProduceA1GraphemeInvalidChar() {
		// Arrange
		var invalidUtf8 = new byte[]{-28};
		var jStr = new String(invalidUtf8, StandardCharsets.UTF_8);
		var utf8 = jStr.getBytes(StandardCharsets.UTF_8);
		var buf = wrap(utf8);
		// Act
		var size = NativeRuntime.indexString(buf).length;
		// Assert
		NativeRuntime.validateStringOrThrow(buf);
		assertEquals(1, size);
	}
	@Test void shouldHashConsistently() {
		var strA = "ABC".getBytes(StandardCharsets.UTF_8);
		var buf = wrap(strA);
		assertEquals(NativeRuntime.hashString(buf), NativeRuntime.hashString(buf));
	}
	@Test void shouldHashDifferently() {
		var strA = "ABC".getBytes(StandardCharsets.UTF_8);
		var strB = "DEF".getBytes(StandardCharsets.UTF_8);
		assertNotEquals(NativeRuntime.hashString(wrap(strA)), NativeRuntime.hashString(wrap(strB)));
	}

	@Test void shouldNormaliseEqForSimpleStrings() {
		var strA = "ABC".getBytes(StandardCharsets.UTF_8);
		assertEquals(wrap(strA), wrap(NativeRuntime.normaliseString(wrap(strA))));
	}
	@Test void shouldNormaliseDifferentlyForComplexStrings() {
		var strA = "Å".getBytes(StandardCharsets.UTF_8); // just Å
		var strB = "Å".getBytes(StandardCharsets.UTF_8); // A + ring

		assert streamBytes(strA).toList() != streamBytes(strB).toList();
		assertEquals(
			streamBytes(NativeRuntime.normaliseString(wrap(strA))).toList(),
			streamBytes(NativeRuntime.normaliseString(wrap(strB))).toList()
		);
	}
	@Test void shouldNotChangeSizeWhenNormalising() {
		var str = "Å".getBytes(StandardCharsets.UTF_8); // A + ring

		assertEquals(
			NativeRuntime.indexString(wrap(str)).length,
			NativeRuntime.indexString(wrap(NativeRuntime.normaliseString(wrap(str)))).length
		);
	}

	private Stream<Byte> streamBytes(byte[] input) {
		var stream = Stream.<Byte>builder();
		for (var b : input) {
			stream.accept(b);
		}
		return stream.build();
	}
	private ByteBuffer wrap(String str) {
		var bytes = str.getBytes(StandardCharsets.UTF_8);
		return ByteBuffer.allocateDirect(bytes.length).put(bytes).position(0);
	}
	private ByteBuffer wrap(byte[] bytes) {
		return ByteBuffer.allocateDirect(bytes.length).put(bytes).position(0);
	}
}
